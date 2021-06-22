package com.digitaldukaan.models.dto

import com.digitaldukaan.models.response.PaymentModesItemResponse
import com.google.gson.annotations.SerializedName

class PaymentModelDTO(
    @SerializedName("key") var name: String?,
    @SerializedName("value") var value: ArrayList<PaymentModesItemResponse>
)