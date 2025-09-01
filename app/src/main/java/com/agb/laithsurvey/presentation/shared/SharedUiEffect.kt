package com.agb.laithsurvey.presentation.shared

import com.agb.laithsurvey.presentation.base.BaseUiEffect

sealed class SharedUiEffect : BaseUiEffect {
    data class ShowToast(val message: String) : SharedUiEffect()
}