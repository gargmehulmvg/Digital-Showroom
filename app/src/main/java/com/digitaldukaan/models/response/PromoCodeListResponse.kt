package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName
import java.util.*

data class PromoCodeListResponse(
    @SerializedName("coupons")                  var mPromoCodeList: ArrayList<PromoCodeListItemResponse>?,
    @SerializedName("is_next_page")             var mIsNextPage: Boolean
)

data class PromoCodeListItemResponse(
    @SerializedName("status")                   var status: String?,
    @SerializedName("discount_type")            var discountType: String?,
    @SerializedName("promo_code")               var promoCode: String?,
    @SerializedName("discount")                 var discount: Double?,
    @SerializedName("max_discount")             var maxDiscount: Double?,
    @SerializedName("min_order_price")          var minOrderPrice: Double?,
    @SerializedName("once_per_user_flag")       var isOncePerUserFlag: Boolean,
    @SerializedName("is_website_visible")       var isWebsiteVisible: Boolean
)
