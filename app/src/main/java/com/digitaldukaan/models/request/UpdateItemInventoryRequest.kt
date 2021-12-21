package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class UpdateItemInventoryRequest(
    @SerializedName("store_item_id")            var storeItemId: Int,
    @SerializedName("available_quantity")       var availableQuantity: Int,
    @SerializedName("managed_inventory")        var managedInventory: Int = 1,
    @SerializedName("variant_id")               var variantId: Int = 0
)