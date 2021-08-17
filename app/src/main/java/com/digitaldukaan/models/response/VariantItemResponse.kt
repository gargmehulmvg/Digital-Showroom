package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class VariantItemResponse(
    @SerializedName("variant_id")       var variantId: Int,
    @SerializedName("variant_name")     var variantName: String?,
    @SerializedName("price")            var price: Double,
    @SerializedName("discounted_price") var discountedPrice: Double,
    @SerializedName("status")           var status: Int,
    @SerializedName("master_id")        var masterId: Int?,
    @SerializedName("available")        var available: Int,
    @SerializedName("images")           var variantImagesList: ArrayList<VariantItemImageResponse>?
)

data class VariantItemImageResponse(
    @SerializedName("image_id")         var imageId: Int,
    @SerializedName("image_url")        var imageUrl: String,
    @SerializedName("status")           var status: Int
)