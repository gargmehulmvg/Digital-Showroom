package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class ConvertFileToLinkRequest(
    @SerializedName("file") var file: String?
)