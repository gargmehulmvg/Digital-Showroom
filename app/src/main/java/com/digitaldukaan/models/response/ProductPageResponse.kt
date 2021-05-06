package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ProductPageResponse(
    @SerializedName("product_page_url") var product_page_url: String,
    @SerializedName("is_zero_product") var isZeroProduct: Boolean,
    @SerializedName("option_menu") var optionMenuList: ArrayList<TrendingListResponse>,
    @SerializedName("share_shop") var shareShop: TrendingListResponse?,
    @SerializedName("add_product") var addProduct: TrendingListResponse?,
    @SerializedName("static_text") var static_text: AddProductStaticText
)