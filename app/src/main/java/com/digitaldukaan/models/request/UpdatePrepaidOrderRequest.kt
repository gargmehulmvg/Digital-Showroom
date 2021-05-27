package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class UpdatePrepaidOrderRequest(
    @SerializedName("delivery_to") val deliveryTo: String?,
    @SerializedName("delivery_from") val deliveryFrom: String?,
    @SerializedName("custom_delivery_time") val customDeliveryTime: String?
)