package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class UpdatePaymentMethodRequest(
    @SerializedName("flag") var flag: Int
)