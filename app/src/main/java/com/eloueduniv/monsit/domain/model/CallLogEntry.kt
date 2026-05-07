package com.eloueduniv.monsit.domain.model

data class CallLogEntry(
    val number: String,
    val name: String?,
    val date: Long,
    val duration: Long,
    val type: Int // CallLog.Calls.INCOMING_TYPE, etc.
)
