package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class AddProductItemResponse(
    @SerializedName("id") var id: Int,
    @SerializedName("store_id") var storeId: Int,
    @SerializedName("name") var name: String,
    @SerializedName("price") var price: Double,
    @SerializedName("image_url") var imageUrl: String,
    @SerializedName("available") var available: Int,
    @SerializedName("discounted_price") var discountedPrice: Double,
    @SerializedName("description") var description: String
)