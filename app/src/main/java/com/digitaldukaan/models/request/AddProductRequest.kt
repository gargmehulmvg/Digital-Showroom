package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class AddProductRequest(
    @SerializedName("id") var id: Int,
    @SerializedName("available") var available: Int,
    @SerializedName("price") var price: Double,
    @SerializedName("discounted_price") var discountedPrice: Double,
    @SerializedName("description") var description: String,
    @SerializedName("ItemCategory") var itemCategory: AddProductItemCategory,
    @SerializedName("name") var name: String
)

data class AddProductItemCategory(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String
)