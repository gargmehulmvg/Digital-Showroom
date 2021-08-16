package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class CreateCouponsRequest(
    @SerializedName("discount_type")    var discountType: String,
    @SerializedName("promo_code")       var promoCode: String,
    @SerializedName("discount")         var discount: Double,
    @SerializedName("max_discount")     var maxDiscount: Double,
    @SerializedName("min_order_price")  var minOrderPrice: Double,
    @SerializedName("is_hidden")        var isHidden: Boolean,
    @SerializedName("is_one_time")      var isOneTime: Boolean
)
