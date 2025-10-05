package com.agb.iraq.data.remote.model

data class QuotationDetailResponse(
    val success: Boolean? = null,
    val data: QuotationData? = null,
    val message: String? = null,
    val code: Int? = null,
    val status: Int? = null
)

data class QuotationData(
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
    val payment: String? = null,
    val customer: Customer? = null,
    val items: List<QuotationItem>? = emptyList(),
    val warehouse: Warehouse? = null
)

data class Customer(
    val id: Int? = null,
    val customer_id: Int? = null,
    val name: String? = null,
    val email: String? = null,
    val tax_number: String? = null,
    val contact: String? = null,
    val avatar: String? = null,
    val created_by: Int? = null,
    val is_active: Int? = null,
    val email_verified_at: String? = null,
    val billing_name: String? = null,
    val billing_country: String? = null,
    val billing_state: String? = null,
    val billing_city: String? = null,
    val billing_phone: String? = null,
    val billing_zip: String? = null,
    val billing_address: String? = null,
    val shipping_name: String? = null,
    val shipping_country: String? = null,
    val shipping_state: String? = null,
    val shipping_city: String? = null,
    val shipping_phone: String? = null,
    val shipping_zip: String? = null,
    val shipping_address: String? = null,
    val lang: String? = null,
    val balance: String? = null,
    val credit_balance: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val city_id: Int? = null
)

data class QuotationItem(
    val id: Int? = null,
    val quotation_id: Int? = null,
    val product_id: Int? = null,
    val warehouse_id: Int? = null,
    val quantity: Int? = null,
    val tax: String? = null,
    val discount: Int? = null,
    val price: String? = null,
    val description: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val product: Product? = null,
    val customer: Customer? = null,
)

data class Product(
    val id: Int? = null,
    val name: String? = null,
    val sku: String? = null,
    val sale_price: String? = null,
    val purchase_price: String? = null,
    val quantity: Int? = null,
    val tax_id: String? = null,
    val category_id: Int? = null,
    val unit_id: Int? = null,
    val type: String? = null,
    val sale_chartaccount_id: Int? = null,
    val expense_chartaccount_id: Int? = null,
    val description: String? = null,
    val pro_image: String? = null,
    val created_by: Int? = null,
    val created_at: String? = null,
    val updated_at: String? = null,
    val minimum_price: String? = null
)

data class Warehouse(
    val id: Int? = null,
    val name: String? = null,
    val address: String? = null,
    val city: String? = null,
    val city_zip: String? = null,
    val created_by: Int? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)
