package com.agb.iraq.presentation.model

import com.agb.iraq.data.remote.model.QuotationItem

data class QuotationItemUi(
    val item: QuotationItem,
    val isScanned: Boolean = false
)
