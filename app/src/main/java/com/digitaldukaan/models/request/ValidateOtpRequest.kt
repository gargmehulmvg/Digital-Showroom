package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class ValidateOtpRequest(
    @SerializedName("otp") var mOTP: Int,
    @SerializedName("phone") var mMobileNumber: String
)