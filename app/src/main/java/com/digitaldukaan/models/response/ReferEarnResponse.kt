package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ReferAndEarnResponse(
    @SerializedName("heading_refer_bottom_sheet")   var heading: String?,
    @SerializedName("heading_refer_bottom_sheet2")  var heading2: String?,
    @SerializedName("message_refer_bottom_sheet")   var message: String?,
    @SerializedName("text_refer_now")               var referNow: String?
)