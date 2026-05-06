package com.eloueduniv.monsit.presentation.call.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eloueduniv.monsit.data.model.Call
import com.eloueduniv.monsit.domain.usecase.AddCallUseCase
import com.eloueduniv.monsit.domain.usecase.ProcessCallUseCase
import com.eloueduniv.monsit.domain.usecase.ProcessState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddCallViewModel @Inject constructor(
    private val addCallUseCase: AddCallUseCase,
    private val processCallUseCase: ProcessCallUseCase
): ViewModel() {

    val _uiState = MutableStateFlow(AddCallUiState())
    val uiState: StateFlow<AddCallUiState> = _uiState.asStateFlow()


    fun onAction(action: AddCallUiAction) {
        when (action) {
            is AddCallUiAction.onContactNameChange -> {
                _uiState.value = _uiState.value.copy(contactName = action.contactName)
            }
            is AddCallUiAction.onCallDateChange -> {
                _uiState.value = _uiState.value.copy(callDate = action.callDate)
            }
            is AddCallUiAction.onCallTimeChange -> {
                _uiState.value = _uiState.value.copy(callTime = action.callTime)
            }
            is AddCallUiAction.onDurationChange -> {
                _uiState.value = _uiState.value.copy(duration = action.duration)
            }
            is AddCallUiAction.onAudioUrlChange -> {
                _uiState.value = _uiState.value.copy(audioUrl = action.audioUrl)
            }
            is AddCallUiAction.onNoteChange -> {
                _uiState.value = _uiState.value.copy(note = action.note)
            }
            is AddCallUiAction.onTranscriptChange -> {
                _uiState.value = _uiState.value.copy(transcript = action.transcript)
            }
            is AddCallUiAction.onSummaryChange -> {
                _uiState.value = _uiState.value.copy(summary = action.summary)
            }
            is AddCallUiAction.onProcessCall -> {
                processCall()
            }
            is AddCallUiAction.onAddCall -> {
                val call = Call(
                    id = 0,
                    contactName = _uiState.value.contactName ?: "",
                    startTime = _uiState.value.callDate.time + _uiState.value.callTime.time,
                    duration = _uiState.value.duration,
                    audioUrl = _uiState.value.audioUrl ?: "",
                    transcript = _uiState.value.transcript ?: "",
                    summary = _uiState.value.summary ?: "",
                    contactId = 0,
                    note = _uiState.value.note
                )
                viewModelScope.launch {
                    addCallUseCase(call)
                }
            }
        }
    }

    private fun processCall() {
        val audioUrl = _uiState.value.audioUrl ?: return
        viewModelScope.launch {
            processCallUseCase(audioUrl).collectLatest { state ->
                when (state) {
                    is ProcessState.Transcribing -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = true,
                            processingMessage = "Transcribing with Groq...",
                            errorMessage = null
                        )
                    }
                    is ProcessState.Summarizing -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = true,
                            processingMessage = "Summarizing with Gemini 2.5..."
                        )
                    }
                    is ProcessState.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            processingMessage = null,
                            transcript = state.result.transcript,
                            summary = state.result.summary
                        )
                    }
                    is ProcessState.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            processingMessage = null,
                            errorMessage = state.message
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}