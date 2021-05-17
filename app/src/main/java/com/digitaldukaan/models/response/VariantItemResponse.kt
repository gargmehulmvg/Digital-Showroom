package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class VariantItemResponse(
    @SerializedName("variant_id") var variantId: Int,
    @SerializedName("status") var status: Int,
    @SerializedName("available") var available: Int,
    @SerializedName("variant_name") var variantName: String?,
    @SerializedName("master_id") var masterId: Int?,
    var isSelected: Boolean
)