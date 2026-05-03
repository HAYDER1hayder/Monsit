package com.eloueduniv.monsit.data.repository

import com.eloueduniv.monsit.data.model.Call
import kotlinx.coroutines.flow.Flow

interface CallRepository {

    fun getCalls(): Flow<List<Call>>

    fun getResentCalls(): Flow<List<Call>>

    suspend fun getCall(id: Int): Call?

    suspend fun addCall(call: Call)

}