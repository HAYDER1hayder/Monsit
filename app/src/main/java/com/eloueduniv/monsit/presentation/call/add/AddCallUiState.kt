package com.eloueduniv.monsit.presentation.call.add

import java.util.Date

data class AddCallUiState(
    val contactName : String? = null,
    val callDate : Date = Date(),
    val callTime : Date = Date(),
    val duration : Long = 0,
    val audioUrl: String? = null,
    val note : String? = null,
    val isProcessing: Boolean = false,
    val processingMessage: String? = null,
    val transcript: String? = null,
    val summary: String? = null,
    val errorMessage: String? = null
)
