package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class StoreLinkRequest(
    @SerializedName("store_id") var storeId: Int,
    @SerializedName("domain") var domain: String
)