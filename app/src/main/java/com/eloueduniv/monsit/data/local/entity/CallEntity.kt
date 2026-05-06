package com.eloueduniv.monsit.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "calls")
data class CallEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val contactName: String,
    val startTime: Long,
    val duration: Long,
    val audioUrl: String,
    val transcript: String,
    val summary: String,
    val contactId: Int,
    val note: String?
)