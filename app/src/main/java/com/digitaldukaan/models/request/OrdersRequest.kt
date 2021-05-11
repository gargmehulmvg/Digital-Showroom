package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class OrdersRequest(
    @SerializedName("mode") var orderMode: String,
    @SerializedName("page") var pageNumber: Int
)