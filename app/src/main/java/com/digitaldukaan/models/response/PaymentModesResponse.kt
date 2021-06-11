package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class PaymentModesResponse (
    @SerializedName("payment_options") var paymentOptionsMap: HashMap<String, ArrayList<PaymentModesItemResponse>>?,
    @SerializedName("upi") var upi: PaymentModesItemResponse?,
    @SerializedName("cod") var cod: PaymentModesItemResponse?,
    @SerializedName("IsKYCDone") var kycStatus: PaymentModesKYCStatusResponse?,
    @SerializedName("static_text") var staticText: PaymentModesStaticData?
)

data class PaymentModesStaticData(
    @SerializedName("page_heading_payment_mode") var page_heading_payment_mode: String?,
    @SerializedName("message_complete_kyc_to_unlock") var message_complete_kyc_to_unlock: String?,
    @SerializedName("text_complete_kyc_now") var text_complete_kyc_now: String?,
    @SerializedName("page_sub_heading_payment_mode") var page_sub_heading_payment_mode: String?
)
data class PaymentModesKYCStatusResponse(
    @SerializedName("kyc_status") var isKycActive: Boolean,
    @SerializedName("kyc_cdn") var kycCdn: String?,
    @SerializedName("heading_complete_your_kyc") var heading_complete_your_kyc: String?,
    @SerializedName("message_complete_your_kyc") var message_complete_your_kyc: String?,
    @SerializedName("text_complete_kyc_now") var text_complete_kyc_now: String?
)

data class PaymentModesItemResponse(
    @SerializedName("name") var name: String?,
    @SerializedName("payment_id") var paymentId: String?,
    @SerializedName("image_url") var imageUrl: String?,
    @SerializedName("payment_type") var paymentType: String?,
    @SerializedName("transaction_charges") var transactionCharges: Double,
    @SerializedName("settlement_day") var settlementDay: Int?,
    @SerializedName("status") var status: Int?
)