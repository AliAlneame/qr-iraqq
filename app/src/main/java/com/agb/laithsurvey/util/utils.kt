package com.agb.laithsurvey.util

import com.agb.laithsurvey.data.remote.model.QuotationItem

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
