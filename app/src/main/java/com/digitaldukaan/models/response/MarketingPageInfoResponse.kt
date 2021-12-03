package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class MarketingPageInfoResponse(
    @SerializedName("store_share")              var marketingStoreShare: MarketingStoreShareResponse?,
    @SerializedName("store_info")               var marketingStoreInfo: MarketingStoreInfoResponse?,
    @SerializedName("static_text")              var marketingStaticTextResponse: MarketingStaticTextResponse?,
    @SerializedName("more_options_list")        var marketingMoreOptionsList: ArrayList<MarketingMoreOptionsItemResponse?>,
    @SerializedName("marketing_item_list")      var marketingItemList: ArrayList<MarketingCardsItemResponse?>,
    @SerializedName("is_share_store_locked")    var isShareStoreLocked: Boolean,
    @SerializedName("help_page")                var marketingHelpPage: HelpPageResponse?
)

data class MarketingStoreShareResponse(
    @SerializedName("heading")                  var heading: String?,
    @SerializedName("left_icon_cdn")            var leftIconCdn: String?,
    @SerializedName("right_icon_cdn")           var rightIconCdn: String?,
    @SerializedName("right_icon_text")          var rightIconText: String?,
    @SerializedName("domain_expiry_message")    var domainExpiryMessage: String?,
    @SerializedName("domain_expiry_cdn")        var domainExpiryCdn: String?,
    @SerializedName("heading_know_more")        var headingKnowMore: String?,
    @SerializedName("domain")                   var domain: String?,
    @SerializedName("domain_know_more")         var knowMore: MarketingDomainKnowMoreResponse?,
    @SerializedName("is_enabled")               var isEnabled: Boolean,
    @SerializedName("is_know_more_enable")      var isKnowMoreEnable: Boolean,
    @SerializedName("is_clickable")             var isClickable: Boolean
)

data class MarketingStaticTextResponse(
    @SerializedName("heading_marketing")                        var heading_marketing: String?,
    @SerializedName("heading_edit_and_share")                   var heading_edit_and_share: String?,
    @SerializedName("heading_product_discount")                 var heading_product_discount: String?,
    @SerializedName("heading_new_launches_and_bestsellers")     var heading_new_launches_and_bestsellers: String?,
    @SerializedName("text_edit_text")                           var text_edit_text: String?,
    @SerializedName("text_edit_background")                     var text_edit_background: String?,
    @SerializedName("text_background")                          var text_background: String?,
    @SerializedName("text_share")                               var text_share: String?,
    @SerializedName("text_change_product")                      var text_change_product: String?,
    @SerializedName("text_recently_added")                      var text_recently_added: String?,
    @SerializedName("text_save_changes")                        var text_save_changes: String?,
    @SerializedName("text_whatsapp")                            var text_whatsapp: String?,
    @SerializedName("hint_search_product")                      var hint_search_product: String?,
    @SerializedName("hint_text_line_1")                         var text_line_1: String?,
    @SerializedName("hint_text_line_2")                         var text_line_2: String?,
    @SerializedName("hint_text_line_3")                         var text_line_3: String?,
    @SerializedName("message_bestseller_zero_screen")           var message_bestseller_zero_screen: String?,
    @SerializedName("cta_text_add_products")                    var cta_text_add_products: String?,
    @SerializedName("message_product_discount_zero_screen")     var message_product_discount_zero_screen: String?,
    @SerializedName("text_order_at")                            var text_order_at: String?,
    @SerializedName("text_locked")                              var text_locked: String?,
    @SerializedName("message_please_note")                      var message_please_note: String?,
    @SerializedName("text_more_options")                        var text_more_options: String
)

data class MarketingStoreInfoResponse(
    @SerializedName("name")                         var name: String?,
    @SerializedName("is_store_item_limit_exceed")   var isStoreItemLimitExceeds: Boolean,
    @SerializedName("domain")                       var domain: String
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
