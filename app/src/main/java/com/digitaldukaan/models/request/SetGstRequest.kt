package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class SetGstRequest(
    @SerializedName("text") var text: String
)