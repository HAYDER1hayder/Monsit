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
                return@withContext "Error: Groq API Key not configured."
            }

            val originalFile = getFileFromUri(audioUrl) ?: return@withContext "Error: Could not access audio file"
            
            // Try to split stereo
            val monoFiles = WavChannelSplitter.split(originalFile, context.cacheDir)
            
            if (monoFiles != null) {
                // Dual channel processing
                val leftResult = transcribeSingleFile(monoFiles.first, "You")
                val rightResult = transcribeSingleFile(monoFiles.second, "Contact")
                
                // Cleanup mono files
                monoFiles.first.delete()
                monoFiles.second.delete()
                
                // Merge and format
                mergeTranscripts(leftResult, rightResult)
            } else {
                // Fallback to single channel
                val response = transcribeFile(originalFile)
                response.text
            }
        } catch (e: Exception) {
            "Transcription failed: ${e.message}"
        }
    }

    private suspend fun transcribeSingleFile(file: File, speaker: String): List<TranscriptionSegment> {
        return try {
            val response = transcribeFile(file)
            response.segments?.map { it.copy(text = "$speaker: ${it.text}") } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private suspend fun transcribeFile(file: File): TranscriptionResponse {
        val requestFile = file.asRequestBody("audio/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", file.name, requestFile)
        val model = "whisper-large-v3".toRequestBody("text/plain".toMediaTypeOrNull())
        val temperature = "0".toRequestBody("text/plain".toMediaTypeOrNull())
        val responseFormat = "verbose_json".toRequestBody("text/plain".toMediaTypeOrNull())

        return api.transcribeAudio("Bearer $apiKey", body, model, temperature, responseFormat)
    }

    private fun mergeTranscripts(left: List<TranscriptionSegment>, right: List<TranscriptionSegment>): String {
        val allSegments = (left + right).sortedBy { it.start }
        return allSegments.joinToString("\n") { it.text.trim() }
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
