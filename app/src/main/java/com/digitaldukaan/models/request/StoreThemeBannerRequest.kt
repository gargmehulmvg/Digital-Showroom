package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class StoreThemeBannerRequest(
    @SerializedName("store_theme_id") var id: Int?,
    @SerializedName("img_id_url") var imageUrlItems: ArrayList<ImageUrlItemRequest>?
)
data class ImageUrlItemRequest(
    @SerializedName("img_id") var id: Int?,
    @SerializedName("img_url") var url: String?
)