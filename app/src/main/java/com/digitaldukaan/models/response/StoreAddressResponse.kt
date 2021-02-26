package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class StoreAddressResponse(
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("address") var mUserAddressResponse: UserAddressResponse
)

data class UserAddressResponse(
    @SerializedName("address_1") var address1: String,
    @SerializedName("latitude") var latitude: Double,
    @SerializedName("longitude") var longitude: Double,
    @SerializedName("google_address") var googleAddress: String,
    @SerializedName("pincode") var pinCode: String,
    @SerializedName("city") var city: String,
    @SerializedName("state") var state: String
)