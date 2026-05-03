package com.eloueduniv.monsit.presentation.call.add

import java.util.Date

sealed class AddCallUiAction {
    data class onContactNameChange(val contactName: String) : AddCallUiAction()
    data class onCallDateChange(val callDate: Date) : AddCallUiAction()
    data class onCallTimeChange(val callTime: Date) : AddCallUiAction()
    data class onDurationChange(val duration: Long) : AddCallUiAction()
    data class onAudioUrlChange(val audioUrl: String) : AddCallUiAction()
    data class onNoteChange(val note: String) : AddCallUiAction()
    object onProcessCall : AddCallUiAction()
    data class onTranscriptChange(val transcript: String) : AddCallUiAction()
    data class onSummaryChange(val summary: String) : AddCallUiAction()
    object onAddCall : AddCallUiAction()
}
