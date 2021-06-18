package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class TransactionDetailResponse(
    @SerializedName("transaction_id")               var transactionId: String?,
    @SerializedName("transaction_state")            var transactionState: String?,
    @SerializedName("transaction_timestamp")        var transactionTimestamp: String?,
    @SerializedName("transaction_message")          var transactionMessage: String?,
    @SerializedName("settlement_id")                var settlementId: String?,
    @SerializedName("settlement_timestamp")         var settlementTimestamp: String?,
    @SerializedName("settlement_state")             var settlementState: String?,
    @SerializedName("settlement_cdn")               var settlementCdn: String?,
    @SerializedName("settlement_message")           var settlementMessage: String?,
    @SerializedName("order_id")                     var orderId: Int,
    @SerializedName("amount")                       var amount: Double,
    @SerializedName("transaction_charges")          var transactionCharges: Double,
    @SerializedName("transaction_charge_amount")    var transactionChargeAmount: Double,
    @SerializedName("utr")                          var utr: String?,
    @SerializedName("settlement_amount")            var settlementAmount: Double,
    @SerializedName("payment_mode_image")           var paymentImage: String?,
    @SerializedName("cta")                          var ctaItem: CTAItemResponse?,
    @SerializedName("static_text")                  var staticText: TransactionDetailStaticTextResponse
)

data class TransactionDetailStaticTextResponse(
    @SerializedName("order_number")                         var order_number: String?,
    @SerializedName("transaction_charges_bottom_sheet")     var transaction_charges: String?,
    @SerializedName("amount_settled_bottom_sheet")          var amount_to_settled: String?,
    @SerializedName("payment_mode_bottom_sheet")            var payment_mode: String?,
    @SerializedName("bill_amount_order_bottom_sheet")       var bill_amount: String?
)

data class CTAItemResponse(
    @SerializedName("text")             var text: String?,
    @SerializedName("action")           var action: String,
    @SerializedName("display_message")  var displayMessage: String?
)