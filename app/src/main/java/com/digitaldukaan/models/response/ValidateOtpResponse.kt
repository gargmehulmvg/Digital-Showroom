package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ValidateOtpResponse (
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("new_user") var mIsNewUser: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("user_id") var mUserId: String?,
    @SerializedName("phone") var mUserPhoneNumber: String?,
    @SerializedName("auth_token") var mUserAuthToken: String?,
    @SerializedName("store") var mStore: StoreResponse?,
    @SerializedName("auto_verify_time") var mAutoVerifyTime: Int?
)

data class StoreResponse (
    @SerializedName("store_id") var storeId: Int,
    @SerializedName("spotlight_flag") var spotlightFlag: Int,
    @SerializedName("score") var score: Int,
    @SerializedName("store_info") var storeInfo: UserStoreInfoResponse
)

data class UserStoreInfoResponse (
    @SerializedName("status") var mIsSuccessStatus: Boolean?,
    @SerializedName("name") var name: String?,
    @SerializedName("domain") var domain: String?,
    @SerializedName("store_url") var storeUrl: String?,
    @SerializedName("store_type") var storeType: String?,
    @SerializedName("logo_image") var logoImage: String?,
    @SerializedName("reference_store_id") var referenceStoreId: Int?,
    @SerializedName("description") var description: String?
)

data class ValidateOtpErrorResponse (
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("errorType") var mErrorType: String?,
    @SerializedName("message") var mMessage: String?
)