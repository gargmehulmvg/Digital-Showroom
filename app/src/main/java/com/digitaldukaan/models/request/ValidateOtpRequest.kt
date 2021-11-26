package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class ValidateOtpRequest(
    @SerializedName("otp")          var mOTP: Int,
    @SerializedName("ct_id")        var mCleverTapId: String?,
    @SerializedName("fcm_token")    var mFirebaseTokenId: String?,
    @SerializedName("phone")        var mMobileNumber: String
)