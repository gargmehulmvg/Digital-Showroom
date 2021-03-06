package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName
import java.util.*

data class OrdersResponse(
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("next") var mIsNextDataAvailable: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("orders") var mOrdersList: ArrayList<OrderItemResponse>
)

class OrderItemResponse {
    @SerializedName("items") var items: String = ""
    @SerializedName("store_id") var storeId: Int = 0
    @SerializedName("order_id") var orderId: Int = 0
    @SerializedName("merchant_id") var merchantId: Int = 0
    @SerializedName("order_type") var orderType: Int = 0
    @SerializedName("amount") var amount: Int = 0
    @SerializedName("discount") var discount: Int = 0
    @SerializedName("pay_amount") var payAmount: Int = 0
    @SerializedName("status") var status: Int = 0
    @SerializedName("payment_status") var paymentStatus: Int = 0
    @SerializedName("display_status") var displayStatus: String = ""
    @SerializedName("status_message") var statusMessage: String = ""
    @SerializedName("order_hash") var orderHash: String = ""
    @SerializedName("transaction_id") var transactionId: String = ""
    @SerializedName("image_link") var imageLink: String = ""
    @SerializedName("phone") var phone: String = ""
    @SerializedName("updated_at") var updatedAt: String = ""
    @SerializedName("created_at") var createdAt: String = ""
    var updatedDate: Date? = Date()
    var viewType: Int = 0

    override fun equals(other: Any?): Boolean {
        val obj: OrderItemResponse? = other as OrderItemResponse
        if (obj?.updatedDate != updatedDate) return false
        return true
    }

    override fun hashCode(): Int {
        return 79 * this.orderId
    }

    override fun toString(): String {
        return "OrderItemResponse(items='$items', storeId=$storeId, orderId=$orderId, merchantId=$merchantId, orderType=$orderType, amount=$amount, discount=$discount, payAmount=$payAmount, status=$status, paymentStatus=$paymentStatus, displayStatus='$displayStatus', statusMessage='$statusMessage', orderHash='$orderHash', transactionId='$transactionId', imageLink='$imageLink', phone='$phone', updatedAt='$updatedAt', createdAt='$createdAt', updatedDate=$updatedDate, viewType=$viewType)"
    }


}