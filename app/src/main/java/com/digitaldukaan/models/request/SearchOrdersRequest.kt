package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class SearchOrdersRequest(
    @SerializedName("order_id") var orderId: Int,
    @SerializedName("phone") var phone: String,
    @SerializedName("page_no") var pageNumber: Int
)