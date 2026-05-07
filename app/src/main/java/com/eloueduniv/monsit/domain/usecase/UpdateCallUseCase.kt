package com.eloueduniv.monsit.domain.usecase

import com.eloueduniv.monsit.data.model.Call
import com.eloueduniv.monsit.data.repository.CallRepository
import javax.inject.Inject

class UpdateCallUseCase @Inject constructor(
    private val callRepository: CallRepository
) {
    suspend operator fun invoke(call: Call) {
        callRepository.updateCall(call)
    }
}
