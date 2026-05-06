package com.eloueduniv.monsit.domain.usecase

import com.eloueduniv.monsit.data.model.Call
import com.eloueduniv.monsit.data.repository.CallRepository
import javax.inject.Inject

class GetCallByIdUseCase @Inject constructor(
    private val repository: CallRepository
) {
    suspend operator fun invoke(id: Int): Call? {
        return repository.getCall(id)
    }
}
