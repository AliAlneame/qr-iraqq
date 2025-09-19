package com.agb.iraq.data.repository

import androidx.paging.Pager
import com.agb.iraq.data.remote.model.ConfirmResponse
import com.agb.iraq.data.remote.model.ProductData
import com.agb.iraq.data.remote.model.ProductResponse
import com.agb.iraq.data.remote.model.QuotationDetailResponse
import com.agb.iraq.data.remote.model.QuotationItem
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface IErpRepository {
    suspend fun getQuotationsPager(): Pager<Int, QuotationItem>
    suspend fun getPurchasesPager(): Pager<Int, QuotationItem>
    suspend fun getQuotationById(id: Int): QuotationDetailResponse
    suspend fun getPurchasesById(id: Int): QuotationDetailResponse
    suspend fun confirmQuotation(quotationId: Int, fields: Map<String, String>): ConfirmResponse
    suspend fun confirmPurchases(quotationId: Int, fields: Map<String, String>): ConfirmResponse
    fun updateQuotation(
        quotationId: Int,
        customerId: Int,
        warehouseId: Int,
        quotationDate: String,
        items: Map<String, String>
    ): ConfirmResponse

    suspend fun getProducts(sku: String, warehouseId: Int): List<ProductData>
}

