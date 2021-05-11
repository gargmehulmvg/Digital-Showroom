package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class CommonApiResponse(
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("error_type") var mErrorType: String?,
    @SerializedName("data") var mCommonDataStr: String
)