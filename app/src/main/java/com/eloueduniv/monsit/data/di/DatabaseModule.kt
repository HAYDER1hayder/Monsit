package com.eloueduniv.monsit.data.di

import android.content.Context
import androidx.room3.Room
import com.eloueduniv.monsit.data.local.AppDatabase
import com.eloueduniv.monsit.data.local.dao.CallDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "monsit_database"
        ).build()
    }

    @Provides
    fun provideCallDao(database: AppDatabase): CallDao {
        return database.callDao()
    }
}
