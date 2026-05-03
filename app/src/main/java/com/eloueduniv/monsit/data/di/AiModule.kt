package com.eloueduniv.monsit.data.di

import com.eloueduniv.monsit.data.ai.CloudWhisperTranscriptionService
import com.eloueduniv.monsit.data.ai.GeminiSummarizationService
import com.eloueduniv.monsit.data.ai.SummarizationService
import com.eloueduniv.monsit.data.ai.TranscriptionService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AiModule {

    @Binds
    @Singleton
    abstract fun bindTranscriptionService(
        cloudWhisperTranscriptionService: CloudWhisperTranscriptionService
    ): TranscriptionService

    @Binds
    @Singleton
    abstract fun bindSummarizationService(
        geminiSummarizationService: GeminiSummarizationService
    ): SummarizationService
}
