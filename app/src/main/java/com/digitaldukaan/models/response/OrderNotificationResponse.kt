package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class OrderNotificationResponse(
    @SerializedName("heading_bottom_sheet")         var headingBottomSheet: String?,
    @SerializedName("notifications_list")           var orderNotificationList: ArrayList<OrderNotificationItemResponse>?

)

data class OrderNotificationItemResponse(
    @SerializedName("id")               var id: Int,
    @SerializedName("heading")          var heading: String,
    @SerializedName("sub_heading")      var subHeading: String,
    @SerializedName("event_name")       var eventName: String,
    @SerializedName("is_selected")      var isSelected: Boolean
)