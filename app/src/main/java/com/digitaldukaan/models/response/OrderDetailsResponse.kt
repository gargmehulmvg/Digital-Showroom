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

data class OrderDetailMainResponse(
    @SerializedName("order") var orders: OrderDetailsResponse?,
    @SerializedName("static_text") var staticText: OrderDetailsStaticTextResponse?,
    @SerializedName("store_services") var storeServices: StoreServicesResponse?
)

data class OrderDetailsStaticTextResponse(
    @SerializedName("heading_add_delivery_and_other_charges") val heading_add_delivery_and_other_charges: String?,
    @SerializedName("heading_customer_details") val heading_customer_details: String?,
    @SerializedName("heading_free") val heading_free: String?,
    @SerializedName("heading_instructions") val heading_instructions: String?,
    @SerializedName("heading_send_bill_to_your_customer") val heading_send_bill_to_your_customer: String?,
    @SerializedName("text_customer_can_pay_via") val text_customer_can_pay_via: String?,
    @SerializedName("text_rupees_symbol") val text_rupees_symbol: String?,
    @SerializedName("text_refund_successful") val text_refund_successful: String?,
    @SerializedName("text_status") val text_status: String?,
    @SerializedName("text_quantity") val text_quantity: String?,
    @SerializedName("text_packet") val text_packet: String?,
    @SerializedName("text_order_id") val text_order_id: String?,
    @SerializedName("text_order") val text_order: String?,
    @SerializedName("text_new_order") val text_new_order: String?,
    @SerializedName("text_name") val text_name: String?,
    @SerializedName("text_id") val text_id: String?,
    @SerializedName("hint_other_charges") val hint_other_charges: String?,
    @SerializedName("hint_discount") val hint_discount: String?,
    @SerializedName("text_add_bank_account") val text_add_bank_account: String?,
    @SerializedName("text_address") val text_address: String?,
    @SerializedName("text_bank_account_not_found") val text_bank_account_not_found: String?,
    @SerializedName("text_bill_amount") val text_bill_amount: String?,
    @SerializedName("text_city_and_pincode") val text_city_and_pincode: String?,
    @SerializedName("text_delivery_charges") val text_delivery_charges: String?,
    @SerializedName("text_details") val text_details: String?,
    @SerializedName("text_estimate_delivery") val text_estimate_delivery: String?,
    @SerializedName("text_landmark") val text_landmark: String?,
    @SerializedName("message_customer_paid") val message_customer_paid: String?,
    @SerializedName("message_bill_sent_customer_not_paid") val message_bill_sent_customer_not_paid: String?,
    @SerializedName("text_processing_refund") val text_processing_refund: String?,
    @SerializedName("text_send_bill") val text_send_bill: String?
)