package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ProfilePreviewBannerResponse(
    @SerializedName("image_url") var mCDN: String?,
    @SerializedName("heading") var mHeading: String?,
    @SerializedName("sub_heading") var mSubHeading: String?,
    @SerializedName("start_now") var mStartNow: String?
)