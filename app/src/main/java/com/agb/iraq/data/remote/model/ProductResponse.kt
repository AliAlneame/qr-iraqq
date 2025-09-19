package com.agb.iraq.data.remote.model

data class ProductResponse(
    val success: Boolean? = null,
    val data: List<ProductData>? = null,
    val message: String? = null
)

data class ProductData(
    val id: Int? = null,
    val name: String? = null,
    val sku: String? = null,
    val sale_price: String? = null,
    val purchase_price: String? = null,
    val warehouses: List<ProductWarehouse>? = null
)

data class ProductWarehouse(
    val warehouse_id: Int? = null,
    val warehouse_name: String? = null,
    val quantity: Int? = null
)

