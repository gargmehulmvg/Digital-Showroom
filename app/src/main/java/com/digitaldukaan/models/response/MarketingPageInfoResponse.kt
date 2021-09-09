package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class MarketingPageInfoResponse(
    @SerializedName("store_share")              var marketingStoreShare: MarketingStoreShareResponse?,
    @SerializedName("static_text")              var marketingStaticTextResponse: MarketingStaticTextResponse?,
    @SerializedName("more_options_list")        var marketingMoreOptionsList: ArrayList<MarketingMoreOptionsItemResponse?>,
    @SerializedName("marketing_item_list")      var marketingItemList: ArrayList<MarketingCardsItemResponse?>,
    @SerializedName("help_page")                var marketingHelpPage: HelpPageResponse?
)

data class MarketingStoreShareResponse(
    @SerializedName("heading")                  var heading: String?,
    @SerializedName("left_icon_cdn")            var left_icon_cdn: String?,
    @SerializedName("right_icon_cdn")            var right_icon_cdn: String?,
    @SerializedName("right_icon_text")          var right_icon_text: String?,
    @SerializedName("domain_expiry_message")    var domain_expiry_message: String?,
    @SerializedName("domain_expiry_cdn")        var domain_expiry_cdn: String?,
    @SerializedName("heading_know_more")        var heading_know_more: String?,
    @SerializedName("domain")                   var domain: String?,
    @SerializedName("is_enabled")               var isEnabled: Boolean,
    @SerializedName("is_clickable")             var isClickable: Boolean
)

data class MarketingStaticTextResponse(
    @SerializedName("heading_marketing")        var heading_marketing: String?,
    @SerializedName("text_more_options")        var text_more_options: String
)

data class MarketingMoreOptionsItemResponse(
    @SerializedName("info_text")                var info_text: String?,
    @SerializedName("heading")                  var heading: String,
    @SerializedName("right_cdn")                var right_cdn: String,
    @SerializedName("is_clickable")             var is_clickable: Boolean,
    @SerializedName("is_enabled")               var is_enabled: Boolean,
    @SerializedName("is_expandable")            var is_expandable: Boolean,
    @SerializedName("action")                   var action: String?,
    @SerializedName("expandable_data_heading")  var expandable_data_heading: String?,
    @SerializedName("left_cdn")                 var left_cdn: String
)
