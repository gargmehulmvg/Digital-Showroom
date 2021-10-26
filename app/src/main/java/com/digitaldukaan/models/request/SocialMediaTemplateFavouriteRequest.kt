package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class SocialMediaTemplateFavouriteRequest(
    @SerializedName("template_id") var templateId: Int,
    @SerializedName("is_favourite") var isFavourite: Boolean
)