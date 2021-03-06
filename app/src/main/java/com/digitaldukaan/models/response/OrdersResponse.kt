package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName
import java.util.*
import kotlin.collections.ArrayList

data class OrdersResponse(
    @SerializedName("next")             var mIsNextDataAvailable: Boolean = false,
    @SerializedName("orders")           var mOrdersList: ArrayList<OrderItemResponse>
)

class OrderItemResponse {
    @SerializedName("items")            var items: ArrayList<Any> = ArrayList()
    @SerializedName("store_id")         var storeId: Int = 0
    @SerializedName("order_id")         var orderId: Int = 0
    @SerializedName("merchant_id")      var merchantId: Int = 0
    @SerializedName("order_type")       var orderType: Int = 0
    @SerializedName("amount")           var amount: Double = 0.0
    @SerializedName("discount")         var discount: Double = 0.0
    @SerializedName("pay_amount")       var payAmount: Double = 0.0
    @SerializedName("status")           var status: Int = 0
    @SerializedName("payment_status")   var paymentStatus: Int = 0
    @SerializedName("display_status")   var displayStatus: String = ""
    @SerializedName("status_message")   var statusMessage: String = ""
    @SerializedName("order_hash")       var orderHash: String = ""
    @SerializedName("transaction_id")   var transactionId: String = ""
    @SerializedName("image_link")       var imageLink: String = ""
    @SerializedName("phone")            var phone: String = ""
    @SerializedName("delivery_info")    var deliveryInfo: DeliveryInfoItemResponse? = null
    @SerializedName("updated_at")       var updatedAt: String = ""
    @SerializedName("created_at")       var createdAt: String = ""
    @SerializedName("prepaid_flag")     var prepaidFlag: Int = 0
    var updatedDate: Date? = Date()
    var updatedCompleteDate: Date? = Date()
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

data class DeliveryInfoItemResponse(
    @SerializedName("fulfillment_id")       var fulfillmentId: Int,
    @SerializedName("latitude")             var latitude: Double,
    @SerializedName("longitude")            var longitude: Double,
    @SerializedName("assigned_to")          var assignedTo: Double,
    @SerializedName("address1")             var address1: String,
    @SerializedName("address2")             var address2: String,
    @SerializedName("type")                 var type: Int,
    @SerializedName("city")                 var city: String?,
    @SerializedName("landmark")             var landmark: String?,
    @SerializedName("pincode")              var pincode: String?,
    @SerializedName("deliver_to")           var deliverTo: String?,
    @SerializedName("delivery_to")          var deliveryTo: String?,
    @SerializedName("delivery_from")        var deliveryFrom: String?,
    @SerializedName("custom_delivery_time") var customDeliveryTime: String?,
    @SerializedName("slot_start")           var slotStart: String?,
    @SerializedName("shipment_id")          var shipmentId: String?,
    @SerializedName("alternate_phone")      var alternatePhone: String?,
    @SerializedName("email_id")             var emailId: String?,
    @SerializedName("slot_end")             var slotEnd: String?
)