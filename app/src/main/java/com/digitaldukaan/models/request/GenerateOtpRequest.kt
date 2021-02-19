package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class GenerateOtpRequest(
    @SerializedName("app_name") var mAppName: String,
    @SerializedName("phone") var mMobileNumber: String
)