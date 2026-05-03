# Room 3 (KMP-Ready) Implementation Guide

This guide provides a detailed walkthrough of how to install and implement **Room 3** in an Android project using Clean Architecture and Hilt.

## 1. Installation & Configuration

### a. `libs.versions.toml`
Add the Room 3 version and libraries to your version catalog. Note that Room 3 uses the `androidx.room3` group.

```toml
[versions]
room3 = "3.0.0-alpha03"

[libraries]
room3-runtime = { group = "androidx.room3", name = "room3-runtime", version.ref = "room3" }
room3-compiler = { group = "androidx.room3", name = "room3-compiler", version.ref = "room3" }

[plugins]
room3 = { id = "androidx.room3", version.ref = "room3" }
```

### b. Root `build.gradle.kts`
Register the Room 3 plugin in the top-level build file.

```kotlin
plugins {
    alias(libs.plugins.room3) apply false
}
```

### c. App `build.gradle.kts`
Apply the plugin, configure the schema directory, and add dependencies.

```kotlin
plugins {
    alias(libs.plugins.ksp)
    alias(libs.plugins.room3)
}

android {
    // ...
}

room3 {
    // Required: Sets the directory where Room should export database schemas
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(libs.room3.runtime)
    ksp(libs.room3.compiler)
    // Note: room3-ktx is no longer needed as coroutines support is built into runtime
}
```

## 2. Implementation Components

### a. Entity
Define your database table. Use `androidx.room3` annotations.

```kotlin
package com.eloueduniv.monsit.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey val id: String,
    val contactName: String,
    val startTime: Long,
    val duration: Long,
    val audioUrl: String,
    val transcript: String,
    val summary: String,
    val contactId: Int,
    val note: String?
)
```

### b. Mapper
Create a dedicated mapper to convert between Domain models and Database entities. This keeps your data layers decoupled.

```kotlin
package com.eloueduniv.monsit.data.mapper

import com.eloueduniv.monsit.data.local.entity.CallEntity
import com.eloueduniv.monsit.data.model.Call

fun CallEntity.asExternalModel(): Call = Call(
    id = id,
    contactName = contactName,
    startTime = startTime,
    duration = duration,
    audioUrl = audioUrl,
    transcript = transcript,
    summary = summary,
    contactId = contactId,
    note = note
)

fun Call.asEntity(): CallEntity = CallEntity(
    id = id,
    contactName = contactName,
    startTime = startTime,
    duration = duration,
    audioUrl = audioUrl,
    transcript = transcript,
    summary = summary,
    contactId = contactId,
    note = note
)
```

### b. DAO (Data Access Object)
Define the database operations. Use `Flow` for reactive updates and `suspend` for one-shot tasks.

```kotlin
package com.eloueduniv.monsit.data.local.dao

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import com.eloueduniv.monsit.data.local.entity.CallEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallDao {
    @Query("SELECT * FROM calls ORDER BY startTime DESC")
    fun getCalls(): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls ORDER BY startTime DESC LIMIT 5")
    fun getRecentCalls(): Flow<List<CallEntity>>

    @Query("SELECT * FROM calls WHERE id = :id")
    suspend fun getCall(id: String): CallEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallEntity)
}
```

### c. Database
Define the main database class. Ensure `exportSchema = false` if you don't want to manage schema versions manually yet.

```kotlin
package com.eloueduniv.monsit.data.local

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.eloueduniv.monsit.data.local.dao.CallDao
import com.eloueduniv.monsit.data.local.entity.CallEntity

@Database(entities = [CallEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun callDao(): CallDao
}
```

## 3. Dependency Injection (Hilt)

Provide the Database and DAOs to the rest of the application.

```kotlin
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
```

## 5. Repository Implementation

Use the DAO in your Repository implementation to bridge the Local Data source with the Domain layer.

```kotlin
package com.eloueduniv.monsit.data.repository

import com.eloueduniv.monsit.data.local.dao.CallDao
import com.eloueduniv.monsit.data.mapper.asEntity
import com.eloueduniv.monsit.data.mapper.asExternalModel
import com.eloueduniv.monsit.data.model.Call
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomCallRepositoryImpl @Inject constructor(
    private val callDao: CallDao
) : CallRepository {

    override fun getCalls(): Flow<List<Call>> {
        return callDao.getCalls().map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override suspend fun getCall(id: String): Call? {
        return callDao.getCall(id)?.asExternalModel()
    }

    override suspend fun addCall(call: Call) {
        callDao.insertCall(call.asEntity())
    }
    
    // ...
}
```

## 5. Summary of Key Differences in Room 3
1.  **Package:** Use `androidx.room3` instead of `androidx.room`.
2.  **Plugin:** Use the `androidx.room3` Gradle plugin.
3.  **DSL:** Configuration uses `room3 { ... }`.
4.  **Kotlin Multiplatform:** Designed to work across Android, iOS, and Desktop.
5.  **No `-ktx`:** Coroutines support is now built directly into the runtime.
