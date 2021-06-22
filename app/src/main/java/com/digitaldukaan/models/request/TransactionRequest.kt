package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class TransactionRequest(
    @SerializedName("page_no") var pageNo: Int?,
    @SerializedName("start_date") var startDate: String?,
    @SerializedName("end_date") var endDate: String?,
    @SerializedName("mode") var mode: String
)