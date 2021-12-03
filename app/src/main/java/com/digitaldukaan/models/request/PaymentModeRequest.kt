package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class PaymentModeRequest(
    @SerializedName("status")           var status: Int?,
    @SerializedName("payment_type")     var paymentType: String?
)