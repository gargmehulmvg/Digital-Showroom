package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class CreateStoreRequest(
    @SerializedName("phone")            var phone: String?,
    @SerializedName("user_id")          var userId: Int,
    @SerializedName("store_name")       var storeName: String?,
    @SerializedName("secret_key")       var secretKey: String?,
    @SerializedName("language_id")      var languageId: Int,
    @SerializedName("reference_phone")  var referencePhone: String?,
    @SerializedName("appsflyer_id")     var appsflyerId: String?,
    @SerializedName("clevertap_id")     var cleverTapId: String?,
    @SerializedName("longitude")        var longitude: Double,
    @SerializedName("latitude")         var latitude: Double
)