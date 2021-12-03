package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class BuildCatalogRequest(
    @SerializedName("items")            val buildCatalogItemList: ArrayList<BuildCatalogItemRequest>
)

data class BuildCatalogItemRequest(
    @SerializedName("category_id")      val category_Id: Int?,
    @SerializedName("item_id")          val itemId: Int?,
    @SerializedName("price")            val price: Double?
)