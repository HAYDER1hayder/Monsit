package com.eloueduniv.monsit.data.ai

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OpenAiApi {
    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun transcribeAudio(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("temperature") temperature: RequestBody? = null,
        @Part("response_format") responseFormat: RequestBody? = null
    ): TranscriptionResponse
}

data class TranscriptionResponse(
    val text: String,
    val segments: List<TranscriptionSegment>? = null
)

data class TranscriptionSegment(
    val start: Double,
    val end: Double,
    val text: String
)
