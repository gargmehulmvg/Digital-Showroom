package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class UpdatePromoCodeRequest(
    @SerializedName("promo_code")   var promoCode: String,
    @SerializedName("status")       var status: String
)
