package com.digitaldukaan.models.dto

import com.google.gson.annotations.SerializedName

class ContactModel(
    @SerializedName("name") var name: String? = null,
    @SerializedName("number") var number: String? = null
)