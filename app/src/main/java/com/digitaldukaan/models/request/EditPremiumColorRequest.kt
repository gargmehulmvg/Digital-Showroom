package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class EditPremiumColorRequest(
    @SerializedName("store_theme_id")       var storeThemeId: Int?,
    @SerializedName("color_palette_id")     var colorPaletteId: Int?
)