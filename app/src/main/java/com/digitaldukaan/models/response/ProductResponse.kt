package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ProductResponse(

	@field:SerializedName("store_id")
	val storeId: Int = 0,

	@field:SerializedName("category")
	val category: StoreCategoryItem? = null,

	@field:SerializedName("variants_count")
	val variantsCount: Int = 0,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("available")
	val available: Int = 0,

	@field:SerializedName("has_images")
	val hasImages: Int = 0,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("thumbnail_url")
	val thumbnailUrl: String? = null,

	@field:SerializedName("discounted_price")
	val discountedPrice: Double = 0.0,

	@field:SerializedName("price")
	val price: Double = 0.0,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("id")
	val id: Int = 0
)
