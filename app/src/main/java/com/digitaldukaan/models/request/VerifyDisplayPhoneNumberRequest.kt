package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class VerifyDisplayPhoneNumberRequest(
    @SerializedName("phone")    var phone: String,
    @SerializedName("otp")      var otp: Int
)