package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName
import java.util.*

data class MyPaymentsResponse(
    @SerializedName("transaction_list")         var mMyPaymentList: ArrayList<MyPaymentsItemResponse>?,
    @SerializedName("settled_amount")           var mSettledAmount: Double,
    @SerializedName("unsettled_amount")         var mUnsettledAmount: Double,
    @SerializedName("is_next_page")             var mIsNextPage: Boolean
)

data class MyPaymentsItemResponse(
    @SerializedName("transaction_id")           var transactionId: String?,
    @SerializedName("transaction_timestamp")    var transactionTimestamp: String?,
    @SerializedName("amount_paid")              var amount: Double?,
    @SerializedName("order_id")                 var orderId: String?,
    @SerializedName("utr")                      var utr: String?,
    @SerializedName("image_url")                var imageUrl: String?,
    @SerializedName("settlement_type")          var transactionState: String?,
    var updatedDate: Date? = Date(),
    var updatedCompleteDate: Date? = Date()
)
