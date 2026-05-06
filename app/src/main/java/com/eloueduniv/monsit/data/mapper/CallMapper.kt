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
