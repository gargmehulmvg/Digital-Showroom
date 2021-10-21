package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ProductCategoryCombineResponse(
    @SerializedName("category")     var category: StoreCategoryItem,
    @SerializedName("items")        var productsList: ArrayList<ProductResponse>?
)
