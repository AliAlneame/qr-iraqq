package com.agb.laithsurvey.data.repository

import androidx.paging.Pager
import com.agb.laithsurvey.data.remote.model.ConfirmResponse
import com.agb.laithsurvey.data.remote.model.QuotationDetailResponse
import com.agb.laithsurvey.data.remote.model.QuotationItem
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

interface IErpRepository {
    suspend fun getQuotationsPager(): Pager<Int, QuotationItem>
    suspend fun getPurchasesPager(): Pager<Int, QuotationItem>
    suspend fun getQuotationById(id: Int): QuotationDetailResponse
    suspend fun getPurchasesById(id: Int): QuotationDetailResponse
    suspend fun confirmQuotation(quotationId: Int, fields: Map<String, String>): ConfirmResponse
    suspend fun confirmPurchases(quotationId: Int, fields: Map<String, String>): ConfirmResponse
}

