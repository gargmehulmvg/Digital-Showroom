package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class GenerateOtpRequest(
    @SerializedName("comm_medium") var commMedium: Int
)