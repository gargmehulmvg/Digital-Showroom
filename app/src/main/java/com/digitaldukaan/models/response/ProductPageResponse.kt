package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ProductPageResponse(
    @SerializedName("product_page_url")         var productPageUrl: String,
    @SerializedName("is_zero_product")          var isZeroProduct: Boolean,
    @SerializedName("option_menu")              var optionMenuList: ArrayList<TrendingListResponse>,
    @SerializedName("share_shop")               var shareShop: TrendingListResponse?,
    @SerializedName("add_product")              var addProduct: TrendingListResponse?,
    @SerializedName("is_share_store_locked")    var isShareStoreLocked: Boolean,
    @SerializedName("web_console_bottom_sheet") var webConsoleBottomSheet: WebConsoleBottomSheetResponse,
    @SerializedName("static_text")              var staticText: AddProductStaticText
)