package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class StoreDeliveryStatusChangeRequest(
    @SerializedName("store_id") var storeId: Int,
    @SerializedName("store_flag") var storeFlag: Int,
    @SerializedName("delivery_flag") var deliveryFlag: Int,
    @SerializedName("pickup_flag") var pickupFlag: Int
)