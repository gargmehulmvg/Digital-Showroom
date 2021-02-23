package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class StaticTextResponse(
    @SerializedName("status") var mIsSuccessStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("data") var mStaticData: StaticData
)

data class StaticData(
    @SerializedName("auth_new") var mAuthNew: AuthNewResponseData,
    @SerializedName("verify_otp") var mVerifyOtpStaticData: VerifyOtpStaticResponseData,
    @SerializedName("onbording_step_one") var mOnBoardStep1StaticData: OnBoardStep1StaticResponseData,
    @SerializedName("onboarding_step_two") var mOnBoardStep2StaticData: OnBoardStep2StaticResponseData,
    @SerializedName("settings") var mSettingsStaticData: SettingStaticData
)

data class SettingStaticData(
    @SerializedName("app_version_text") var mAppVersionText: String?,
    @SerializedName("logout_body") var mLogoutBody: String?,
    @SerializedName("logout_text") var mLogoutText: String?,
    @SerializedName("logout_title") var mLogoutTitle: String?,
    @SerializedName("best_view_text") var mBestViewedText: String?,
    @SerializedName("bottom_sheet_text") var mBottomSheetText: String?,
    @SerializedName("store_id") var mStoreId: String?
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