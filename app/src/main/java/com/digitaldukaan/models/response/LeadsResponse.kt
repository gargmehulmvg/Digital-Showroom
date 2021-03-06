package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName
import java.util.*

data class LeadsResponse(
    @SerializedName("cart_id")                  var cartId: String = "",
    @SerializedName("customer_name")            var customerName: String = "",
    @SerializedName("phone_number")             var phoneNumber: String = "",
    @SerializedName("last_updated_on")          var lastUpdateOn: String = "",
    @SerializedName("order_value")              var orderValue: Double = 0.0,
    @SerializedName("notification_sent_on")     var notificationSentOn: String = "",
    @SerializedName("cart_type")                var cartType: Int = 0,
    var updatedDate: Date? = Date()
)