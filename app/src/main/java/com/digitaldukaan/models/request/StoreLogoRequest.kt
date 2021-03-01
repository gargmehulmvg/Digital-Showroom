package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class StoreLogoRequest(
    @SerializedName("store_id") var storeId: Int,
    @SerializedName("store_logo") var storeLogoBase64Str: String?
)