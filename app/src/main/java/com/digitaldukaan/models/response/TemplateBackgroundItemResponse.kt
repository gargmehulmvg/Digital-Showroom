package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class TemplateBackgroundItemResponse(
    @SerializedName("name")         var name: String?,
    @SerializedName("type")         var type: String?,
    @SerializedName("text_color")   var textColor: String?,
    var isSelected: Boolean
)
