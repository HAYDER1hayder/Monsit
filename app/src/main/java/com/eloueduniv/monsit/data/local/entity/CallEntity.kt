package com.eloueduniv.monsit.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.eloueduniv.monsit.data.model.Call

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey
    val id: String,
    val contactName: String,
    val startTime: Long,
    val duration: Long,
    val audioUrl: String,
    val transcript: String,
    val summary: String,
    val contactId: Int,
    val note: String?
)
