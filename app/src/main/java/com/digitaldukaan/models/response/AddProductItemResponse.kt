package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class AddProductItemResponse(
    @SerializedName("id")               var id: Int,
    @SerializedName("store_id")         var storeId: Int,
    @SerializedName("name")             var name: String,
    @SerializedName("price")            var price: Double,
    @SerializedName("image_url")        var imageUrl: String,
    @SerializedName("available")        var available: Int,
    @SerializedName("discounted_price") var discountedPrice: Double,
    @SerializedName("category")         var category: StoreCategoryItem?,
    @SerializedName("images")           var imagesList: ArrayList<AddProductImagesResponse>?,
    @SerializedName("variants")         var variantsList: ArrayList<VariantItemResponse>?,
    @SerializedName("description")      var description: String
)

data class AddProductImagesResponse(
    @SerializedName("image_id")         var imageId: Int,
    @SerializedName("image_url")        var imageUrl: String,
    @SerializedName("status")           var status: Int,
    @SerializedName("media_type")       var mediaType: Int
)