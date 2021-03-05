package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class StaticTextResponse(
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("data") var mStaticData: StaticData
)

data class StaticData(
    @SerializedName("auth_new") var mAuthNew: AuthNewResponseData,
    @SerializedName("profile") var mProfileStaticData: ProfileStaticData,
    @SerializedName("verify_otp") var mVerifyOtpStaticData: VerifyOtpStaticResponseData,
    @SerializedName("onbording_step_one") var mOnBoardStep1StaticData: OnBoardStep1StaticResponseData,
    @SerializedName("onboarding_step_two") var mOnBoardStep2StaticData: OnBoardStep2StaticResponseData,
    @SerializedName("merchant_address") var mMapStaticData: MapLocationStaticResponseData,
    @SerializedName("catalog_add") var mCatalogStaticData: CatalogStaticData,
    @SerializedName("marketing") var mMarketingStaticData: MarketingStaticData,
    @SerializedName("settings") var mSettingsStaticData: SettingStaticData
)

data class ProfileStaticData(
    @SerializedName("add_store_description_text") var addStoreDescription: String?,
    @SerializedName("default_store_description_text") var defaultStoreDescriptionText: String?,
    @SerializedName("page_heading") var pageHeading: String?,
    @SerializedName("save_change_text") var saveChanges: String?,
    @SerializedName("save_text") var saveText: String?,
    @SerializedName("store_description_text") var storeDescriptionText: String?,
    @SerializedName("store_description_title_text") var storeDescriptionTitleText: String?,
    @SerializedName("store_desction_hint_text") var storeDescriptionHint: String?,
    @SerializedName("warning_one") var mStoreLinkChangeWarningOne: String?,
    @SerializedName("warning_two") var mStoreLinkChangeWarningTwo: String?,
    @SerializedName("conform_text") var mStoreLinkChangeDialogHeading: String?,
    @SerializedName("edit_store_link") var editStoreLink: String?,
    @SerializedName("edit_store_link_text") var editStoreLinkText: String?,
    @SerializedName("current_link") var currentLink: String?,
    @SerializedName("d_text") var dText: String?,
    @SerializedName("dotpe_dot_in_text") var dotPeDotInText: String?,
    @SerializedName("store_link_condition_one") var storeLinkConditionOne: String?,
    @SerializedName("store_link_condition_two") var storeLinkConditionTwo: String?,
    @SerializedName("store_link_not_found") var storeLinkNotFound: String?,
    @SerializedName("store_link_title") var storeLinkTitle: String?,
    @SerializedName("yes_text") var mYesText: String?,
    @SerializedName("update_store_text") var mBottomSheetStoreNameHeading: String?,
    @SerializedName("confirm_text") var mBottomSheetStoreButtonText: String?,
    @SerializedName("conform_desc_text") var mBottomSheetCloseConfirmationMessage: String?,
    @SerializedName("domain_already_exists_error") var mDomainAlreadyExistError: String?,
    @SerializedName("domain_unavailable_error") var mDomainUnAvailableError: String?,
    @SerializedName("no_text") var mNoText: String?
)

data class MarketingStaticData(
    @SerializedName("page_heading") var pageHeading: String?
)

data class CatalogStaticData(
    @SerializedName("upload_img_heading") var uploadImageHeading: String?,
    @SerializedName("take_photo") var takePhoto: String?,
    @SerializedName("search_img") var searchImageSubTitle: String?,
    @SerializedName("remove_image") var removeImageText: String?,
    @SerializedName("search_img_placeholder") var searchImageHint: String?,
    @SerializedName("add_gallery") var addGallery: String?
)

data class SettingStaticData(
    @SerializedName("app_version_text") var mAppVersionText: String?,
    @SerializedName("logout_body") var mLogoutBody: String?,
    @SerializedName("logout_text") var mLogoutText: String?,
    @SerializedName("logout_title") var mLogoutTitle: String?,
    @SerializedName("best_view_text") var mBestViewedText: String?,
    @SerializedName("bottom_sheet_text") var mBottomSheetText: String?,
    @SerializedName("delivery_text") var mDeliveryText: String?,
    @SerializedName("store_txt") var mStoreText: String?,
    @SerializedName("off_text") var mOffText: String?,
    @SerializedName("on_text") var mOnText: String?,
    @SerializedName("store_id") var mStoreId: String?
)

data class MapLocationStaticResponseData(
    @SerializedName("address_error") var addressError: String?,
    @SerializedName("city_text_hint") var cityTextHint: String?,
    @SerializedName("complete_address_text_hint") var completeAddressHint: String?,
    @SerializedName("enable_location") var enableLocation: String?,
    @SerializedName("pin_drag_text") var pinDragText: String?,
    @SerializedName("pincode_text_hint") var pinCodeTextHint: String?,
    @SerializedName("save_changes_text") var saveChangesText: String?,
    @SerializedName("set_location_text") var setLocationText: String?,
    @SerializedName("store_location_text") var storeLocationText: String?
)

data class AuthNewResponseData(
    @SerializedName("btn_txt") var mButtonText: String?,
    @SerializedName("input_txt") var mInputText: String?,
    @SerializedName("sub_heading") var mSubHeadingText: String?,
    @SerializedName("heading") var mHeadingText: String?
)

data class VerifyOtpStaticResponseData(
    @SerializedName("alert_text") var mAlertText: String?,
    @SerializedName("heading") var mHeadingText: String?,
    @SerializedName("otp_verified_text") var mOtpVerifiedText: String?,
    @SerializedName("resend_btn_text") var mResendButtonText: String?,
    @SerializedName("sub_heading") var mSubHeadingText: String?,
    @SerializedName("verify_text") var mVerifyText: String?,
    @SerializedName("verifying_text") var mVerifyingText: String?,
    @SerializedName("seconds_text") var mSecondText: String?
)

data class OnBoardStep1StaticResponseData(
    @SerializedName("cancel_text") var mCancelText: String?,
    @SerializedName("confirm_exit_text") var mConfirmExitText: String?,
    @SerializedName("dukaan_name_text") var mDukaanName: String?,
    @SerializedName("error_alert_text") var mErrorAlertText: String?,
    @SerializedName("exit_text") var mExitText: String?,
    @SerializedName("next_btn_text") var mNextButton: String?,
    @SerializedName("no_text") var mNoText: String?,
    @SerializedName("title_and_hint_text") var mTitleHinText: String?,
    @SerializedName("valid_phone_alert_text") var mValidPhoneAlert: String?,
    @SerializedName("yes_text") var mYesText: String?,
    @SerializedName("step_count") var mStepCount: String?
)

data class OnBoardStep2StaticResponseData(
    @SerializedName("heading") var mHeading: String?,
    @SerializedName("next_btn_text") var mNextButton: String?,
    @SerializedName("step_count") var mStepCount: String?,
    @SerializedName("title_and_hint_text") var mTitleHinText: String?
)