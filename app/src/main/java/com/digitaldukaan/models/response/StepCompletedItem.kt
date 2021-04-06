package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class StepCompletedItem(
    @SerializedName("is_completed") var isCompleted: Boolean,
    @SerializedName("action") var action: String?
)