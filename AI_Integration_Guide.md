# Comprehensive AI Integration Guide: Monsit App

This document explains the step-by-step implementation of the cloud-based AI transcription and summarization pipeline in the Monsit application.

---

## 1. Secure API Key Management
To prevent leaking sensitive API keys to GitHub, we implemented a secure storage system using `local.properties`.

### Step: Enable BuildConfig and Property Loading
In `app/build.gradle.kts`, we enabled `buildConfig` and added logic to read keys from `local.properties` at build time.

```kotlin
// In app/build.gradle.kts
defaultConfig {
    // ...
    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }

    buildConfigField("String", "GROQ_API_KEY", "\"${localProperties.getProperty("groq.api.key") ?: ""}\"")
    buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("gemini.api.key") ?: ""}\"")
}

buildFeatures {
    buildConfig = true
}
```

---

## 2. Dependency Configuration
We transitioned from local MediaPipe models to cloud SDKs for better performance and smaller app size.

### Step: Update `libs.versions.toml`
We added Retrofit for API calls and the Google AI SDK for Gemini.

```toml
[versions]
generativeai = "0.9.0"
retrofit = "2.9.0"
okhttp = "4.12.0"

[libraries]
google-ai-client = { group = "com.google.ai.client.generativeai", name = "generativeai", version.ref = "generativeai" }
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
# ... okhttp and others
```

---

## 3. Transcription Service (Groq)
We use **Groq** for transcription because it provides the fastest Whisper inference available.

### Step: API Interface (`OpenAiApi.kt`)
Groq is OpenAI-compatible, so we defined a standard multipart interface.

```kotlin
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
```

### Step: Service Implementation (`CloudWhisperTranscriptionService.kt`)
This service handles file URIs, copies them to a temporary file if needed, and uploads them to Groq.

```kotlin
@Singleton
class CloudWhisperTranscriptionService @Inject constructor(
    @ApplicationContext private val context: Context
) : TranscriptionService {
    private val apiKey = BuildConfig.GROQ_API_KEY
    private val api = Retrofit.Builder().baseUrl("https://api.groq.com/openai/")...

    override suspend fun transcribe(audioUrl: String): String {
        val file = getFileFromUri(audioUrl) // Helper to handle content:// URIs
        val body = MultipartBody.Part.createFormData("file", file.name, file.asRequestBody())
        val response = api.transcribeAudio("Bearer $apiKey", body, "whisper-large-v3".toRequestBody())
        return response.text
    }
}
```

---

## 4. Summarization Service (Gemini 2.5)
We use **Gemini 2.5 Flash** for high-quality, concise call summaries.

### Step: Service Implementation (`GeminiSummarizationService.kt`)
Uses the official Google AI SDK to generate content from the transcript.

```kotlin
@Singleton
class GeminiSummarizationService @Inject constructor() : SummarizationService {
    private val apiKey = BuildConfig.GEMINI_API_KEY
    private val generativeModel = GenerativeModel(modelName = "gemini-2.5-flash", apiKey = apiKey)

    override suspend fun summarize(text: String): String {
        val response = generativeModel.generateContent("Summarize this transcript: $text")
        return response.text ?: ""
    }
}
```

---

## 5. UI and ViewModel Integration
The UI allows users to pick a file and triggers the AI pipeline.

### Step: Audio File Picker (`AddCallView.kt`)
Used `rememberLauncherForActivityResult` for a clean, permission-less file picking experience.

```kotlin
val audioPickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri ->
    uri?.let { viewModel.onAction(AddCallUiAction.onAudioUrlChange(it.toString())) }
}
```

### Step: Orchestration (`AddCallViewModel.kt`)
The ViewModel manages the state transitions: `Transcribing` -> `Summarizing` -> `Success`.

```kotlin
private fun processCall() {
    viewModelScope.launch {
        processCallUseCase(audioUrl).collect { state ->
            when (state) {
                is ProcessState.Transcribing -> updateMessage("Transcribing with Groq...")
                is ProcessState.Summarizing -> updateMessage("Summarizing with Gemini 2.5...")
                is ProcessState.Success -> updateResults(state.result)
            }
        }
    }
}
```

---

## Summary of Benefits
1. **Speed**: Groq provides near-instant transcription.
2. **Quality**: Gemini 2.5 Flash produces superior summaries compared to lightweight on-device models.
3. **Security**: Keys are safely stored in `local.properties`.
4. **Clean Architecture**: Services are decoupled through interfaces and injected via Hilt.
