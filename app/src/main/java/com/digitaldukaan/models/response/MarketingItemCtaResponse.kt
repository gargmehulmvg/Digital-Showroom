package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class MarketingItemCtaResponse(
    @SerializedName("text")             var text: String?,
    @SerializedName("cdn")              var cdn: String?,
    @SerializedName("action")           var action: String?,
    @SerializedName("bg_color")         var bg_color: String?,
    @SerializedName("text_color")       var text_color: String?
)
