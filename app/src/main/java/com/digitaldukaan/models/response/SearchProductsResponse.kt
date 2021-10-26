package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class SearchProductsResponse(

	@field:SerializedName("items")
	val productList: ArrayList<ProductResponse> = ArrayList(),

	@field:SerializedName("next")
	val next: Int? = 0
)
