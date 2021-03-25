package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class UpdateOrderStatusRequest(
    @SerializedName("order_id") var orderId: Long?,
    @SerializedName("status") var status: Long?
)