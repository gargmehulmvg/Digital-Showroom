package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class HelpScreenItemResponse(
    @SerializedName("url")              var url: String?,
    @SerializedName("heading_3")        var heading3: String?,
    @SerializedName("heading_2")        var heading2: String?,
    @SerializedName("heading_1")        var heading1: String?
)
