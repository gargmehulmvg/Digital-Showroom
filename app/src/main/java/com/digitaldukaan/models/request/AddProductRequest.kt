package com.digitaldukaan.models.request

import com.digitaldukaan.models.response.VariantItemResponse
import com.google.gson.annotations.SerializedName

data class AddProductRequest(
    @SerializedName("id") var id: Int,
    @SerializedName("available") var available: Int,
    @SerializedName("price") var price: Double?,
    @SerializedName("discounted_price") var discountedPrice: Double,
    @SerializedName("description") var description: String,
    @SerializedName("category") var itemCategory: AddProductItemCategory,
    @SerializedName("images") var imageList: ArrayList<AddProductImageItem>,
    @SerializedName("name") var name: String,
    @SerializedName("variants") var variantsList: ArrayList<VariantItemResponse>?
)

data class AddProductItemCategory(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String
)

data class AddProductImageItem(
    @SerializedName("image_id") var id: Int,
    @SerializedName("image_url") var name: String,
    @SerializedName("status") var status: Int
)