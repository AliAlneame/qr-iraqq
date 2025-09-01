package com.agb.laithsurvey.data.remote.api

import com.agb.laithsurvey.data.remote.model.ConfirmResponse
import com.agb.laithsurvey.data.remote.model.PagingResponse
import com.agb.laithsurvey.data.remote.model.QuotationDetailResponse
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

}