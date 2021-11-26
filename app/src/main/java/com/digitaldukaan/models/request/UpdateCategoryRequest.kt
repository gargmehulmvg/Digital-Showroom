package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class UpdateCategoryRequest(
    @SerializedName("id")       var id: Int?,
    @SerializedName("name")     var name: String?
)