package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class BusinessTypeRequest(
    @SerializedName("store_id") var storeId: Int,
    @SerializedName("businesses") var businessList: ArrayList<Int>
)