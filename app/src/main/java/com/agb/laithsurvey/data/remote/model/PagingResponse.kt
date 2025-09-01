package com.agb.laithsurvey.data.remote.model

import com.google.gson.annotations.SerializedName

data class PagingResponse(
    val success: Boolean,
    val data: PaginatedData
)

data class PaginatedData(
    @SerializedName("current_page") val currentPage: Int,
    val data: List<QuotationItem>,
    @SerializedName("last_page") val lastPage: Int
)

data class BaseResponse<T>(
    @SerializedName("value")
    val value: T,
    @SerializedName("isSuccess")
    val isSuccess: Boolean,
    @SerializedName("status")
    val status: Status,
)

data class Status(
    @SerializedName("message")
    val message: String,
    @SerializedName("code")
    val code: Int?
)