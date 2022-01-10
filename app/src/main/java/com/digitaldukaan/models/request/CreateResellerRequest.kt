package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class CreateResellerRequest(
    @SerializedName("email")    var emailId: String?
)