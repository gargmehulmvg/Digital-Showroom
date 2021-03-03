package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ReferEarnResponse(
    @SerializedName("status") var mStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("data") var mReferAndEarnData: ReferAndEarnItemResponse,
    @SerializedName("is_offer_valid") var mIsOfferApplicable: Boolean?
)

data class ReferAndEarnItemResponse(
    @SerializedName("heading1") var heading1: String?,
    @SerializedName("heading2") var heading2: String?,
    @SerializedName("image_url") var imageUrl: String?,
    @SerializedName("settings_txt") var settingsTxt: String?,
    @SerializedName("work_journey") var workJourneyList: ArrayList<WorkJourneyItemResponse>?,
    @SerializedName("is_offer_valid") var mErrorString: String?
)


data class WorkJourneyItemResponse(
    @SerializedName("title") var title: String?,
    @SerializedName("amount") var amount: Int,
    @SerializedName("is_fixed") var mIsFixed: Boolean?
)