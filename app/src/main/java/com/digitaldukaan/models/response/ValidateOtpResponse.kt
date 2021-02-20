package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ValidateOtpResponse (
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("new_user") var mIsNewUser: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("user_id") var mUserId: String?,
    @SerializedName("phone") var mUserPhoneNumber: String?,
    @SerializedName("auth_token") var mUserAuthToken: String?,
    @SerializedName("store") var mStore: String?,
    @SerializedName("auto_verify_time") var mAutoVerifyTime: Int?
)

data class ValidateOtpErrorResponse (
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("errorType") var mErrorType: String?,
    @SerializedName("message") var mMessage: String?
)