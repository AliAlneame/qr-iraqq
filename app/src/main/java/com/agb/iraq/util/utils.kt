package com.agb.iraq.util

import com.agb.iraq.data.remote.model.QuotationItem

fun buildConfirmFields(items: List<QuotationItem>): Map<String, String> {
    val fields = mutableMapOf<String, String>()
    items.forEachIndexed { index, item ->
        val productId = item.product_id ?: throw IllegalArgumentException("Missing product_id for index=$index")
        val qty = (item.quantity ?: 1).coerceAtLeast(1)

        // إن كان الباك إند يحتاج سطر id عند التأكيد، احتفظ به:
        item.id?.let { fields["items[$index][id]"] = it.toString() }
        fields["items[$index][item]"] = productId.toString()
        fields["items[$index][price]"] = item.price ?: "0"
        fields["items[$index][quantity]"] = qty.toString()
        fields["items[$index][tax]"] = item.tax ?: "0"
        fields["items[$index][discount]"] = (item.discount ?: 0).toString()
        fields["items[$index][description]"] = item.description ?: ""
    }
    return fields
}

fun buildQuotationFields(items: List<QuotationItem>): Map<String, String> {
    val fields = mutableMapOf<String, String>()
    items.forEachIndexed { index, item ->
        val productId = item.product_id ?: throw IllegalArgumentException("Missing product_id for index=$index")
        val qty = (item.quantity ?: 1).coerceAtLeast(1)

        // لو كان API التحديث يحتاج أيضاً تمرير id للسطر حتى يحدّثه بدل إنشاء سطر جديد:
        item.id?.let { fields["items[$index][id]"] = it.toString() }

        fields["items[$index][item]"] = productId.toString()
        fields["items[$index][price]"] = item.price ?: "0"
        fields["items[$index][quantity]"] = qty.toString()
        fields["items[$index][tax]"] = item.tax ?: "0"
        fields["items[$index][discount]"] = (item.discount ?: 0).toString()
        fields["items[$index][description]"] = item.description ?: ""
    }
    return fields
}
