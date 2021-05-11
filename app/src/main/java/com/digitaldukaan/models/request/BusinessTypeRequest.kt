package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class BusinessTypeRequest(
    @SerializedName("businesses") var businessList: ArrayList<Int>
)