package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class EditPremiumColorItemResponse(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String?,
    @SerializedName("image_url") var imageUrl: String?,
    @SerializedName("primary_color") var primaryColor: String?,
    @SerializedName("secondary_color") var secondaryColor: String?,
    var isSelected: Boolean
)
