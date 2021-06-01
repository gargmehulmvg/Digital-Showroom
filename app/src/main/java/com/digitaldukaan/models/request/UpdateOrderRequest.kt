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
    @SerializedName("bill_url") val billUrl: String?,
    @SerializedName("extra_charge_name") val extraChargeName: String?,
    @SerializedName("extra_charges") val extraCharges: Double?,
    @SerializedName("discount") val discount: Double?,
    @SerializedName("pay_amount") val payAmount: Double?,
    @SerializedName("delivery_charge") val deliveryCharge: Double?
)