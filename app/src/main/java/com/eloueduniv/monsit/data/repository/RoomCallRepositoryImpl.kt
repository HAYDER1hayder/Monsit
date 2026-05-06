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
): CallRepository {

    override fun getCalls(): Flow<List<Call>> {
        return callDao.getCalls().map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override fun getResentCalls(): Flow<List<Call>> {
        return callDao.getRecentCalls().map { entities ->
            entities.map { it.asExternalModel() }
        }
    }

    override suspend fun getCall(id: Int): Call? {
        return callDao.getCall(id)?.asExternalModel()
    }

    override suspend fun addCall(call: Call) {
        callDao.insertCall(call.asEntity())
    }
}