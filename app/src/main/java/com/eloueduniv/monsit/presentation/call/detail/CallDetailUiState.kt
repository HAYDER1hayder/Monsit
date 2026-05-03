package com.eloueduniv.monsit.presentation.call.detail

import com.eloueduniv.monsit.data.model.Call

data class CallDetailUiState(
    val call: Call? = null,
    val isLoading: Boolean = false,
    val isPlaying: Boolean = false,
    val currentPosition: Float = 0f,
    val error: String? = null
)
