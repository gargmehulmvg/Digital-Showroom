package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class HelpScreenItemResponse(
    @SerializedName("url")      var url: String?,
    @SerializedName("text")     var text: String?
)
