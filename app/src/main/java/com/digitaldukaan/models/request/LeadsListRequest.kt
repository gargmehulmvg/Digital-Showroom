package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class LeadsListRequest(
    @SerializedName("start_date")       val startDate: String?,
    @SerializedName("end_date")         val endDate: String?,
    @SerializedName("user_name")        val userName: String?,
    @SerializedName("user_phone")       val userPhone: String?,
    @SerializedName("sort_type")        val sortType: Int,
    @SerializedName("cart_type")        val cartType: Int
)