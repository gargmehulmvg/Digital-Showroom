package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName
import java.util.*

data class StaticTextResponse(
    @SerializedName("auth_new")                         var mAuthNew: AuthNewResponseData,
    @SerializedName("login")                            var mLoginStaticText: LoginStaticText,
    @SerializedName("business_name")                    var mBusinessNameStaticText: BusinessNameStaticText,
    @SerializedName("profile")                          var mProfileStaticData: ProfileStaticData,
    @SerializedName("verify_otp")                       var mVerifyOtpStaticData: VerifyOtpStaticResponseData,
    @SerializedName("onbording_step_one")               var mOnBoardStep1StaticData: OnBoardStep1StaticResponseData,
    @SerializedName("onboarding_step_two")              var mOnBoardStep2StaticData: OnBoardStep2StaticResponseData,
    @SerializedName("merchant_address")                 var mMapStaticData: MapLocationStaticResponseData,
    @SerializedName("subscription_lock")                var mSubscriptionLockStaticData: SubscriptionLockStaticData,
    @SerializedName("catalog_add")                      var mCatalogStaticData: CatalogStaticData,
    @SerializedName("otp_modes_list")                   var mOtpModesList: ArrayList<CommonCtaResponse>
)

data class ProfileStaticData(
    @SerializedName("save_changes_text")                var saveChanges: String?,
    @SerializedName("store_description_hint_text")      var storeDescriptionHint: String?
)

data class CatalogStaticData(
    @SerializedName("upload_img_heading")               var uploadImageHeading: String?,
    @SerializedName("take_photo")                       var takePhoto: String?,
    @SerializedName("search_img")                       var searchImageSubTitle: String?,
    @SerializedName("remove_image")                     var removeImageText: String?,
    @SerializedName("search_img_placeholder")           var searchImageHint: String?,
    @SerializedName("add_gallery")                      var addGallery: String?
)

data class MapLocationStaticResponseData(
    @SerializedName("address_error")                    var addressError: String?,
    @SerializedName("city_text_hint")                   var cityTextHint: String?,
    @SerializedName("complete_address_text_hint")       var completeAddressHint: String?,
    @SerializedName("enable_location")                  var enableLocation: String?,
    @SerializedName("pin_drag_text")                    var pinDragText: String?,
    @SerializedName("pincode_text_hint")                var pinCodeTextHint: String?,
    @SerializedName("save_changes_text")                var saveChangesText: String?,
    @SerializedName("set_location_text")                var setLocationText: String?,
    @SerializedName("store_location_text")              var storeLocationText: String?
)

data class AuthNewResponseData(
    @SerializedName("btn_txt")                          var mButtonText: String?,
    @SerializedName("input_txt")                        var mInputText: String?,
    @SerializedName("sub_heading")                      var mSubHeadingText: String?,
    @SerializedName("heading")                          var mHeadingText: String?
)

data class BusinessNameStaticText(
    @SerializedName("heading_page")                     var heading_page: String?,
    @SerializedName("hint_enter_here")                  var hint_enter_here: String?,
    @SerializedName("cta_text")                         var cta_text: String?,
    @SerializedName("error_mandatory_field")            var error_mandatory_field: String?,
    @SerializedName("message_create_store_success")     var message_create_store_success: String?,
    @SerializedName("sub_heading_page")                 var sub_heading_page: String?
)

data class LoginStaticText(
    @SerializedName("hint_enter_whatsapp_number")       var hint_enter_whatsapp_number: String?,
    @SerializedName("text_get_otp")                     var text_get_otp: String?,
    @SerializedName("text_or")                          var text_or: String?,
    @SerializedName("error_mandatory_field")            var error_mandatory_field: String?,
    @SerializedName("error_mobile_number_not_valid")    var error_mobile_number_not_valid: String?,
    @SerializedName("text_login_with_truecaller")       var text_login_with_truecaller: String?
)

data class VerifyOtpStaticResponseData(
    @SerializedName("alert_text")                       var mAlertText: String?,
    @SerializedName("heading")                          var mHeadingText: String?,
    @SerializedName("otp_verified_text")                var mOtpVerifiedText: String?,
    @SerializedName("resend_btn_text")                  var mResendButtonText: String?,
    @SerializedName("sub_heading")                      var mSubHeadingText: String?,
    @SerializedName("verify_text")                      var mVerifyText: String?,
    @SerializedName("verifying_text")                   var mVerifyingText: String?,
    @SerializedName("text_ok")                          var mTextOk: String?,
    @SerializedName("text_read_more")                   var mReadMore: String?,
    @SerializedName("message_consent")                  var mConsentMessage: String?,
    @SerializedName("heading_consent")                  var mHeadingMessage: String?,
    @SerializedName("seconds_text")                     var mSecondText: String?,
    @SerializedName("text_change_caps")                 var text_change_caps: String?,
    @SerializedName("text_send_again")                  var text_send_again: String?,
    @SerializedName("text_verified")                    var text_verified: String?,
    @SerializedName("heading_otp_sent_to")              var heading_otp_sent_to: String?,
    @SerializedName("text_did_not_receive_otp")         var text_did_not_receive_otp: String?,
    @SerializedName("error_mandatory_field")            var error_mandatory_field: String?,
    @SerializedName("error_otp_not_valid")              var error_otp_not_valid: String?,
    @SerializedName("hint_enter_4_digit_otp")           var hint_enter_4_digit_otp: String?,
    @SerializedName("text_resend_otp")                  var text_resend_otp: String?,
    @SerializedName("text_sent_on")                     var text_sent_on: String?
)

data class OnBoardStep1StaticResponseData(
    @SerializedName("cancel_text")                      var mCancelText: String?,
    @SerializedName("confirm_exit_text")                var mConfirmExitText: String?,
    @SerializedName("dukaan_name_text")                 var mDukaanName: String?,
    @SerializedName("error_alert_text")                 var mErrorAlertText: String?,
    @SerializedName("exit_text")                        var mExitText: String?,
    @SerializedName("next_btn_text")                    var mNextButton: String?,
    @SerializedName("no_text")                          var mNoText: String?,
    @SerializedName("title_and_hint_text")              var mTitleHinText: String?,
    @SerializedName("valid_phone_alert_text")           var mValidPhoneAlert: String?,
    @SerializedName("yes_text")                         var mYesText: String?,
    @SerializedName("step_count")                       var mStepCount: String?
)

data class OnBoardStep2StaticResponseData(
    @SerializedName("heading")                          var mHeading: String?,
    @SerializedName("next_btn_text")                    var mNextButton: String?,
    @SerializedName("step_count")                       var mStepCount: String?,
    @SerializedName("title_and_hint_text")              var mTitleHinText: String?
)

data class SubscriptionLockStaticData(
    @SerializedName("heading")      var heading: String?,
    @SerializedName("message")      var message: String?,
    @SerializedName("cta_text")     var ctaText: String?,
    @SerializedName("page_url")     var pageUrl: String?
)