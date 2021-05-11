package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class DeleteCategoryRequest(
    @SerializedName("category_id") var id: Int?,
    @SerializedName("delete_items") var deleteItems: Boolean
)