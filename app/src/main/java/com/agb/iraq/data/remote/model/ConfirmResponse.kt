package com.agb.iraq.data.remote.model

data class ConfirmResponse(
    val success: Boolean? = null,
    val data: ConfirmData? = null,
    val message: String? = null,
    val code: Int? = null,
    val status: Int? = null
)

data class ConfirmData(
    val id: Int? = null,
    val quotation_id: Int? = null,
    val customer_id: Int? = null,
    val warehouse_id: Int? = null,
    val quotation_date: String? = null,
    val category_id: Int? = null,
    val status: Int? = null,
    val converted_pos_id: Int? = null,
    val is_converted: Int? = null,
    val created_by: Int? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val type: Int? = null,
    val parent_id: Int? = null,
    val invoice_id: Int? = null,
    val return_invoice_id: Int? = null,
    val invoice: ConfirmInvoice? = null,
    val items: List<ConfirmItem>? = emptyList()
)

data class ConfirmInvoice(
    val id: Int? = null,
    val invoice_id: Int? = null,
    val customer_id: Int? = null,
    val issue_date: String? = null,
    val due_date: String? = null,
    val send_date: String? = null,
    val category_id: Int? = null,
    val ref_number: String? = null,
    val status: Int? = null,
    val shipping_display: Int? = null,
    val discount_apply: Int? = null,
    val created_by: Int? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val type: Int? = null,
    val parent_id: Int? = null
)

data class ConfirmItem(
    val id: Int? = null,
    val quotation_id: Int? = null,
    val product_id: Int? = null,
    val quantity: Int? = null,
    val tax: String? = null,
    val discount: Int? = null,
    val price: String? = null,
    val description: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)
