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
