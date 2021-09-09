package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class GetPromoCodeRequest(
    @SerializedName("mode") var mode: String,
    @SerializedName("page") var page: Int
)
