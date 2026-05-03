package com.eloueduniv.monsit.domain.usecase

import com.eloueduniv.monsit.data.ai.SummarizationService
import com.eloueduniv.monsit.data.ai.TranscriptionService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

data class AiResult(
    val transcript: String,
    val summary: String
)

sealed class ProcessState {
    object Idle : ProcessState()
    object Transcribing : ProcessState()
    object Summarizing : ProcessState()
    data class Success(val result: AiResult) : ProcessState()
    data class Error(val message: String) : ProcessState()
}

class ProcessCallUseCase @Inject constructor(
    private val transcriptionService: TranscriptionService,
    private val summarizationService: SummarizationService
) {
    operator fun invoke(audioUrl: String): Flow<ProcessState> = flow {
        emit(ProcessState.Transcribing)
        try {
            val transcript = transcriptionService.transcribe(audioUrl)
            
            emit(ProcessState.Summarizing)
            val summary = summarizationService.summarize(transcript)
            
            emit(ProcessState.Success(AiResult(transcript, summary)))
        } catch (e: Exception) {
            emit(ProcessState.Error(e.message ?: "An unknown error occurred during AI processing"))
        }
    }
}
