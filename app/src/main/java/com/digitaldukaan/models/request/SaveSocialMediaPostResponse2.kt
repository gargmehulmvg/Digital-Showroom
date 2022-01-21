package com.digitaldukaan.models.request

import com.digitaldukaan.models.response.SocialMediaTemplateHtmlDefaultsResponse
import com.google.gson.annotations.SerializedName

data class SaveSocialMediaPostRequest(

	@field:SerializedName("share_type")
	val shareType: String? = null,

	@field:SerializedName("html_defaults")
	val htmlDefaults: SocialMediaTemplateHtmlDefaultsResponse? = null,

	@field:SerializedName("template_id")
	val templateId: Int = 0,

	@field:SerializedName("template_type")
	val templateType: String? = null,

	@field:SerializedName("is_edited")
	val isEdited: Boolean = false
)
