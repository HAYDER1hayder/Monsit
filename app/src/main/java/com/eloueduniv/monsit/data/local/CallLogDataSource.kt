package com.eloueduniv.monsit.data.local

import android.content.Context
import android.provider.CallLog
import com.eloueduniv.monsit.domain.model.CallLogEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class CallLogDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getRecentCallLogs(limit: Int = 10): List<CallLogEntry> {
        val callLogs = mutableListOf<CallLogEntry>()
        val cursor = context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            null,
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )

        cursor?.use {
            val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
            val nameIndex = it.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
            val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
            val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)

            var count = 0
            while (it.moveToNext() && count < limit) {
                callLogs.add(
                    CallLogEntry(
                        number = it.getString(numberIndex),
                        name = it.getString(nameIndex),
                        date = it.getLong(dateIndex),
                        duration = it.getLong(durationIndex),
                        type = it.getInt(typeIndex)
                    )
                )
                count++
            }
        }
        return callLogs
    }
}
