package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class MyPaymentsPageInfoResponse(
    @SerializedName("heading_my_payments")                          var heading_my_payments: String?,
    @SerializedName("text_share")                                   var text_share: String?,
    @SerializedName("amount_settle_cdn")                            var amount_settle_cdn: String?,
    @SerializedName("amount_to_settle_cdn")                         var amount_to_settle_cdn: String?,
    @SerializedName("text_amount_to_settle")                        var text_amount_to_settle: String?,
    @SerializedName("text_amount_settled")                          var text_amount_settled: String?,
    @SerializedName("message_share_your_store_now_to_get_orders")   var message_share_your_store_now_to_get_orders: String?,
    @SerializedName("message_order_no_payment_received")            var message_order_no_payment_received: String?,
    @SerializedName("message_settlements_no_payment_received")      var message_settlements_no_payment_received: String?,
    @SerializedName("message_cash_transactions_are_not_shown")      var message_cash_transactions_are_not_shown: String?
)
