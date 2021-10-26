package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class SocialMediaTemplateListResponse(
    @SerializedName("is_next_page")                 var isNextPage: Boolean,
    @SerializedName("template_list")                var templateList: ArrayList<SocialMediaTemplateListItemResponse?>?
)

data class SocialMediaTemplateListItemResponse(
    @SerializedName("id")                   var id: String?,
    @SerializedName("cover_image")          var coverImage: String?,
    @SerializedName("text_color")           var textColor: String?,
    @SerializedName("is_favourite")         var isFavourite: Boolean?,
    @SerializedName("share")                var share: SocialMediaTemplateShareIconResponse?,
    @SerializedName("edit")                 var edit: SocialMediaTemplateShareIconResponse?,
    @SerializedName("whatsapp")             var whatsapp: SocialMediaTemplateShareIconResponse?,
    @SerializedName("favourite")            var favourite: SocialMediaTemplateShareIconResponse?,
    @SerializedName("html")                 var html: SocialMediaTemplateHtmlResponse?
)

data class SocialMediaTemplateShareIconResponse(
    @SerializedName("is_active")            var isActive: Boolean,
    @SerializedName("text_share")           var textShare: String?,
    @SerializedName("logo")                 var logo: String,
    @SerializedName("text")                 var text: String
)

data class SocialMediaTemplateHtmlResponse(
    @SerializedName("text_color")           var textColor: String?,
    @SerializedName("html")                 var htmlText: String,
    @SerializedName("html_defaults")        var htmlDefaults: SocialMediaTemplateHtmlDefaultsResponse?
)

data class SocialMediaTemplateHtmlDefaultsResponse(
    @SerializedName("bg_images")            var bgImagesList: ArrayList<SocialMediaTemplateBgImagesResponse>?,
    @SerializedName("text_1")               var text1: SocialMediaTemplateTextResponse,
    @SerializedName("text_2")               var text2: SocialMediaTemplateTextResponse,
    @SerializedName("store_name_max_length")var storeNameMaxLength: Int
)

data class SocialMediaTemplateBgImagesResponse(
    @SerializedName("id")                   var id: String?,
    @SerializedName("src")                  var src: String
)

data class SocialMediaTemplateTextResponse(
    @SerializedName("font_name")            var fontName: String?,
    @SerializedName("text_color")           var textColor: String?,
    @SerializedName("font_size")            var fontSize: Int,
    @SerializedName("max_length")           var maxLength: Int,
    @SerializedName("name")                 var name: String
)