package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ProfileInfoResponse(
    @SerializedName("store") var mStoreItemResponse: StoreResponse?,
    @SerializedName("is_profile_completed") var mIsProfileCompleted: Boolean,
    @SerializedName("banner") var mProfilePreviewBanner: ProfilePreviewBannerResponse,
    @SerializedName("setting_keys") var mSettingsKeysList: ArrayList<ProfilePreviewSettingsKeyResponse>,
    @SerializedName("total_steps") var mTotalSteps: String?,
    @SerializedName("steps") var mStepsList: ArrayList<StepCompletedItem>?,
    @SerializedName("completed_steps") var mCompletedSteps: String?,
    @SerializedName("profile_banner") var mBannerList: ArrayList<HomePageBannerResponse>,
    @SerializedName("static_text") var mProfileStaticText: ProfileStaticTextResponse
)