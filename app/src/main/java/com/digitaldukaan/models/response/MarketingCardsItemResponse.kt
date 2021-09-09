package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class MarketingCardsItemResponse(
    @SerializedName("heading")              var heading: String?,
    @SerializedName("sub_heading")          var subHeading: String?,
    @SerializedName("action")               var action: String?,
    @SerializedName("logo")                 var logo: String?,
    @SerializedName("bg_color")             var bgColor: String?,
    @SerializedName("heading_color")        var headingColor: String?,
    @SerializedName("sub_heading_color")    var subHeadingColor: String?,
    @SerializedName("background")           var background: String?,
    @SerializedName("type")                 var type: String?,
    @SerializedName("info_text")            var infoText: String?,
    @SerializedName("info_text_color")      var infoTextColor: String?,
    @SerializedName("info_text_bg_color")   var infoTextBgColor: String?,
    @SerializedName("is_enabled")           var isEnabled: Boolean?,
    @SerializedName("is_shimmer")           var isShimmer: Boolean?,
    @SerializedName("is_clickable")         var isClickable: Boolean?,
    @SerializedName("cta")                  var marketingCta: MarketingItemCtaResponse?,
    @SerializedName("url")                  var url: String?
)
