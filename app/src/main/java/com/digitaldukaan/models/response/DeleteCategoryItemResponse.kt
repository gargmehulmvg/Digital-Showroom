package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class DeleteCategoryItemResponse(
    @SerializedName("text") var text: String?,
    @SerializedName("action") var action: String?
)