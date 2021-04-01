package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class MasterCatalogItemResponse(
    @SerializedName("item_id") var itemId: Int,
    @SerializedName("price") var price: Double,
    @SerializedName("item_name") var itemName: String?,
    @SerializedName("image_url") var imageUrl: String?,
    @SerializedName("is_added") var isAdded: Boolean,
    var isSelected: Boolean
)

data class MasterCatalogResponse(
    @SerializedName("master_items") var itemList: ArrayList<MasterCatalogItemResponse>,
    @SerializedName("total_items") var totalItems: Int,
    @SerializedName("is_next") var isNext: Boolean
)
