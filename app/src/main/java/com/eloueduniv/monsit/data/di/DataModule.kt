package com.eloueduniv.monsit.data.di

import com.eloueduniv.monsit.data.local.dao.CallDao
import com.eloueduniv.monsit.data.repository.CallRepository
import com.eloueduniv.monsit.data.repository.RoomCallRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideCallRepository(callDao: CallDao): CallRepository = RoomCallRepositoryImpl(callDao)
}
