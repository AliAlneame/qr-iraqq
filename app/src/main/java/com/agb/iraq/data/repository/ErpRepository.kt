package com.agb.iraq.data.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.agb.iraq.data.paging.PurchasesPagingSource
import com.agb.iraq.data.paging.QuotationPagingSource
import com.agb.iraq.data.remote.api.QuotationApi
import com.agb.iraq.data.remote.model.BaseResponse
import com.agb.iraq.data.remote.model.ConfirmResponse
import com.agb.iraq.data.remote.model.ProductData
import com.agb.iraq.data.remote.model.ProductResponse
import com.agb.iraq.data.remote.model.QuotationDetailResponse
import com.agb.iraq.data.remote.model.QuotationItem
import com.agb.iraq.data.remote.model.UpdateResponse
import retrofit2.Response
import javax.inject.Inject

class ErpRepository @Inject constructor(
    private val api: QuotationApi
) : IErpRepository {

    override suspend fun getQuotationsPager(): Pager<Int, QuotationItem> {
        return Pager(
            config = PagingConfig(pageSize = 15),
            pagingSourceFactory = { QuotationPagingSource(api) }
        )
    }

    override suspend fun getPurchasesPager(): Pager<Int, QuotationItem> {
        return Pager(
            config = PagingConfig(pageSize = 15),
            pagingSourceFactory = { PurchasesPagingSource(api) }
        )
    }

    override suspend fun getQuotationById(id: Int): QuotationDetailResponse {
        return api.getQuotationById(id)
    }

    override suspend fun getPurchasesById(id: Int): QuotationDetailResponse {
        return api.getPurchasesById(id)
    }

    override suspend fun confirmQuotation(
        quotationId: Int,
        fields: Map<String, String>
    ): ConfirmResponse {
        return api.confirmQuotation(quotationId,fields)
    }

    override suspend fun confirmPurchases(
        quotationId: Int,
        fields: Map<String, String>
    ): ConfirmResponse {
        return api.confirmPurchases(quotationId,fields)
    }

    override suspend fun updateQuotation(
        quotationId: Int,
        customerId: Int,
        warehouseId: Int,
        quotationDate: String,
        items: Map<String, String>
    ): ConfirmResponse {
        return api.updateQuotation(quotationId,customerId,warehouseId,quotationDate,items)
    }

    override suspend fun getProducts(sku: String, warehouseId: Int): List<ProductData> {
        return api.getProducts(sku, warehouseId).data ?: emptyList()
    }

    private suspend fun <T> wrapResponse(
        function: suspend () -> Response<BaseResponse<T>>
    ): T {
        try {
            val apiResponse = function()
            val response = apiResponse.body()
            if (apiResponse.isSuccessful) {
                if (response!!.isSuccess) {
                    return response.value
                } else {
                    throw Exception(response.status.message)
                }
            } else {
                val message = apiResponse.message()
                Log.d("Success_Tag", "response Not Success:: $message")
                throw Exception(message)
            }
        } catch (e: Exception) {
            Log.e("Error_Tag", "response Error:${e.message}")
            throw Exception("${e.message}")
        }
    }


}
