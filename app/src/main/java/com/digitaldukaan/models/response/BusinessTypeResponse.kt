package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class BusinessTypeResponse(
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("businesses") var mBusinessList: ArrayList<BusinessTypeItemResponse>
)

data class BusinessTypeItemResponse(
    @SerializedName("business_id") var businessId: Int,
    @SerializedName("business_name") var businessName: String,
    @SerializedName("image") var businessImage: String,
    var isBusinessTypeSelected: Boolean
)