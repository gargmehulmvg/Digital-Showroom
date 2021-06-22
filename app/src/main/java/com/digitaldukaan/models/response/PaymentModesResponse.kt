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
    @SerializedName("message_activate_and_start_payment_one") var message_activate_and_start_payment: String?,
    @SerializedName("message_activate_and_start_payment_two") var message_activate_and_start_payment2: String?,
    @SerializedName("text_complete_kyc_now") var text_complete_kyc_now: String?,
    @SerializedName("text_please_note") var text_please_note: String?,
    @SerializedName("text_confirmation") var text_confirmation: String?,
    @SerializedName("message_confirm_payment_mode") var message_confirm_payment_mode: String?,
    @SerializedName("text_no") var text_no: String?,
    @SerializedName("text_yes") var text_yes: String?,
    @SerializedName("text_activate") var text_activate: String?,
    @SerializedName("text_instant_settlements") var text_instant_settlements: String?,
    @SerializedName("text_txn_charge") var text_txn_charge: String?,
    @SerializedName("text_view_your_payment_report") var text_view_your_payment_report: String?,
    @SerializedName("heading_view_your_payments_and_settlements") var heading_view_your_payments_and_settlements: String?,
    @SerializedName("message_receive_the_amount_in_your_bank") var message_receive_the_amount_in_your_bank: String?,
    @SerializedName("message_transaction_charges_will_be_applied") var message_transaction_charges_will_be_applied: String?,
    @SerializedName("page_sub_heading_payment_mode") var page_sub_heading_payment_mode: String?
)
data class PaymentModesKYCStatusResponse(
    @SerializedName("kyc_status") var isKycActive: Boolean,
    @SerializedName("kyc_cdn") var kycCdn: String?,
    @SerializedName("heading") var heading_complete_your_kyc: String?,
    @SerializedName("message") var message_complete_your_kyc: String?,
    @SerializedName("cta_text") var text_complete_kyc_now: String?
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