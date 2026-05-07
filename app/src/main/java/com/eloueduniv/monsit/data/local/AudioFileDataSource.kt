package com.eloueduniv.monsit.data.local

import android.content.Context
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.math.abs

class AudioFileDataSource @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Finds an audio file that matches the phone number and timestamp.
     * marginMillis: Time window to search around the call start time (default 10s).
     */
    fun findMatchingAudio(phoneNumber: String, timestamp: Long, marginMillis: Long = 10000): String? {
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DISPLAY_NAME
        )

        // We search for all audio files added around the timestamp
        // DATE_ADDED is in seconds, so we convert timestamp to seconds
        val startTimeSec = (timestamp - marginMillis) / 1000
        val endTimeSec = (timestamp + marginMillis) / 1000

        val selection = "${MediaStore.Audio.Media.DATE_ADDED} BETWEEN ? AND ?"
        val selectionArgs = arrayOf(startTimeSec.toString(), endTimeSec.toString())

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            "${MediaStore.Audio.Media.DATE_ADDED} DESC"
        )

        cursor?.use {
            val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            val nameIndex = it.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)

            while (it.moveToNext()) {
                val filePath = it.getString(dataIndex)
                val fileName = it.getString(nameIndex)

                // Heuristic: Check if phone number is in the filename or path
                // Most native recorders include the number in the name
                if (fileName.contains(phoneNumber.takeLast(8)) || filePath.contains(phoneNumber.takeLast(8))) {
                    return filePath
                }
                
                // Fallback: If no number found but it's the only file in that window, 
                // and it contains "Call" or "Recorder"
                if (fileName.contains("Call", ignoreCase = true) || fileName.contains("Record", ignoreCase = true)) {
                    return filePath
                }
            }
        }
        return null
    }
}
