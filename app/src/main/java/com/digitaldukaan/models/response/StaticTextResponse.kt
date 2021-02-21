package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class StaticTextResponse(
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("data") var mStaticData: StaticData
)

data class StaticData(
    @SerializedName("auth_new") var mAuthNew: AuthNewResponseData
)

data class AuthNewResponseData(
    @SerializedName("btn_txt") var mButtonText: String?,
    @SerializedName("input_txt") var mInputText: String?,
    @SerializedName("sub_heading") var mSubHeadingText: String?,
    @SerializedName("heading") var mHeadingText: String?
)