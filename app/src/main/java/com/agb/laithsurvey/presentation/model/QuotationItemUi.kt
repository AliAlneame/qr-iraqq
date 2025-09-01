package com.agb.laithsurvey.presentation.model

import com.agb.laithsurvey.data.remote.model.QuotationItem

data class QuotationItemUi(
    val item: QuotationItem,
    val isScanned: Boolean = false
)