package com.agb.iraq.data.remote.api

import com.agb.iraq.data.remote.model.ConfirmResponse
import com.agb.iraq.data.remote.model.PagingResponse
import com.agb.iraq.data.remote.model.ProductResponse
import com.agb.iraq.data.remote.model.QuotationDetailResponse
import com.agb.iraq.data.remote.model.UpdateResponse
import retrofit2.http.Field
import retrofit2.http.FieldMap
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface QuotationApi {
    @GET("quotations")
    suspend fun getQuotations(
        @Query("page") page: Int
    ): PagingResponse

    @GET("purchases")
    suspend fun getPurchases(
        @Query("page") page: Int
    ): PagingResponse


    @GET("quotations/{id}")
    suspend fun getQuotationById(
        @Path("id") id: Int
    ): QuotationDetailResponse

    @GET("purchases/{id}")
    suspend fun getPurchasesById(
        @Path("id") id: Int
    ): QuotationDetailResponse

    @FormUrlEncoded
    @POST("quotations/{id}/confirm")
    suspend fun confirmQuotation(
        @Path("id") quotationId: Int,
        @FieldMap fields: Map<String, String>
    ): ConfirmResponse

    @FormUrlEncoded
    @POST("purchases/{id}/confirm")
    suspend fun confirmPurchases(
        @Path("id") quotationId: Int,
        @FieldMap fields: Map<String, String>
    ): ConfirmResponse

    @FormUrlEncoded
    @POST("quotations/{id}/update")
    suspend fun updateQuotation(
        @Path("id") quotationId: Int,
        @Field("customer_id") customerId: Int,
        @Field("warehouse_id") warehouseId: Int,
        @Field("quotation_date") quotationDate: String,
        @FieldMap items: Map<String, String>
    ): ConfirmResponse

    @GET("products")
    suspend fun getProducts(
        @Query("sku") sku: String,
        @Query("warehouse_id") warehouseId: Int
    ): ProductResponse

}
