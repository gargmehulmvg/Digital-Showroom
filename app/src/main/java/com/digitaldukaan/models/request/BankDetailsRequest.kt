package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class BankDetailsRequest(
    @SerializedName("account_holder_name") var accountHolderName: String?,
    @SerializedName("registered_phone") var registeredPhone: String?,
    @SerializedName("account_number") var accountNumber: String?,
    @SerializedName("ifsc_code") var ifscCode: String?
)