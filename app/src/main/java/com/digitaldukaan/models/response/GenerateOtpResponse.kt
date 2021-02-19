package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class GenerateOtpResponse(
    @SerializedName("status") var mStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("error_type") var mErrorType: String?
)