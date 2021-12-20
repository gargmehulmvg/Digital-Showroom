package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class AddProductResponse(
    @SerializedName("static_text")                                              var addProductStaticText: AddProductStaticText?,
    @SerializedName("domain")                                                   var domain: String?,
    @SerializedName("categories")                                               var addProductStoreCategories: AddProductStoreCategory?,
    @SerializedName("option_menu")                                              var addProductStoreOptionsMenu: ArrayList<TrendingListResponse>?,
    @SerializedName("recent_variants")                                          var recentVariantsList: ArrayList<VariantItemResponse>?,
    @SerializedName("master_variants")                                          var masterVariantsList: ArrayList<VariantItemResponse>?,
    @SerializedName("store_item")                                               var storeItem: AddProductItemResponse?,
    @SerializedName("youtube_info")                                             var youtubeInfo: YoutubeInfoResponse?,
    @SerializedName("is_share_store_locked")                                    var isShareStoreLocked: Boolean,
    var deletedVariants: HashMap<String?, VariantItemResponse?> = HashMap(),
    var isVariantSaved: Boolean
)

data class YoutubeInfoResponse(
    @SerializedName("heading")                                                  val heading:String?,
    @SerializedName("steps")                                                    val steps:ArrayList<String>?,
    @SerializedName("hint_1")                                                   val hint1:String?,
    @SerializedName("hint_2")                                                   val hint2:String?,
    @SerializedName("message")                                                  val message:String?
)

data class AddProductStoreCategory(
    @SerializedName("store_categories")                                         val storeCategoriesList:ArrayList<StoreCategoryItem>?,
    @SerializedName("suggested_categories")                                     val suggestedCategories:String?
)

data class StoreCategoryItem(
    @SerializedName("id")                                                       val id: Int,
    @SerializedName("name")                                                     val name:String?,
    var isSelected: Boolean
)

data class AddProductStaticText(
    @SerializedName("product_page_heading")                                     var product_page_heading: String?,
    @SerializedName("bottom_sheet_add_from_gallery")                            var bottom_sheet_add_from_gallery: String?,
    @SerializedName("bottom_sheet_add_image")                                   var bottom_sheet_add_image: String?,
    @SerializedName("bottom_sheet_confirm_selection")                           var bottom_sheet_confirm_selection: String?,
    @SerializedName("bottom_sheet_hint_search_for_images_here")                 var bottom_sheet_hint_search_for_images_here: String?,
    @SerializedName("bottom_sheet_remove_image")                                var bottom_sheet_remove_image: String?,
    @SerializedName("bottom_sheet_search_image")                                var bottom_sheet_search_image: String?,
    @SerializedName("bottom_sheet_set_price")                                   var bottom_sheet_set_price: String?,
    @SerializedName("bottom_sheet_set_price_below")                             var bottom_sheet_set_price_below: String?,
    @SerializedName("bottom_sheet_take_a_photo")                                var bottom_sheet_take_a_photo: String?,
    @SerializedName("bottom_sheet_you_can_add_upto_4_images")                   var bottom_sheet_you_can_add_upto_4_images: String?,
    @SerializedName("error_mandatory_field")                                    var error_mandatory_field: String?,
    @SerializedName("error_discounted_price_must_be_less_than_items_price")     var error_discount_price_less_then_original_price: String?,
    @SerializedName("heading_add_product_banner")                               var heading_add_product_banner: String?,
    @SerializedName("heading_add_product_page")                                 var heading_add_product_page: String?,
    @SerializedName("heading_explore_categories_page")                          var heading_explore_categories_page: String?,
    @SerializedName("heading_search_products_page")                             var heading_search_products_page: String?,
    @SerializedName("hint_enter_category_optional")                             var hint_enter_category_optional: String?,
    @SerializedName("hint_item_name")                                           var hint_item_name: String?,
    @SerializedName("hint_mrp")                                                 var hint_mrp: String?,
    @SerializedName("hint_price")                                               var hint_price: String?,
    @SerializedName("hint_search_products")                                     var hint_search_products: String?,
    @SerializedName("hint_tap_to_edit_price")                                   var hint_tap_to_edit_price: String?,
    @SerializedName("text_add")                                                 var text_add: String?,
    @SerializedName("text_add_item")                                            var text_add_item: String?,
    @SerializedName("text_add_item_description")                                var text_add_item_description: String?,
    @SerializedName("text_added_already")                                       var text_added_already: String?,
    @SerializedName("text_explore")                                             var text_explore: String?,
    @SerializedName("text_images_added")                                        var text_images_added: String?,
    @SerializedName("text_product")                                             var text_product: String?,
    @SerializedName("text_products")                                            var text_products: String?,
    @SerializedName("text_rupees_symbol")                                       var text_rupees_symbol: String?,
    @SerializedName("text_set_your_price")                                      var text_set_your_price: String?,
    @SerializedName("text_tap_to_select")                                       var text_tap_to_select: String?,
    @SerializedName("text_add_discount_on_this_item")                           var text_add_discount_on_this_item: String?,
    @SerializedName("text_try_now")                                             var text_try_now: String?,
    @SerializedName("text_upload_or_search_images")                             var text_upload_or_search_images: String?,
    @SerializedName("hint_discounted_price")                                    var hint_discounted_price: String?,
    @SerializedName("heading_view_store_as_a_customer")                         var heading_view_store_as_customer: String?,
    @SerializedName("message_get_premium_website_for_your_showroom")            var message_get_premium_website_for_your_showroom: String?,
    @SerializedName("text_get_started")                                         var text_get_started: String?,
    @SerializedName("bottom_sheet_heading_edit_category")                       var bottom_sheet_heading_edit_category: String?,
    @SerializedName("bottom_sheet_category_name")                               var bottom_sheet_category_name: String?,
    @SerializedName("bottom_sheet_hint_category_name")                          var bottom_sheet_hint_category_name: String?,
    @SerializedName("bottom_sheet_delete_category")                             var bottom_sheet_delete_category: String?,
    @SerializedName("bottom_sheet_save")                                        var bottom_sheet_save: String?,
    @SerializedName("dailog_stock_dont_show_this_again")                        var dialog_stock_dont_show_this_again: String?,
    @SerializedName("text_yes")                                                 var text_yes: String?,
    @SerializedName("text_no")                                                  var text_no: String?,
    @SerializedName("text_share_product")                                       var text_share_product: String?,
    @SerializedName("text_go_back_message")                                     var text_go_back_message: String?,
    @SerializedName("text_go_back")                                             var text_go_back: String?,
    @SerializedName("text_plus_add")                                            var text_plus_add: String?,
    @SerializedName("text_suggestions")                                         var text_suggestions: String?,
    @SerializedName("text_variants")                                            var text_variants: String?,
    @SerializedName("heading_edit_variant")                                     var heading_edit_variant: String?,
    @SerializedName("text_save")                                                var text_save: String?,
    @SerializedName("heading_manage_inventory")                                 var heading_manage_inventory: String?,
    @SerializedName("sub_heading_inventory")                                    var sub_heading_inventory: String?,
    @SerializedName("footer_text_low_stock_alert")                              var footer_text_low_stock_alert: String?,
    @SerializedName("dialog_heading_inventory")                                 var dialog_heading_inventory: String?,
    @SerializedName("dialog_sub_heading_inventory")                             var dialog_sub_heading_inventory: String?,
    @SerializedName("dialog_cta_disable_inventory")                             var dialog_cta_disable_inventory: String?,
    @SerializedName("dialog_cta_add_inventory")                                 var dialog_cta_add_inventory: String?,
    @SerializedName("dailog_stock_message")                                     var dialog_stock_message: String?,
    @SerializedName("text_delete")                                              var text_delete: String?,
    @SerializedName("text_cancel")                                              var text_cancel: String?,
    @SerializedName("dialog_delete_variant_message")                            var dialog_delete_variant_message: String?,
    @SerializedName("text_in_stock")                                            var text_in_stock: String?,
    @SerializedName("text_recently_created")                                    var text_recently_created: String?,
    @SerializedName("text_active_variants")                                     var text_active_variants: String?,
    @SerializedName("hint_variant_name")                                        var hint_variant_name: String?,
    @SerializedName("text_add_variant")                                         var text_add_variant: String?,
    @SerializedName("error_variant_name_same_with_item_name")                   var error_variant_name_same_with_item_name: String?,
    @SerializedName("dialog_heading_mark_inventory")                            var dialog_heading_mark_inventory: String?,
    @SerializedName("dialog_sub_heading_mark_inventory")                        var dialog_sub_heading_mark_inventory: String?,
    @SerializedName("dialog_heading_add_inventory")                             var dialog_heading_add_inventory: String?,
    @SerializedName("hint_available_quantity")                                  var hint_available_quantity: String?,
    @SerializedName("error_text_inventory_limit")                               var error_text_inventory_limit: String?,
    @SerializedName("error_variant_already_exist")                              var error_variant_already_exist: String?
)