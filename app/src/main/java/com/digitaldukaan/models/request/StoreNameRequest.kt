package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class StoreNameRequest(
    @SerializedName("store_name") var storeName: String
)