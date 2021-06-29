package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class PaymentLinkResponse(
    @SerializedName("sms")          var sms: SMSItemResponse?,
    @SerializedName("whatsapp")     var whatsapp: WhatsAppItemResponse?
)

data class SMSItemResponse(
    @SerializedName("date")             var date: String?,
    @SerializedName("time")             var time: String?,
    @SerializedName("amount")           var amount: String?,
    @SerializedName("order_id")         var orderId: String?,
    @SerializedName("static_text")      var staticText: SMSStaticTextResponse?
)

data class WhatsAppItemResponse(
    @SerializedName("text")     var text: String?
)

data class SMSStaticTextResponse(
    @SerializedName("text_your_link_sent_to")   var text_your_link_sent_to: String?,
    @SerializedName("text_amount")              var text_amount: String?,
    @SerializedName("text_id")                  var text_id: String?,
    @SerializedName("text_time")                var text_time: String?,
    @SerializedName("text_date")                var text_date: String?
)