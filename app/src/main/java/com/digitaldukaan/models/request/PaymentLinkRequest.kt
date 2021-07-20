package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class PaymentLinkRequest(
    @SerializedName("mode")         var mode: String,
    @SerializedName("pay_amount")   var amount: Double,
    @SerializedName("phone")        var phone: String,
    @SerializedName("image_link")   var imageLink: String
)