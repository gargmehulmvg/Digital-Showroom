package com.digitaldukaan.models.dto

import com.google.gson.annotations.SerializedName

data class ConvertMultiImageDTO(
    @SerializedName("id") var id: String = "",
    @SerializedName("src") var src: String = ""
)