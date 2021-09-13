package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class MarketingItemCtaResponse(
    @SerializedName("text")             var text: String?,
    @SerializedName("cdn")              var cdn: String?,
    @SerializedName("action")           var action: String?,
    @SerializedName("url")              var url: String?,
    @SerializedName("bg_color")         var bgColor: String?,
    @SerializedName("text_color")       var textColor: String?
)
