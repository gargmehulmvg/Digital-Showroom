package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class CreateStoreResponse(
    @SerializedName("store_id") var storeId: Int,
    @SerializedName("store_info") var storeInfo: UserStoreInfoResponse?
)