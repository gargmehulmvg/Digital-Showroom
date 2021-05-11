package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class BankDetailsResponse (
    @SerializedName("account_holder_name") var accountHolderName: String?,
    @SerializedName("registered_phone") var registeredPhone: String?,
    @SerializedName("bank_id") var bankId: String?,
    @SerializedName("bank_name") var bankName: String?,
    @SerializedName("transfer_type") var transferType: String?,
    @SerializedName("account_number") var accountNumber: String?,
    @SerializedName("ifsc_code") var ifscCode: String?,
    @SerializedName("account_status") var accountStatus: String?
)

data class BankDetailsPageInfoResponse (
    @SerializedName("static_text") var mBankPageStaticText: BankDetailsPageStaticTextResponse,
    @SerializedName("bank_details") var mBankDetail: BankDetailsResponse
)

data class BankDetailsPageStaticTextResponse(
    @SerializedName("heading_bank_page") var heading_bank_page: String?,
    @SerializedName("sub_heading_bank_page") var sub_heading_bank_page: String?,
    @SerializedName("hint_bank_account_holder_name") var hint_bank_account_holder_name: String?,
    @SerializedName("hint_bank_account_number") var hint_bank_account_number: String?,
    @SerializedName("hint_bank_verify_account_number") var hint_bank_verify_account_number: String?,
    @SerializedName("hint_bank_ifsc_code") var hint_bank_ifsc_code: String?,
    @SerializedName("hint_bank_registered_mobile_number") var hint_bank_registered_mobile_number: String?,
    @SerializedName("bank_save_changes") var hint_bank_save_changes: String?,
    @SerializedName("text_skip") var text_skip: String?,
    @SerializedName("error_mandatory_field") var error_mandatory_field: String?,
    @SerializedName("error_invalid_mobile_number_field") var error_invalid_mobile_number: String?,
    @SerializedName("error_invalid_account_number") var error_invalid_account_number: String?,
    @SerializedName("error_both_account_number_verify_account_number_must_be_same") var error_both_account_number_verify_account_number_must_be_same: String?
)