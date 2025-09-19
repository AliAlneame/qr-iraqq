package com.agb.iraq.util

import com.agb.iraq.data.remote.model.QuotationItem

fun buildConfirmFields(items: List<QuotationItem>): Map<String, String> {
    val fields = mutableMapOf<String, String>()
    items.forEachIndexed { index, item ->
        fields["items[$index][id]"] = item.id.toString()
        fields["items[$index][item]"] = item.product_id.toString()
        fields["items[$index][price]"] = item.price ?: "0"
        fields["items[$index][quantity]"] = item.quantity.toString()
        fields["items[$index][tax]"] = item.tax ?: "0"
        fields["items[$index][discount]"] = item.discount.toString()
        fields["items[$index][description]"] = item.description ?: "null"
    }
    return fields
}

fun buildQuotationFields(items: List<QuotationItem>): Map<String, String> {
    val fields = mutableMapOf<String, String>()

    items.forEachIndexed { index, item ->
        item.id?.let { fields["items[$index][id]"] = it.toString() }
        item.product_id?.let { fields["items[$index][item]"] = it.toString() }
        fields["items[$index][price]"] = item.price ?: "0"
        fields["items[$index][quantity]"] = item.quantity?.toString() ?: "0"
        fields["items[$index][tax]"] = item.tax ?: "0"
        fields["items[$index][discount]"] = item.discount?.toString() ?: "0"
        fields["items[$index][description]"] = item.description ?: ""
    }

    return fields
}

