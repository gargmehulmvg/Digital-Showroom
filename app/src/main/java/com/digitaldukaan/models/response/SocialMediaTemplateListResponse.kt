package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class SocialMediaTemplateListResponse(
    @SerializedName("is_next_page")                 var isNextPage: Boolean,
    @SerializedName("template_list")                var templateList: ArrayList<SocialMediaTemplateListItemResponse?>?
)

data class SocialMediaTemplateListItemResponse(
    @SerializedName("id")                   var id: String?,
    @SerializedName("cover_image")          var coverImage: String?,
    @SerializedName("is_favourite")         var isFavourite: Boolean?,
    @SerializedName("share")                var share: SocialMediaTemplateShareIconResponse?,
    @SerializedName("edit")                 var edit: SocialMediaTemplateShareIconResponse?,
    @SerializedName("whatsapp")             var whatsapp: SocialMediaTemplateShareIconResponse?,
    @SerializedName("favourite")            var favourite: SocialMediaTemplateShareIconResponse?
)

data class SocialMediaTemplateShareIconResponse(
    @SerializedName("is_active")            var isActive: Boolean,
    @SerializedName("text_share")           var text_share: String?,
    @SerializedName("logo")                 var logo: String,
    @SerializedName("text")                 var text: String
)