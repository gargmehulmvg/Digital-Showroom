package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class LockedStoreShareResponse(

	@field:SerializedName("heading")
	val heading: String? = null,

	@field:SerializedName("sub_heading")
	val subHeading: String? = null,

	@field:SerializedName("store_domain")
	val storeDomain: String? = null,

	@field:SerializedName("message")
	val message: String? = null
)
