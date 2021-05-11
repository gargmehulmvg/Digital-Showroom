package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class StoreLogoRequest(
    @SerializedName("image_url") var storeLogoBase64Str: String?
)