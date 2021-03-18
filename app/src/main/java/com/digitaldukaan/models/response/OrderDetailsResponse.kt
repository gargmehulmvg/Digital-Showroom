package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class OrderDetailsResponse (
    @SerializedName("store_id") var storeId: Int?,
    @SerializedName("order_id") var orderId: Int?,
    @SerializedName("merchant_id") var merchantId: Int?,
    @SerializedName("payment_status") var paymentStatus: Int?,
    @SerializedName("status") var status: Int?,
    @SerializedName("order_type") var orderType: Int?,
    @SerializedName("amount") var amount: Double?,
    @SerializedName("discount") var discount: Double?,
    @SerializedName("pay_amount") var payAmount: Double?,
    @SerializedName("instruction") var instruction: String?,
    @SerializedName("phone") var phone: String?,
    @SerializedName("display_status") var displayStatus: String?,
    @SerializedName("status_message") var statusMessage: String?,
    @SerializedName("order_hash") var orderHash: String?,
    @SerializedName("transaction_id") var transactionId: String?,
    @SerializedName("image_link") var imageLink: String?,
    @SerializedName("created_at") var createdAt: String?,
    @SerializedName("updated_at") var updatedAt: String?,
    @SerializedName("digital_receipt") var digitalReceipt: String?,
    @SerializedName("items") var orderDetailsItemsList: ArrayList<OrderDetailItemResponse>?,
    @SerializedName("delivery_info") var deliveryInfo: DeliveryInfoItemResponse?
)

data class OrderDetailItemResponse(
    @SerializedName("order_item_id") var order_item_id: Int?,
    @SerializedName("item_id") var item_id: Int?,
    @SerializedName("item_name") var item_name: String?,
    @SerializedName("item_quantity") var item_quantity: Int?,
    @SerializedName("item_price") var item_price: Int?,
    @SerializedName("item_status") var item_status: Int?,
    @SerializedName("item_type") var item_type: String?,
    @SerializedName("creator_type") var creator_type: Int?
)