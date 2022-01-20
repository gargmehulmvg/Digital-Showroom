package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ExploreCategoryPageInfoResponse(
    @SerializedName("master_categories_list")   var masterCategoriesList: ArrayList<ExploreCategoryItemResponse>?,
    @SerializedName("static_text")              var staticText: ExploreCategoryStaticTextResponse?
)

data class ExploreCategoryStaticTextResponse(
    @SerializedName("text_explore")                     val text_explore: String,
    @SerializedName("heading_explore_categories_page")  val heading_explore_categories_page: String,
    @SerializedName("text_set_your_price")              val text_set_your_price: String,
    @SerializedName("category_name")                    val category_name: String,
    @SerializedName("text_tap_to_select")               val text_tap_to_select: String,
    @SerializedName("text_add")                         val text_add: String,
    @SerializedName("text_product")                     val text_product: String,
    @SerializedName("text_products")                    val text_products: String,
    @SerializedName("bottom_sheet_set_price_below")     val bottom_sheet_set_price_below: String,
    @SerializedName("hint_price")                       val hint_price: String,
    @SerializedName("bottom_sheet_set_price")           val bottom_sheet_set_price: String,
    @SerializedName("bottom_sheet_confirm_selection")   val bottom_sheet_confirm_selection: String,
    @SerializedName("hint_mrp")                         val hint_mrp: String,
    @SerializedName("text_rupees_symbol")               val text_rupees_symbol: String
)