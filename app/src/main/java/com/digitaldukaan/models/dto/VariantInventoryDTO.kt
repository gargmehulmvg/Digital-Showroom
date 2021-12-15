package com.digitaldukaan.models.dto

import com.google.gson.annotations.SerializedName

data class InventoryEnableDTO(

    @field:SerializedName("is_variant_enabled")
    var isVariantEnabled: Boolean = false,

    @field:SerializedName("is_enabled")
    var isEnabled: Boolean = false,

    @field:SerializedName("inventory_count")
    var inventoryCount: Int?,

    @field:SerializedName("inventory_name")
    var inventoryName: String?
)