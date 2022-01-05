package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class ValidateUserRequest(
    @SerializedName("payload")          var payload: String,
    @SerializedName("signature")        var signature: String?,
    @SerializedName("ct_id")            var cleverTapId: String?,
    @SerializedName("fcm_token")        var firebaseToken: String?,
    @SerializedName("fcm_instance_id")  var firebaseInstanceId: String?,
    @SerializedName("phone")            var phone: String
)