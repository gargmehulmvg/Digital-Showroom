package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class RequestToCallbackRequest(
    @SerializedName("text")     var text: String
)