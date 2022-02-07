package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName
import kotlin.collections.ArrayList


data class LeadsResponse(
    @SerializedName("next")                     var mIsNextDataAvailable: Boolean = false,
    @SerializedName("leads")                    var mLeadsList: ArrayList<LeadsItemResponse>
)

class LeadsItemResponse {
    @SerializedName("cart_id")                  var cartId: Int = 0
    @SerializedName("customer_name")            var customerName: Int = 0
    @SerializedName("phone_number")             var phoneNumber: Int = 0
    @SerializedName("last_updated_on")          var lastUpdateOn: String = ""
    @SerializedName("order_value")              var orderValue: String = ""
    @SerializedName("notification_sent_on")     var notificationSentOn: String = ""
    @SerializedName("cart_type")                var cartType: String = ""
}






