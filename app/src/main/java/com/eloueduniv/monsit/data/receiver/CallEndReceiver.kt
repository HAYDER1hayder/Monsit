package com.eloueduniv.monsit.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.eloueduniv.monsit.data.worker.SyncWorker

class CallEndReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("CallEndReceiver", "onReceive: ${intent.action}")
        if (intent.action == TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            Log.d("CallEndReceiver", "Phone State: $state")
            if (state == TelephonyManager.EXTRA_STATE_IDLE) {
                Log.d("CallEndReceiver", "Call ended, triggering SyncWorker")
                // Call has ended. Trigger the SyncWorker uniquely to avoid duplicates.
                val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>().build()
                WorkManager.getInstance(context).enqueueUniqueWork(
                    "SyncCallsWork",
                    androidx.work.ExistingWorkPolicy.REPLACE,
                    syncRequest
                )
            }
        }
    }
}
