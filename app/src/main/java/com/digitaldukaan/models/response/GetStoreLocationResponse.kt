package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class GetStoreLocationResponse(

	@field:SerializedName("static_text")
	var mMapStaticData: MapLocationStaticResponseData,

	@field:SerializedName("address")
	var storeAddress: UserAddressResponse?
)
