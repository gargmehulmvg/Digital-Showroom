package com.digitaldukaan.models.request

import com.google.gson.annotations.SerializedName

data class AndroidEventLogRequest(
    @SerializedName("store_id")         var storeId: Int,
    @SerializedName("event_name")       var eventName: String?,
    @SerializedName("data")             var data: Map<String, String?>
)