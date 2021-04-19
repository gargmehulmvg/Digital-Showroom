package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class CreateStoreRequest(
    @SerializedName("phone") var phone: String?,
    @SerializedName("user_id") var userId: Int,
    @SerializedName("store_name") var storeName: String?,
    @SerializedName("secret_key") var secretKey: String?
)