package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class AbandonedCartReminderRequest(
    @SerializedName("cart_id")              var cartId: String?,
    @SerializedName("customer_phone")       var customerPhone: String?,
    @SerializedName("reminder_type")        var reminderType: Int,
    @SerializedName("reminder_send_to")     var reminderSendTo: Int = 0,
)