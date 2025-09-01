package com.agb.iraq.data.repository

import androidx.paging.Pager
import com.agb.iraq.data.remote.model.ConfirmResponse
import com.agb.iraq.data.remote.model.QuotationDetailResponse
import com.agb.iraq.data.remote.model.QuotationItem

interface IErpRepository {
    suspend fun getQuotationsPager(): Pager<Int, QuotationItem>
    suspend fun getPurchasesPager(): Pager<Int, QuotationItem>
    suspend fun getQuotationById(id: Int): QuotationDetailResponse
    suspend fun getPurchasesById(id: Int): QuotationDetailResponse
    suspend fun confirmQuotation(quotationId: Int, fields: Map<String, String>): ConfirmResponse
    suspend fun confirmPurchases(quotationId: Int, fields: Map<String, String>): ConfirmResponse
}

