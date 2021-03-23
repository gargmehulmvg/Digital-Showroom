package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class CompleteOrderRequest(
    @SerializedName("id") var orderId: Long?
)