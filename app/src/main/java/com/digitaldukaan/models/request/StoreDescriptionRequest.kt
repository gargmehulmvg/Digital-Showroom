package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class StoreDescriptionRequest(
    @SerializedName("description") var description: String
)