package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class PromoCodeDetailResponse(
    @SerializedName("coupon")               var mPromoCoupon: PromoCodeListItemResponse?,
    @SerializedName("analytics")            var mAnalytics: PromoCodeAnalyticsResponse?,
    @SerializedName("static_text")          var mStaticText: PromoCodeDetailStaticTextResponse?
)

data class PromoCodeAnalyticsResponse(
    @SerializedName("times_used")           var timesUsed: Int,
    @SerializedName("sales_generated")      var salesGenerated: Int
)

data class PromoCodeDetailStaticTextResponse(
    @SerializedName("message_allow_customer_see_coupon")    var message_allow_customer_see_coupon: String?,
    @SerializedName("text_active_coupon")                   var text_active_coupon: String?,
    @SerializedName("text_active_coupon_inactive")          var text_active_coupon_inactive: String?,
    @SerializedName("text_coupon_settings")                 var text_coupon_settings: String?,
    @SerializedName("text_flat_all_caps")                   var text_flat_all_caps: String?,
    @SerializedName("text_max_discount")                    var text_max_discount: String?,
    @SerializedName("text_min_order_amount")                var text_min_order_amount: String?,
    @SerializedName("text_off_all_caps")                    var text_off_all_caps: String?,
    @SerializedName("text_sales_generated")                 var text_sales_generated: String?,
    @SerializedName("text_show_this_coupon_my_website")     var text_show_this_coupon_my_website: String?,
    @SerializedName("text_times_uesed")                     var text_times_uesed: String?,
    @SerializedName("text_use_code")                        var text_use_code: String?
)