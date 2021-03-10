package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ProfilePreviewResponse(
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("error_type") var mErrorType: String?,
    @SerializedName("data") var mProfileDataStr: String
)

data class ProfileInfoResponse(
    @SerializedName("store") var mStoreItemResponse: StoreResponse?,
    @SerializedName("is_profile_completed") var mIsProfileCompleted: Boolean,
    @SerializedName("banner") var mProfilePreviewBanner: ProfilePreviewBannerResponse,
    @SerializedName("setting_keys") var mSettingsKeysList: ArrayList<ProfilePreviewSettingsKeyResponse>,
    @SerializedName("total_steps") var mTotalSteps: String?,
    @SerializedName("completed_steps") var mCompletedSteps: String?,
    @SerializedName("static_text") var mProfileStaticText: ProfileStaticTextResponse
)

data class ProfileStaticTextResponse(
    @SerializedName("page_heading") var pageHeading: String?,
    @SerializedName("text_save_change") var saveChanges: String?,
    @SerializedName("text_save") var saveText: String?,
    @SerializedName("hint_store_description_text") var storeDescriptionHint: String?,
    @SerializedName("warning_one") var mStoreLinkChangeWarningOne: String?,
    @SerializedName("warning_two") var mStoreLinkChangeWarningTwo: String?,
    @SerializedName("text_confirmation") var mStoreLinkChangeDialogHeading: String?,
    @SerializedName("edit_store_link") var editStoreLink: String?,
    @SerializedName("text_edit_store_link") var editStoreLinkText: String?,
    @SerializedName("current_link") var currentLink: String?,
    @SerializedName("text_d_dash") var dText: String?,
    @SerializedName("text_dotpe_dot_in") var dotPeDotInText: String?,
    @SerializedName("store_link_condition_one") var storeLinkConditionOne: String?,
    @SerializedName("store_link_condition_two") var storeLinkConditionTwo: String?,
    @SerializedName("store_link_title") var storeLinkTitle: String?,
    @SerializedName("text_yes") var mYesText: String?,
    @SerializedName("text_update_store_name") var mBottomSheetStoreNameHeading: String?,
    @SerializedName("text_confirm") var mBottomSheetStoreButtonText: String?,
    @SerializedName("text_confirm_desc_lose_changes") var mBottomSheetCloseConfirmationMessage: String?,
    @SerializedName("error_domain_already_exists") var mDomainAlreadyExistError: String?,
    @SerializedName("error_domain_unavailable") var mDomainUnAvailableError: String?,
    @SerializedName("text_no") var mNoText: String?
)

data class ProfilePreviewBannerResponse(
    @SerializedName("image_url") var mCDN: String?,
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
    @SerializedName("design") var mDesign: String?,
    @SerializedName("lock_text") var mLockText: String?
)