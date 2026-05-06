package com.eloueduniv.monsit.data.ai

interface SummarizationService {
    suspend fun summarize(text: String): String
}
