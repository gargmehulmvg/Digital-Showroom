package com.digitaldukaan.models.request

import com.digitaldukaan.models.response.AddProductImagesResponse
import com.digitaldukaan.models.response.VariantItemResponse
import com.google.gson.annotations.SerializedName

data class AddProductRequest(
    @SerializedName("id")                   var id: Int,
    @SerializedName("available")            var available: Int,
    @SerializedName("price")                var price: Double?,
    @SerializedName("discounted_price")     var discountedPrice: Double,
    @SerializedName("description")          var description: String,
    @SerializedName("category")             var itemCategory: AddProductItemCategory,
    @SerializedName("images")               var imageList: ArrayList<AddProductImagesResponse>,
    @SerializedName("name")                 var name: String,
    @SerializedName("variants")             var variantsList: ArrayList<VariantItemResponse>?,
    @SerializedName("low_quantity")         var lowQuantity: Int,
    @SerializedName("managed_inventory")    var managedInventory: Int,
    @SerializedName("available_quantity")   var inventoryCount: Int
)

data class AddProductItemCategory(
    @SerializedName("id")                   var id: Int,
    @SerializedName("name")                 var name: String
)