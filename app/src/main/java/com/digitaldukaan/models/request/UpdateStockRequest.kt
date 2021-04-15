package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class UpdateStockRequest(
    @SerializedName("store_item_id") var storeItemId: Int?,
    @SerializedName("available") var available: Int
)