package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class LeadsFilterOptionsRequest(
    @SerializedName("cart_type")       val cartType: Int,
    @SerializedName("startDate")        val startDate: String?,
    @SerializedName("endDate")        val endDate: String?,
    @SerializedName("sortType")        val sortType: Int
)