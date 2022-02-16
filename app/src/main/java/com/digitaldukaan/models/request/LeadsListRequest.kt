package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class LeadsListRequest(
    @SerializedName("start_date")       var startDate: String?,
    @SerializedName("end_date")         var endDate: String?,
    @SerializedName("user_name")        var userName: String?,
    @SerializedName("user_phone")       var userPhone: String?,
    @SerializedName("sort_type")        var sortType: Int,
    @SerializedName("cart_type")        var cartType: Int
)