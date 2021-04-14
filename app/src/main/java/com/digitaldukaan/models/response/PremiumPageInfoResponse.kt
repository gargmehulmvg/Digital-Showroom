package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class PremiumPageInfoResponse(
    @SerializedName("theme") var theme: ThemeResponse?,
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
    @SerializedName("category") var category: String,
    @SerializedName("expiry") var expiry: String
)