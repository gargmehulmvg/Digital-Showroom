package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class UpdateOrderResponse(
    @SerializedName("order_id") var orderId: Int?,
    @SerializedName("whatsapp_text") var whatsAppText: String?
)