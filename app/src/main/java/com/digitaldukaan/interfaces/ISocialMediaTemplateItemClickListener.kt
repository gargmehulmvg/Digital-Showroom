package com.digitaldukaan.interfaces

import com.digitaldukaan.models.response.SocialMediaCategoryItemResponse
import com.digitaldukaan.models.response.SocialMediaTemplateListItemResponse

interface ISocialMediaTemplateItemClickListener {

    fun onSocialMediaTemplateCategoryItemClickListener(position: Int, item: SocialMediaCategoryItemResponse?)

    fun onSocialMediaTemplateFavItemClickListener(position: Int, item: SocialMediaTemplateListItemResponse?)

    fun onSocialMediaTemplateEditItemClickListener(position: Int, item: SocialMediaTemplateListItemResponse?)

    fun onSocialMediaTemplateShareItemClickListener(position: Int, item: SocialMediaTemplateListItemResponse?)

    fun onSocialMediaTemplateWhatsappItemClickListener(position: Int, item: SocialMediaTemplateListItemResponse?)

}