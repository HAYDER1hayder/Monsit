package com.eloueduniv.monsit.data.ai

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudWhisperTranscriptionService @Inject constructor(
    @ApplicationContext private val context: Context
) : TranscriptionService {

    private val apiKey = com.eloueduniv.monsit.BuildConfig.GROQ_API_KEY

    private val api = Retrofit.Builder()
        .baseUrl("https://api.groq.com/openai/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(OpenAiApi::class.java)

    override suspend fun transcribe(audioUrl: String): String = withContext(Dispatchers.IO) {
        try {
            if (apiKey.isEmpty()) {
                return@withContext "Error: Groq API Key not configured. Please add 'groq.api.key=your_key' to local.properties."
            }

            val file = getFileFromUri(audioUrl) ?: return@withContext "Error: Could not access audio file"
            
            val requestFile = file.asRequestBody("audio/*".toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
            val model = "whisper-large-v3".toRequestBody("text/plain".toMediaTypeOrNull())
            val temperature = "0".toRequestBody("text/plain".toMediaTypeOrNull())
            val responseFormat = "verbose_json".toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.transcribeAudio("Bearer $apiKey", body, model, temperature, responseFormat)
            response.text
        } catch (e: Exception) {
            "Transcription failed: ${e.message}"
        }
    }

    private fun getFileFromUri(uriString: String): File? {
        return try {
            val uri = Uri.parse(uriString)
            // If it's already a file path
            if (uri.scheme == null || uri.scheme == "file") {
                return File(uri.path ?: uriString)
            }
            
            // If it's a content URI, we need to copy it to a temp file
            val tempFile = File(context.cacheDir, "temp_audio_${System.currentTimeMillis()}.wav")
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            null
        }
    }
}
