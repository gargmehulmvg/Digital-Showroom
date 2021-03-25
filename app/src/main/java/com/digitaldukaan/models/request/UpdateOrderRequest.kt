package com.digitaldukaan.models.request

import com.digitaldukaan.models.response.OrderDetailItemResponse
import com.google.gson.annotations.SerializedName

data class UpdateOrderRequest(
    @SerializedName("order_id") val orderId: Int?,
    @SerializedName("amount") val amount: Double?,
    @SerializedName("cash_paid") val cash_paid: Boolean?,
    @SerializedName("delivery_to") val deliveryTo: String?,
    @SerializedName("delivery_from") val deliveryFrom: String?,
    @SerializedName("custom_delivery_time") val customDeliveryTime: String?,
    @SerializedName("items") val itemsList: ArrayList<OrderDetailItemResponse>?,
    @SerializedName("bill_url") val billUrl: String?
)