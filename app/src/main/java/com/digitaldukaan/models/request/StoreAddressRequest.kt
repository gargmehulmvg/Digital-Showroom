package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class StoreAddressRequest(
    @SerializedName("store_id")         var storeId: Int?,
    @SerializedName("address")          var address: String,
    @SerializedName("google_address")   var googleAddress: String? = "",
    @SerializedName("latitude")         var latitude: Double = 0.0,
    @SerializedName("longitude")        var longitude: Double = 0.0,
    @SerializedName("pincode")          var pinCode: String = "",
    @SerializedName("city")             var city: String = "",
    @SerializedName("state")            var state: String = ""
)