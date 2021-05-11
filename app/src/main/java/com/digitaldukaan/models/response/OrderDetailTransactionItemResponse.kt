package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class OrderDetailTransactionItemResponse(
    @SerializedName("transaction_id") var transactionId: String?,
    @SerializedName("settlement_status") var settlementStatus: String?,
    @SerializedName("transaction_status") var transactionStatus: String?
)