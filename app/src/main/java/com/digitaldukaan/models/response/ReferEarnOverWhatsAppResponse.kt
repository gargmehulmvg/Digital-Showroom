package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ReferEarnOverWhatsAppResponse(
    @SerializedName("status")               var mStatus: Boolean,
    @SerializedName("message")              var mMessage: String?,
    @SerializedName("data")                 var mReferAndEarnData: ReferAndEarnOverWhatsAppItemResponse
)

data class ReferAndEarnOverWhatsAppItemResponse(
    @SerializedName("wa_text")              var whatsAppText: String?,
    @SerializedName("pending_text")         var pendingText: String?,
    @SerializedName("image_url")            var imageUrl: String?,
    @SerializedName("share_store_banner")   var isShareStoreBanner: Boolean?
)
