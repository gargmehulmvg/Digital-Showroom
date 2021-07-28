package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName
import java.util.*

data class PromoCodeListResponse(
    @SerializedName("coupons")                  var mPromoCodeList: ArrayList<PromoCodeListItemResponse>?,
    @SerializedName("is_next_page")             var mIsNextPage: Boolean
)

data class PromoCodeListItemResponse(
    @SerializedName("status")                   var status: String?,
    @SerializedName("discount_type")            var discount_type: String?,
    @SerializedName("promo_code")               var promo_code: String?,
    @SerializedName("discount")                 var discount: Double?,
    @SerializedName("max_discount")             var max_discount: Double?,
    @SerializedName("min_order_price")          var min_order_price: Double?,
    @SerializedName("once_per_user_flag")       var once_per_user_flag: Boolean
)
