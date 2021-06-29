package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class PaymentLinkResponse(
    @SerializedName("sms")                        var sms: SMSItemResponse?,
    @SerializedName("whatsapp")                   var whatsapp: WhatsAppItemResponse?
)

data class SMSItemResponse(
    @SerializedName("order_number")                         var order_number: String?,
    @SerializedName("transaction_charges_bottom_sheet")     var transaction_charges: String?,
    @SerializedName("amount_settled_bottom_sheet")          var amount_to_settled: String?,
    @SerializedName("payment_mode_bottom_sheet")            var payment_mode: String?,
    @SerializedName("bill_amount_order_bottom_sheet")       var bill_amount: String?
)

data class WhatsAppItemResponse(
    @SerializedName("text")             var text: String?
)