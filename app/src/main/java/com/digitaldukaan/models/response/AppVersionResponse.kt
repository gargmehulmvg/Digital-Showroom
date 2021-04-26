package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class AppVersionResponse(
    @SerializedName("is_active") var mIsActive: Boolean
)
