package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class LandingPageCardsResponse(
    @SerializedName("zero_order_items")         var zeroOrderItemsList: ArrayList<ZeroOrderItemsResponse?>?,
    @SerializedName("domain_expiry_message")    var domainExpiryMessage: String,
    @SerializedName("is_share_store_locked")    var isShareStoreLocked: Boolean,
    @SerializedName("shortcuts_list")           var shortcutsList: ArrayList<CommonCtaResponse?>?
)

data class ZeroOrderItemsResponse(
    @SerializedName("id")                       var id: String,
    @SerializedName("heading")                  var heading: String,
    @SerializedName("heading_text_color")       var headingTextColor: String,
    @SerializedName("sub_heading")              var subHeading: String,
    @SerializedName("sub_heading_text_color")   var subHeadingTextColor: String,
    @SerializedName("message")                  var message: String,
    @SerializedName("message_remaining_android")var messageRemainingAndroid: String,
    @SerializedName("message_text_color")       var messageTextColor: String,
    @SerializedName("message_text_font")        var messageTextFont: String,
    @SerializedName("message_text_cdn")         var messageTextCdn: String,
    @SerializedName("hero_image")               var heroImage: String,
    @SerializedName("is_primary")               var isPrimary: Boolean,
    @SerializedName("bg_color")                 var bgColor: String,
    @SerializedName("bg_image")                 var bgImage: String,
    @SerializedName("message_view_type")        var messageViewType: String,
    @SerializedName("completed")                var completed: LandingPageCardCompletedItemResponse?,
    @SerializedName("cta")                      var ctaResponse: CommonCtaResponse?,
    @SerializedName("message_list_data")        var messageListData: ArrayList<LandingPageMessageListItemResponse>?,
    var suggestedDomainsList: ArrayList<PrimaryDomainItemResponse>?
)

data class LandingPageCardCompletedItemResponse(
    @SerializedName("is_completed")             var isCompleted: Boolean,
    @SerializedName("heading")                  var heading: String,
    @SerializedName("sub_heading")              var subHeading: String,
    @SerializedName("cdn")                      var cdn: String
)

data class LandingPageMessageListItemResponse(
    @SerializedName("is_product_added")         var isProductAdded: Boolean,
    @SerializedName("image_url")                var imageUrl: String
)

data class LandingPageShortcutItemResponse(
    @SerializedName("text")                     var text: String,
    @SerializedName("action")                   var action: String,
    @SerializedName("image_url")                var imageUrl: String
)