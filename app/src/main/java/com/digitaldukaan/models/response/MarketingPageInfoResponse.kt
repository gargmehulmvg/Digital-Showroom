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
    @SerializedName("domain_know_more")         var knowMore: MarketingDomainKnowMoreResponse?,
    @SerializedName("is_enabled")               var isEnabled: Boolean,
    @SerializedName("is_clickable")             var isClickable: Boolean
)

data class MarketingStaticTextResponse(
    @SerializedName("heading_marketing")        var heading_marketing: String?,
    @SerializedName("text_more_options")        var text_more_options: String
)

data class MarketingDomainKnowMoreResponse(
    @SerializedName("heading_your_domain")      var headingYourDomain: String?,
    @SerializedName("message_get_best_domain")  var messageGetBestDomain: String?,
    @SerializedName("domain_expiry_message")    var domainExpiryMessage: String?,
    @SerializedName("suggested_domains")        var suggestedDomainsList: ArrayList<MarketingSuggestedDomainItemResponse?>?,
    @SerializedName("domain_name")              var domainName: String
)

data class MarketingMoreOptionsItemResponse(
    @SerializedName("info_text")                var infoText: String?,
    @SerializedName("heading")                  var heading: String,
    @SerializedName("right_cdn")                var rightCdn: String,
    @SerializedName("is_clickable")             var isClickable: Boolean,
    @SerializedName("is_enabled")               var isEnabled: Boolean,
    @SerializedName("action")                   var action: String?,
    @SerializedName("expandable_data_heading")  var expandableDataHeading: String?,
    @SerializedName("expandable_data")          var expandableData: ArrayList<MarketingExpandableItemResponse?>?,
    @SerializedName("left_cdn")                 var leftCdn: String
)

data class MarketingExpandableItemResponse(
    @SerializedName("heading")                  var heading: String,
    @SerializedName("logo")                     var logo: String,
    @SerializedName("action")                   var action: String?,
    @SerializedName("color")                    var color: String?,
    @SerializedName("info_text")                var infoText: String?,
    @SerializedName("info_text_color")          var infoTextColor: String?,
    @SerializedName("info_text_bg_color")       var infoTextBgColor: String?,
    @SerializedName("is_clickable")             var isClickable: Boolean,
    @SerializedName("is_enabled")               var isEnabled: Boolean,
    @SerializedName("event_name")               var eventName: String?,
    @SerializedName("event_parameter")          var eventParameter: Map<String, String>,
    @SerializedName("url")                      var url: String?,
    @SerializedName("cta_cdn")                  var ctaCdn: String
)

data class MarketingSuggestedDomainItemResponse(
    @SerializedName("offer_message")            var offerMessage: String,
    @SerializedName("offer_text_color")         var offerTextColor: String,
    @SerializedName("offer_bg_color")           var offerBgColor: String,
    @SerializedName("is_free")                  var isFree: Boolean,
    @SerializedName("name")                     var name: String,
    @SerializedName("cta")                      var cta: MarketingItemCtaResponse,
    @SerializedName("promo_code")               var promoCode: String,
    @SerializedName("original_price")           var originalPrice: String,
    @SerializedName("discounted_price")         var discountedPrice: String,
    @SerializedName("message_domain_offer")     var messageDomainOffer: String,
    @SerializedName("cta_cdn")                  var ctaCdn: String
)
