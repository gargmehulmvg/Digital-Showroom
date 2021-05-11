package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class AddProductBannerTextResponse(
    @SerializedName("offer") var offer: String?,
    @SerializedName("header") var header: String?,
    @SerializedName("body") var body: String?,
    @SerializedName("button_text") var button_text: String?,
    @SerializedName("image_url") var image_url: String?,
    @SerializedName("flag") var flag: Boolean
)