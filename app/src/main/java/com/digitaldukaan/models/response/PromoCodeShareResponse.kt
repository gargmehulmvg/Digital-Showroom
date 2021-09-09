package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class PromoCodeShareResponse(
    @SerializedName("coupon_text")      var mCouponText: String?,
    @SerializedName("coupon_cdn")       var mCouponCdn: String?
)