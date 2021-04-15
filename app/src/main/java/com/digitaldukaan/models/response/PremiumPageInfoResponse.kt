package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class PremiumPageInfoResponse(
    @SerializedName("theme") var theme: ThemeResponse?,
    @SerializedName("static_text") var staticText: PremiumPageInfoStaticTextResponse?,
    @SerializedName("domain") var domain: String?,
    @SerializedName("premium") var premium: PremiumPageActiveResponse?
)

data class PremiumPageActiveResponse(
    @SerializedName("is_active") var mIsActive: Boolean,
    @SerializedName("deep_link") var mUrl: String
)

data class ThemeResponse(
    @SerializedName("theme_id") var themeId: Int,
    @SerializedName("store_theme_id") var storeThemeId: Int,
    @SerializedName("roundness") var roundness: Int,
    @SerializedName("is_quick_view") var isQuickView: Int,
    @SerializedName("category") var category: String,
    @SerializedName("colors") var colorItem: EditPremiumColorItemResponse,
    @SerializedName("components") var themeComponent: ThemeComponentResponse,
    @SerializedName("expiry") var expiry: String
)

data class ThemeComponentResponse(
    @SerializedName("Body") var body: ArrayList<ThemeBodyResponse>
)

data class ThemeBodyResponse(
    @SerializedName("images") var images: ArrayList<ThemeBodyImagesItemResponse>
)

data class ThemeBodyImagesItemResponse(
    @SerializedName("id") var id: Int,
    @SerializedName("image_url") var imageUrl: String
)

data class PremiumPageInfoStaticTextResponse(
    @SerializedName("heading_crop_for_desktop_view") var heading_crop_for_desktop_view: String,
    @SerializedName("heading_crop_for_mobile_view") var heading_crop_for_mobile_view: String,
    @SerializedName("heading_edit_theme") var heading_edit_theme: String,
    @SerializedName("text_crop_for_desktop") var text_crop_for_desktop: String,
    @SerializedName("text_cropped_for_mobile_website") var text_cropped_for_mobile_website: String,
    @SerializedName("text_customer_will_see") var text_customer_will_see: String,
    @SerializedName("text_edit_colors") var text_edit_colors: String,
    @SerializedName("text_edit_image") var text_edit_image: String,
    @SerializedName("text_lets_crop_for_desktop_website") var text_lets_crop_for_desktop_website: String,
    @SerializedName("text_save_changes") var text_save_changes: String,
    @SerializedName("text_theme_color") var text_theme_color: String
)