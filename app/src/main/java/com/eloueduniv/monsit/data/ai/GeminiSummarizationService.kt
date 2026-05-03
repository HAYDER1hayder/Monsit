package com.eloueduniv.monsit.data.ai

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiSummarizationService @Inject constructor() : SummarizationService {

    private val apiKey = com.eloueduniv.monsit.BuildConfig.GEMINI_API_KEY
    
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = apiKey
    )

    override suspend fun summarize(text: String): String = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isEmpty()) {
                return@withContext "Error: Gemini API Key not configured. Please add 'gemini.api.key=your_key' to local.properties."
            }

            val response = generativeModel.generateContent(
                content {
                    text("Summarize the following call transcript concisely and highlight key action items:\n\n$text")
                }
            )
            response.text ?: "Summarization failed: Empty response"
        } catch (e: Exception) {
            "Summarization failed: ${e.message}"
        }
    }
}
