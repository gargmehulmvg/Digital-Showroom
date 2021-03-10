package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("status") var mStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("error_type") var mErrorType: String?,
    @SerializedName("account_information") var mAccountInfoResponse: AccountInfoResponse?
)

data class AccountInfoResponse(
    @SerializedName("store") var mStoreInfo: StoreResponse,
    @SerializedName("store_share") var mStoreShare: StoreShareResponse?,
    @SerializedName("wa_share") var waShare: String?,
    @SerializedName("new_releases") var mTrendingList: ArrayList<TrendingListResponse>?,
    @SerializedName("store_options") var mStoreOptions: ArrayList<StoreOptionsResponse>?,
    @SerializedName("app_settings") var mSubPages: ArrayList<SubPagesResponse>?,
    @SerializedName("total_steps") var mTotalSteps: Int?,
    @SerializedName("completed_steps") var mCompletedSteps: Int,
    @SerializedName("static_text") var mAccountStaticText: AccountStaticTextResponse?,
    @SerializedName("footer_images") var mFooterImages: ArrayList<String>?
)

data class StoreInfoResponse(
    @SerializedName("store_id") var mStoreId: Int?,
    @SerializedName("store_name") var mStoreName: String?,
    @SerializedName("store_type") var mStoreType: String?,
    @SerializedName("domain") var mDomain: String?,
    @SerializedName("store_url") var mStoreUrl: String?,
    @SerializedName("logo_image") var mStoreLogoStr: String?,
    @SerializedName("address") var mStoreAddress: UserAddressResponse?,
    @SerializedName("store_businesses") var mStoreBusinessTypeList: ArrayList<BusinessTypeItemResponse>?,
    @SerializedName("services") var mStoreService: StoreServicesResponse?
)

data class StoreServicesResponse(
    @SerializedName("store_id") var mStoreId: Int?,
    @SerializedName("store_flag") var mStoreFlag: Int?,
    @SerializedName("delivery_flag") var mDeliveryFlag: Int?,
    @SerializedName("pickup_flag") var mPickupFlag: Int?,
    @SerializedName("listing_flag") var mListingFlag: Int?,
    @SerializedName("min_order_value") var mMinOrderValue: Double?,
    @SerializedName("delivery_price") var mDeliveryPrice: Double?,
    @SerializedName("delivery_charge_type") var mDeliveryChargeType: Int?,
    @SerializedName("free_delivery_above") var mFreeDeliveryAbove: Double?
)

data class AccountStaticTextResponse(
    @SerializedName("complete_profile") var mCompleteProfile: String?,
    @SerializedName("step_left") var mStepLeft: String?,
    @SerializedName("steps_left") var mStepsLeft: String?,
    @SerializedName("bottom_sheet_hint") var bottom_sheet_hint: String?,
    @SerializedName("bottom_sheet_save_changes") var bottom_sheet_save_changes: String?,
    @SerializedName("custom_delivery_charge_description") var custom_delivery_charge_description: String?,
    @SerializedName("error_amount_must_greater_than_min_order_value") var error_amount_must_greater_than_min_order_value: String?,
    @SerializedName("error_amount_must_greater_than_free_delivery_above") var error_amount_must_greater_than_free_delivery_above: String?,
    @SerializedName("error_amount_must_greater_than_zero") var error_amount_must_greater_than_zero: String?,
    @SerializedName("error_mandatory_field") var error_mandatory_field: String?,
    @SerializedName("error_store_link_bottom_sheet_one") var error_store_link_bottom_sheet_one: String?,
    @SerializedName("error_store_link_bottom_sheet_two") var error_store_link_bottom_sheet_two: String?,
    @SerializedName("error_store_name_empty") var error_store_name_empty: String?,
    @SerializedName("free_delivery_description") var free_delivery_description: String?,
    @SerializedName("heading_custom_delivery_charge") var heading_custom_delivery_charge: String?,
    @SerializedName("heading_fixed_delivery_charge") var heading_fixed_delivery_charge: String?,
    @SerializedName("heading_free_delivery") var heading_free_delivery: String?,
    @SerializedName("heading_set_delivery_charge") var heading_set_delivery_charge: String?,
    @SerializedName("heading_set_min_order_value_for_delivery") var heading_set_min_order_value_for_delivery: String?,
    @SerializedName("heading_store_link_bottom_sheet") var heading_store_link_bottom_sheet: String?,
    @SerializedName("heading_store_link_dialog") var heading_store_link_dialog: String?,
    @SerializedName("heading_store_name_bottom_sheet") var heading_store_name_bottom_sheet: String?,
    @SerializedName("hint_custom_delivery_charge") var hint_custom_delivery_charge: String?,
    @SerializedName("hint_free_delivery_above_optional") var hint_free_delivery_above_optional: String?,
    @SerializedName("hint_free_delivery_charge") var hint_free_delivery_charge: String?,
    @SerializedName("hint_store_name_bottom_sheet") var hint_store_name_bottom_sheet: String?,
    @SerializedName("message_store_link_dialog_one") var message_store_link_dialog_one: String?,
    @SerializedName("message_store_link_dialog_two") var message_store_link_dialog_two: String?,
    @SerializedName("page_heading") var page_heading: String?,
    @SerializedName("page_heading_more_controls") var page_heading_more_controls: String?,
    @SerializedName("page_heading_set_delivery_charge") var page_heading_set_delivery_charge: String?,
    @SerializedName("save_changes") var save_changes: String?,
    @SerializedName("sub_heading_set_delivery_charge") var sub_heading_set_delivery_charge: String?,
    @SerializedName("sub_heading_set_min_order_value_for_delivery") var sub_heading_set_min_order_value_for_delivery: String?,
    @SerializedName("sub_heading_success_set_delivery_charge") var sub_heading_success_set_delivery_charge: String?,
    @SerializedName("sub_heading_success_set_delivery_charge_amount") var sub_heading_success_set_delivery_charge_amount: String?,
    @SerializedName("sub_heading_success_set_min_order_value_for_delivery") var sub_heading_success_set_min_order_value_for_delivery: String?,
    @SerializedName("heading_edit_min_order_value") var heading_edit_min_order_value: String?,
    @SerializedName("text_confirm") var text_confirm: String?,
    @SerializedName("text_no") var text_no: String?,
    @SerializedName("text_optional") var text_optional: String?,
    @SerializedName("text_yes") var text_yes: String?,
    @SerializedName("text_ruppee_symbol") var text_ruppee_symbol: String?,
    @SerializedName("bottom_sheet_heading") var bottom_sheet_heading: String?
)

data class StoreShareResponse(
    @SerializedName("wa_text") var mWaText: String?,
    @SerializedName("image_url") var mImageUrl: String?,
    @SerializedName("share_store_banner") var mShareStoreBanner: Boolean
)

data class SubPagesResponse(
    @SerializedName("logo") var mLogo: String?,
    @SerializedName("text") var mText: String?,
    @SerializedName("action") var mAction: String?,
    @SerializedName("page") var mPage: String?
)

data class StoreOptionsResponse(
    @SerializedName("logo") var mLogo: String?,
    @SerializedName("text") var mText: String?,
    @SerializedName("banner_text") var mBannerText: String?,
    @SerializedName("action") var mAction: String?,
    @SerializedName("show_more") var mIsShowMore: Boolean,
    @SerializedName("page") var mPage: String?
)

data class TrendingListResponse(
    @SerializedName("image_url") var mCDN: String?,
    @SerializedName("text") var mText: String?,
    @SerializedName("action") var mAction: String?,
    @SerializedName("type") var mType: String?,
    @SerializedName("lock_text") var mLockText: String?,
    @SerializedName("page") var mPage: String?
)