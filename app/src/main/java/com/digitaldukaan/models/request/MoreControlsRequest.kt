package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class MoreControlsRequest(
    @SerializedName("store_id") var storeId: Int,
    @SerializedName("delivery_charge_type") var deliveryChargeType: Int,
    @SerializedName("free_delivery_above") var freeDeliveryAbove: Double,
    @SerializedName("delivery_price") var deliveryPrice: Double,
    @SerializedName("min_order_value") var minOrderValue: Double
)