package com.eloueduniv.monsit.data.ai

interface TranscriptionService {
    suspend fun transcribe(audioUrl: String): String
}
