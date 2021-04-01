package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ExploreCategoryItemResponse(
    @SerializedName("category_id") var categoryId: Int?,
    @SerializedName("category_name") var categoryName: String?,
    @SerializedName("image_url") var imageUrl: String?
)
