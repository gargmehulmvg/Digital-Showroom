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