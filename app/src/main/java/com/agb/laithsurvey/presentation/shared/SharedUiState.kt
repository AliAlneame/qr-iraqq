package com.agb.laithsurvey.presentation.shared

data class SharedUiState(
    val isLoading: Boolean = false,
    val isButtonLoading: Boolean = false,
    val toastMessage: String = "",
    val feedbackSubmitted: Boolean = false,
    val rating: Int = 0,
    val name: String = "",
    val phoneNumber: String = "",
    val location: String? = null,
    val branchName: String? = null,
    val complaint: String? = null
)