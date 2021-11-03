package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class SocialMediaPageInfoResponse(
    @SerializedName("static_text")                  var socialMediaStaticTextResponse: SocialMediaStaticTextResponse?,
    @SerializedName("social_media_categories_list") var socialMediaCategoriesList: ArrayList<SocialMediaCategoryItemResponse?>?,
    @SerializedName("category_show_count")          var categoryShowCount: Int,
    @SerializedName("favourite_category_id")        var favouriteCategoryId: Int,
    @SerializedName("is_share_store_locked")        var isShareStoreLocked: Boolean,
    @SerializedName("help_page")                    var socialMediaHelpPage: HelpPageResponse?
)

data class SocialMediaStaticTextResponse(
    @SerializedName("heading_social_media")         var heading_social_media: String?,
    @SerializedName("text_hide")                    var text_hide: String?,
    @SerializedName("text_show_more_categories")    var text_show_more_categories: String

    )
data class SocialMediaCategoryItemResponse(
    @SerializedName("id")                   var id: String?,
    @SerializedName("text")                 var text: String?,
    @SerializedName("logo")                 var logo: String?,
    @SerializedName("text_bg_color")        var textBgColor: String?,
    @SerializedName("text_color")           var textColor: String,
    var isSelected: Boolean
)