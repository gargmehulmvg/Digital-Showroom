package com.digitaldukaan.models.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("status") var mStatus: Boolean,
    @SerializedName("message") var mMessage: String?,
    @SerializedName("error_type") var mErrorType: String?,
    @SerializedName("account_information") var mAccountInfoResponse: AccountInfoResponse?
)

data class AccountInfoResponse(
    @SerializedName("StoreInfo") var mStoreInfo: StoreInfoResponse,
    @SerializedName("store_share") var mStoreShare: StoreShareResponse?,
    @SerializedName("trending_list") var mTrendingList: ArrayList<TrendingListResponse>?,
    @SerializedName("store_options") var mStoreOptions: ArrayList<StoreOptionsResponse>?,
    @SerializedName("sub_pages") var mSubPages: ArrayList<SubPagesResponse>?,
    @SerializedName("total_steps") var mTotalSteps: String?,
    @SerializedName("completed_steps") var mCompletedSteps: String?,
    @SerializedName("account_static_text") var mAccountStaticText: AccountStaticTextResponse?,
    @SerializedName("footer_images") var mFooterImages: ArrayList<String>?
)

data class StoreInfoResponse(
    @SerializedName("store_id") var mStoreId: Int?,
    @SerializedName("store_name") var mStoreName: String?,
    @SerializedName("store_type") var mStoreType: String?,
    @SerializedName("domain") var mDomain: String?,
    @SerializedName("store_url") var mStoreUrl: String?,
    @SerializedName("services") var mStoreService: StoreServicesResponse?
)

data class StoreServicesResponse(
    @SerializedName("store_id") var mStoreId: Int?,
    @SerializedName("store_flag") var mStoreFlag: Int?,
    @SerializedName("delivery_flag") var mDeliveryFlag: Int?,
    @SerializedName("pickup_flag") var mPickupFlag: Int?,
    @SerializedName("listing_flag") var mListingFlag: Int?,
    @SerializedName("min_order_value") var mMinOrderFlag: Int?,
    @SerializedName("delivery_charge_type") var mDeliveryChargeType: Int?,
    @SerializedName("free_delivery_above") var mFreeDeliveryAbove: Int?
)

data class AccountStaticTextResponse(
    @SerializedName("complete_profile") var mCompleteProfile: String?,
    @SerializedName("step_left") var mStepLeft: String?,
    @SerializedName("steps_left") var mStepsLeft: String?
)

data class StoreShareResponse(
    @SerializedName("wa_text") var mWaText: String?,
    @SerializedName("image_url") var mImageUrl: String?,
    @SerializedName("share_store_banner") var mShareStoreBanner: String?
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
    @SerializedName("show_more") var mIsShowMore: Boolean?,
    @SerializedName("page") var mPage: String?
)

data class TrendingListResponse(
    @SerializedName("cdn") var mCDN: String?,
    @SerializedName("text") var mText: String?,
    @SerializedName("type") var mType: String?,
    @SerializedName("action") var mAction: String?,
    @SerializedName("lock_text") var mLockText: String?,
    @SerializedName("page") var mPage: String?
)