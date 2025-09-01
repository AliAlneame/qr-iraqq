package com.agb.iraq.presentation.shared

import com.agb.iraq.presentation.base.BaseUiEffect

sealed class SharedUiEffect : BaseUiEffect {
    data class ShowToast(val message: String) : SharedUiEffect()
}
