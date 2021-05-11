package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class MoreControlsResponse(
    @SerializedName("status") var mStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("services") var mServices: StoreServicesResponse
)