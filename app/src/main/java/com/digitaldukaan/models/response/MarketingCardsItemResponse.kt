package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class MarketingCardsItemResponse(
    @SerializedName("heading") var heading: String?,
    @SerializedName("sub_heading") var subHeading: String?,
    @SerializedName("info_text") var infoText: String?,
    @SerializedName("logo") var logo: String?,
    @SerializedName("action") var action: String?,
    @SerializedName("color") var color: String?,
    @SerializedName("page_url") var pageUrl: String?,
    @SerializedName("free") var free: String?,
    @SerializedName("price") var price: String?,
    @SerializedName("offer_time") var offerTime: String?,
    @SerializedName("type") var type: String?,
    @SerializedName("text") var text: String?,
    @SerializedName("strip_text") var stripText: String?,
    @SerializedName("view_now") var viewNow: String?,
    @SerializedName("lock_text") var lockText: String?,
    @SerializedName("active_flag") var isActiveFlagEnable: Boolean?
)
