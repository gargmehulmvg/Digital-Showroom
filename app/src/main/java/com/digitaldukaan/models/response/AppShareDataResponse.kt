package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class AppShareDataResponse(
    @SerializedName("status") var mStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("wa_text") var mWhatsAppText: String?,
    @SerializedName("image") var mImage: String?,
    @SerializedName("wa_story_text") var mWhatsAppStoryText: String?
)
