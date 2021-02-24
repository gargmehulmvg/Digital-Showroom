package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class StoreDescriptionRequest(
    @SerializedName("store_id") var storeId: Int,
    @SerializedName("description") var description: String
)