package com.eloueduniv.monsit.presentation.call.detail

sealed interface CallDetailUiAction {
    data object OnBack : CallDetailUiAction
    data object OnPlayPause : CallDetailUiAction
    data class OnSeek(val position: Float) : CallDetailUiAction
    data object OnShuffle : CallDetailUiAction
    data object OnLoop : CallDetailUiAction
    data object OnForward : CallDetailUiAction
    data object OnBackward : CallDetailUiAction
    data object OnReprocess : CallDetailUiAction
}
