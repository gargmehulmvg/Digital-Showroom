package com.digitaldukaan.models.dto

import com.google.gson.annotations.SerializedName

data class InventoryEnableDTO(

    @field:SerializedName("is_variant_enabled")
    var isVariantEnabled: Boolean = false,

    @field:SerializedName("is_checkbox_enable")
    var isCheckBoxEnabled: Boolean = false,

    @field:SerializedName("is_edit_text_enable")
    var isEditTextEnabled: Boolean = false,

    @field:SerializedName("inventory_count")
    var inventoryCount: Int?,

    @field:SerializedName("inventory_name")
    var inventoryName: String?,

    @field:SerializedName("is_checkbox_selected")
    var isCheckboxSelected: Boolean = false
)