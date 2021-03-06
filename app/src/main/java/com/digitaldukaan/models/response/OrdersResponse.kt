package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class OrdersResponse(
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("next") var mIsNextDataAvailable: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("orders") var mOrdersList: ArrayList<OrderItemResponse>
)

data class OrderItemResponse(
    @SerializedName("items") var items: String,
    @SerializedName("store_id") var storeId: Int,
    @SerializedName("order_id") var orderId: Int,
    @SerializedName("merchant_id") var merchantId: Int,
    @SerializedName("order_type") var orderType: Int,
    @SerializedName("amount") var amount: Int,
    @SerializedName("discount") var discount: Int,
    @SerializedName("pay_amount") var payAmount: Int,
    @SerializedName("status") var status: Int,
    @SerializedName("payment_status") var paymentStatus: Int,
    @SerializedName("display_status") var displayStatus: String,
    @SerializedName("status_message") var statusMessage: String,
    @SerializedName("order_hash") var orderHash: String,
    @SerializedName("transaction_id") var transactionId: String,
    @SerializedName("image_link") var imageLink: String,
    @SerializedName("phone") var phone: String,
    @SerializedName("updated_at") var updatedAt: String,
    @SerializedName("created_at") var createdAt: String
)