package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class UpdateInvitationRequest(
    @SerializedName("status")       var status: Int,
    @SerializedName("store_id")     var storeId: Int,
    @SerializedName("user_id")      var userId: Int,
    @SerializedName("language_id")  var languageId: Int
)