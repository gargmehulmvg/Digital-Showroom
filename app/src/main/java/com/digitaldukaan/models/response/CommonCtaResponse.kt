package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class CommonCtaResponse(

    @field:SerializedName("is_enabled")
    val isEnabled: Boolean = false,

    @field:SerializedName("bg_color")
    val bgColor: String? = null,

    @field:SerializedName("action")
    val action: String? = null,

    @field:SerializedName("text")
    val text: String? = null,

    @field:SerializedName("text_color")
    val textColor: String? = null,

    @field:SerializedName("type")
    val type: String? = null,

    @field:SerializedName("cdn")
    val cdn: String? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("message_tooltip")
    val messageTooltip: String? = null,

    @field:SerializedName("id")
    val id: Int? = null
)