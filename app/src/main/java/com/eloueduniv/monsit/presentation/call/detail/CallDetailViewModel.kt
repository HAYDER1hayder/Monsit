package com.eloueduniv.monsit.presentation.call.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eloueduniv.monsit.domain.usecase.GetCallByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CallDetailViewModel @Inject constructor(
    private val getCallByIdUseCase: GetCallByIdUseCase,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CallDetailUiState())
    val uiState: StateFlow<CallDetailUiState> = _uiState.asStateFlow()

    private val callId: Int? = savedStateHandle.get<String>("callId")?.toIntOrNull()

    private var mediaPlayer: android.media.MediaPlayer? = null
    private var playbackJob: kotlinx.coroutines.Job? = null
    private var isPrepared = false
    private var playOnPrepared = false

    init {
        fetchCallDetail()
    }

    private fun fetchCallDetail() {
        callId?.let { id ->
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }
                val call = getCallByIdUseCase(id)
                if (call != null) {
                    _uiState.update { it.copy(call = call, isLoading = false) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Call not found") }
                }
            }
        } ?: run {
            _uiState.update { it.copy(error = "Invalid call ID") }
        }
    }

    private fun prepareMediaPlayer(audioUrl: String) {
        if (mediaPlayer != null || audioUrl.isBlank()) return
        mediaPlayer = android.media.MediaPlayer().apply {
            try {
                if (audioUrl.startsWith("content://")) {
                    setDataSource(context, android.net.Uri.parse(audioUrl))
                } else {
                    setDataSource(audioUrl)
                }
                setOnPreparedListener {
                    isPrepared = true
                    if (playOnPrepared) {
                        it.start()
                        startProgressUpdate()
                    }
                }
                setOnCompletionListener {
                    _uiState.update { state -> state.copy(isPlaying = false, currentPosition = 0f) }
                    stopProgressUpdate()
                }
                setOnErrorListener { _, what, extra ->
                    _uiState.update { state -> state.copy(error = "MediaPlayer Error: $what, $extra (URL: $audioUrl)") }
                    false
                }
                prepareAsync()
            } catch (e: Exception) {
                _uiState.update { state -> state.copy(error = "Failed to load audio: ${e.message} (URL: $audioUrl)") }
            }
        }
    }

    fun onAction(action: CallDetailUiAction) {
        when (action) {
            CallDetailUiAction.OnPlayPause -> {
                val call = _uiState.value.call ?: return
                if (mediaPlayer == null) {
                    playOnPrepared = true
                    prepareMediaPlayer(call.audioUrl)
                    _uiState.update { it.copy(isPlaying = true) }
                } else {
                    _uiState.update { 
                        val isPlaying = !it.isPlaying
                        if (isPlaying) {
                            if (isPrepared) {
                                mediaPlayer?.start()
                                startProgressUpdate()
                            } else {
                                playOnPrepared = true
                            }
                        } else {
                            mediaPlayer?.pause()
                            playOnPrepared = false
                            stopProgressUpdate()
                        }
                        it.copy(isPlaying = isPlaying) 
                    }
                }
            }
            is CallDetailUiAction.OnSeek -> {
                if (isPrepared) {
                    mediaPlayer?.let {
                        val seekPos = (action.position * it.duration).toInt()
                        it.seekTo(seekPos)
                    }
                }
                _uiState.update { it.copy(currentPosition = action.position) }
            }
            CallDetailUiAction.OnBack -> {}
            CallDetailUiAction.OnBackward -> {
                if (isPrepared) {
                    mediaPlayer?.let {
                        val newPos = (it.currentPosition - 5000).coerceAtLeast(0)
                        it.seekTo(newPos)
                    }
                }
            }
            CallDetailUiAction.OnForward -> {
                if (isPrepared) {
                    mediaPlayer?.let {
                        val newPos = (it.currentPosition + 5000).coerceAtMost(it.duration)
                        it.seekTo(newPos)
                    }
                }
            }
            CallDetailUiAction.OnLoop -> {
                mediaPlayer?.let { it.isLooping = !it.isLooping }
            }
            CallDetailUiAction.OnShuffle -> {}
        }
    }

    private fun startProgressUpdate() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(500)
                mediaPlayer?.let { mp ->
                    if (isPrepared && mp.isPlaying) {
                        val progress = mp.currentPosition.toFloat() / mp.duration.toFloat()
                        _uiState.update { it.copy(currentPosition = progress) }
                    }
                }
            }
        }
    }

    private fun stopProgressUpdate() {
        playbackJob?.cancel()
        playbackJob = null
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
        stopProgressUpdate()
    }
}
