package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ProfilePreviewResponse(
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("profile_info") var mProfileInfo: ProfileInfoResponse
)

data class ProfileInfoResponse(
    @SerializedName("store_logo") var mStoreLogo: String?,
    @SerializedName("phone_number") var mPhoneNumber: String?,
    @SerializedName("completed_steps") var mCompletedSteps: String?,
    @SerializedName("total_steps") var mTotalSteps: String?,
    @SerializedName("is_profile_completed") var mIsProfileCompleted: Boolean,
    @SerializedName("banner") var mProfilePreviewBanner: ProfilePreviewBannerResponse,
    @SerializedName("setting_keys") var mSettingsKeysList: ArrayList<ProfilePreviewSettingsKeyResponse>
)

data class ProfilePreviewBannerResponse(
    @SerializedName("cdn") var mCDN: String?,
    @SerializedName("heading") var mHeading: String?,
    @SerializedName("sub_heading") var mSubHeading: String?,
    @SerializedName("start_now") var mStartNow: String?
)

data class ProfilePreviewSettingsKeyResponse(
    @SerializedName("heading_text") var mHeadingText: String?,
    @SerializedName("value") var mValue: String?,
    @SerializedName("default_text") var mDefaultText: String?,
    @SerializedName("is_editable") var mIsEditable: Boolean?,
    @SerializedName("action") var mAction: String?,
    @SerializedName("lock_text") var mLockText: String?
)