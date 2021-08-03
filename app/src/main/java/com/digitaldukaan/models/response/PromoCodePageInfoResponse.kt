package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class PromoCodePageInfoResponse(
    @SerializedName("zero_coupon_screen")       var mZeroPromoCodePageResponse: ZeroPromoCodePageResponse?,
    @SerializedName("store_name")               var mStoreName: String?,
    @SerializedName("static_text")              var mStaticText: PromoCodePageStaticTextResponse?
)

data class ZeroPromoCodePageResponse(
    @SerializedName("cdn_link") var mCdnLink: String?,
    @SerializedName("is_active") var mIsActiveFlagEnable: Boolean?
)

data class PromoCodePageStaticTextResponse(
    @SerializedName("active_text")                              var active_text: String?,
    @SerializedName("inactive_text")                            var inactive_text: String?,
    @SerializedName("text_details")                             var text_details: String?,
    @SerializedName("text_share_coupon")                        var text_share_coupon: String?,
    @SerializedName("text_create_coupon")                       var text_create_coupon: String?,
    @SerializedName("heading_create_and_share")                 var heading_create_and_share: String?,
    @SerializedName("heading_custom_coupon")                    var heading_custom_coupon: String?,
    @SerializedName("heading_discount_coupon")                  var heading_discount_coupon: String?,
    @SerializedName("text_percent_discount")                    var text_percent_discount: String?,
    @SerializedName("text_flat_discount")                       var text_flat_discount: String?,
    @SerializedName("text_enter_percentage")                    var text_enter_percentage: String?,
    @SerializedName("text_coupon_code")                         var text_coupon_code: String?,
    @SerializedName("text_coupon_settings")                     var text_coupon_settings: String?,
    @SerializedName("text_enter_max_discount")                  var text_enter_max_discount: String?,
    @SerializedName("text_enter_min_discount")                  var text_enter_min_discount: String?,
    @SerializedName("message_select_this_coupon")               var message_select_this_coupon: String?,
    @SerializedName("text_upto_capital")                        var text_upto_capital: String?,
    @SerializedName("text_off_all_caps")                        var text_off_all_caps: String?,
    @SerializedName("heading_select_discount_type")             var heading_select_discount_type: String?,
    @SerializedName("heading_show_this_coupon_website")         var heading_show_this_coupon_website: String?,
    @SerializedName("message_allow_customer_see_coupon")        var message_allow_customer_see_coupon: String?,
    @SerializedName("text_discount")                            var text_discount: String?,
    @SerializedName("text_use_code")                            var text_use_code: String?,
    @SerializedName("text_flat")                                var text_flat: String?,
    @SerializedName("heading_please_confirm")                   var heading_please_confirm: String?,
    @SerializedName("text_min_order_amount")                    var text_min_order_amount: String?,
    @SerializedName("heading_applicable_once_per_customer")     var heading_applicable_once_per_customer: String?,
    @SerializedName("text_visible_on_website")                  var text_visible_on_website: String?,
    @SerializedName("text_hidden_from_website")                 var text_hidden_from_website: String?,
    @SerializedName("text_no_coupons_are_inactive")             var text_no_coupons_are_inactive: String?,
    @SerializedName("text_no_coupons_are_active")               var text_no_coupons_are_active: String?,
    @SerializedName("heading_bold_create_and_share")            var heading_bold_create_and_share: String?
)
