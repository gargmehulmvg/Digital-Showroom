package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class DeleteItemRequest(
    @SerializedName("store_item_id") var id: Int?
)