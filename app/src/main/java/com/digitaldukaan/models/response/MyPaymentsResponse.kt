package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName
import java.util.*

data class MyPaymentsResponse(
    @SerializedName("my_payments_list")         var mMyPaymentList: ArrayList<MyPaymentsItemResponse>?,
    @SerializedName("is_next_page")             var mIsNextPage: Boolean
)

data class MyPaymentsItemResponse(
    @SerializedName("transaction_id")           var transactionId: String?,
    @SerializedName("transaction_timestamp")    var transactionTimestamp: String?,
    @SerializedName("amount")                   var amount: Double?,
    @SerializedName("order_id")                 var orderId: String?,
    @SerializedName("utr")                      var utr: String?,
    @SerializedName("image_url")                var imageUrl: String?,
    @SerializedName("transaction_state")        var transactionState: String?,
    var updatedDate: Date? = Date(),
    var updatedCompleteDate: Date? = Date()
)
