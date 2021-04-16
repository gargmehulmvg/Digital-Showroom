package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class AddProductResponse(
    @SerializedName("static_text") var addProductStaticText: AddProductStaticText?,
    @SerializedName("categories") var addProductStoreCategories: AddProductStoreCategory?,
    @SerializedName("option_menu") var addProductStoreOptionsMenu: ArrayList<TrendingListResponse>?,
    @SerializedName("store_item") var storeItem: AddProductItemResponse
)

data class AddProductStoreCategory(
    @SerializedName("store_categories") val storeCategoriesList:ArrayList<AddStoreCategoryItem>?,
    @SerializedName("suggested_categories") val suggestedCategories:String?
)

data class AddStoreCategoryItem(
    @SerializedName("id") val id: Int?,
    @SerializedName("name") val name:String?,
    var isSelected: Boolean
)

data class AddProductStaticText(
    @SerializedName("product_page_heading") var product_page_heading: String?,
    @SerializedName("bottom_sheet_add_from_gallery") var bottom_sheet_add_from_gallery: String?,
    @SerializedName("bottom_sheet_add_image") var bottom_sheet_add_image: String?,
    @SerializedName("bottom_sheet_confirm_selection") var bottom_sheet_confirm_selection: String?,
    @SerializedName("bottom_sheet_hint_search_for_images_here") var bottom_sheet_hint_search_for_images_here: String?,
    @SerializedName("bottom_sheet_remove_image") var bottom_sheet_remove_image: String?,
    @SerializedName("bottom_sheet_search_image") var bottom_sheet_search_image: String?,
    @SerializedName("bottom_sheet_set_price") var bottom_sheet_set_price: String?,
    @SerializedName("bottom_sheet_set_price_below") var bottom_sheet_set_price_below: String?,
    @SerializedName("bottom_sheet_take_a_photo") var bottom_sheet_take_a_photo: String?,
    @SerializedName("bottom_sheet_you_can_add_upto_4_images") var bottom_sheet_you_can_add_upto_4_images: String?,
    @SerializedName("error_mandatory_field") var error_mandatory_field: String?,
    @SerializedName("error_discounted_price_must_be_less_than_items_price") var error_discount_price_less_then_original_price: String?,
    @SerializedName("heading_add_product_banner") var heading_add_product_banner: String?,
    @SerializedName("heading_add_product_page") var heading_add_product_page: String?,
    @SerializedName("heading_explore_categories_page") var heading_explore_categories_page: String?,
    @SerializedName("heading_search_products_page") var heading_search_products_page: String?,
    @SerializedName("hint_enter_category_optional") var hint_enter_category_optional: String?,
    @SerializedName("hint_item_name") var hint_item_name: String?,
    @SerializedName("hint_mrp") var hint_mrp: String?,
    @SerializedName("hint_price") var hint_price: String?,
    @SerializedName("hint_search_products") var hint_search_products: String?,
    @SerializedName("hint_tap_to_edit_price") var hint_tap_to_edit_price: String?,
    @SerializedName("text_add") var text_add: String?,
    @SerializedName("text_add_item") var text_add_item: String?,
    @SerializedName("text_add_item_description") var text_add_item_description: String?,
    @SerializedName("text_added_already") var text_added_already: String?,
    @SerializedName("text_explore") var text_explore: String?,
    @SerializedName("text_images_added") var text_images_added: String?,
    @SerializedName("text_product") var text_product: String?,
    @SerializedName("text_products") var text_products: String?,
    @SerializedName("text_rupees_symbol") var text_rupees_symbol: String?,
    @SerializedName("text_set_your_price") var text_set_your_price: String?,
    @SerializedName("text_tap_to_select") var text_tap_to_select: String?,
    @SerializedName("text_try_now") var text_try_now: String?,
    @SerializedName("text_upload_or_search_images") var text_upload_or_search_images: String?,
    @SerializedName("hint_discounted_price") var hint_discounted_price: String?,

    @SerializedName("heading_view_store_as_a_customer") var heading_view_store_as_customer: String?,
    @SerializedName("message_get_premium_website_for_your_showroom") var message_get_premium_website_for_your_showroom: String?,
    @SerializedName("text_get_started") var text_get_started: String?,

    @SerializedName("bottom_sheet_heading_edit_category") var bottom_sheet_heading_edit_category: String?,
    @SerializedName("bottom_sheet_category_name") var bottom_sheet_category_name: String?,
    @SerializedName("bottom_sheet_hint_category_name") var bottom_sheet_hint_category_name: String?,
    @SerializedName("bottom_sheet_delete_category") var bottom_sheet_delete_category: String?,
    @SerializedName("bottom_sheet_save") var bottom_sheet_save: String?,
    @SerializedName("dialog_stock_message") var dialog_stock_message: String?,
    @SerializedName("dialog_stock_dont_show_this_again") var dialog_stock_dont_show_this_again: String?,
    @SerializedName("text_yes") var text_yes: String?,
    @SerializedName("text_no") var text_no: String?
)