package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class StoreDescriptionResponse(
    @SerializedName("status") var mStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("store") var mStoreInfo: StoreResponse?
)