package com.digitaldukaan.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.NewReleaseAdapter
import com.digitaldukaan.adapters.ProfileStatusAdapter
import com.digitaldukaan.adapters.SettingsStoreAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.interfaces.IStoreSettingsItemClicked
import com.digitaldukaan.models.request.StoreLogoRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.ProfileService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProfileServiceInterface
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_settings_fragment.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class SettingsFragment : BaseFragment(), IOnToolbarIconClick, IProfileServiceInterface,
    SwipeRefreshLayout.OnRefreshListener, IStoreSettingsItemClicked {

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }

    private var mAppSettingsResponseStaticData: AccountStaticTextResponse? = null
    private var mAppStoreServicesResponse: StoreServicesResponse? = null
    private val mProfileService = ProfileService()
    private var mReferAndEarnResponse: ReferAndEarnResponse? = null
    private var mAccountInfoResponse: AccountInfoResponse? = null
    private var mStoreLogo: String? = ""
    private var mShareDataOverWhatsAppText = ""
    private var mReferEarnOverWhatsAppResponse: ReferEarnOverWhatsAppResponse? = null
    private var mAccountPageInfoResponse: AccountInfoResponse? = null
    private var mNewReleaseItemClickResponse: TrendingListResponse? = null
    private var mIsDoublePressToExit = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "SettingsFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_settings_fragment, container, false)
        mProfileService.setProfileServiceInterface(this)
        return mContentView
    }

    override fun onStart() {
        super.onStart()
        ToolBarManager.getInstance().apply {
            setSideIconVisibility(true)
            mActivity?.let { setSideIcon(ContextCompat.getDrawable(it, R.drawable.ic_setting_toolbar), this@SettingsFragment) }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateNavigationBarState(R.id.menuSettings)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            onBackPressed(this@SettingsFragment)
            setSideIconVisibility(true)
            hideBackPressFromToolBar(mActivity, false)
            mActivity?.let { setSideIcon(ContextCompat.getDrawable(it, R.drawable.ic_setting_toolbar), this@SettingsFragment) }
            setSecondSideIcon(null, null)
            setSecondSideIconVisibility(false)
        }
        hideBottomNavigationView(false)
        swipeRefreshLayout?.setOnRefreshListener(this)
        mStoreLogo = ""
        fetchUserProfile()
        setupLockedPermissionUI()
    }

    private fun setupLockedPermissionUI() {
        mActivity?.let { context ->
            imageView5?.setImageDrawable(ContextCompat.getDrawable(context, if (false == StaticInstances.sPermissionHashMap?.get(Constants.STORE_CONTROLS)) R.drawable.ic_subscription_locked_black_small else R.drawable.ic_half_arrow_forward))
            shapeableImageView?.setImageDrawable(ContextCompat.getDrawable(context, if (false == StaticInstances.sPermissionHashMap?.get(Constants.STORE_PROFILE)) R.drawable.ic_subscription_locked_black_small else R.drawable.round_black_background))
            editProfileTextView?.apply {
                setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, if (false == StaticInstances.sPermissionHashMap?.get(Constants.STORE_PROFILE)) R.drawable.ic_subscription_locked_black_small else R.drawable.ic_round_green_arrow_small, 0)
                setTextColor(ContextCompat.getColor(context, if (true == StaticInstances.sPermissionHashMap?.get(Constants.STORE_PROFILE)) R.color.open_green else R.color.black))
            }
            lockedProfilePhotoGroup?.visibility = if (true == StaticInstances.sPermissionHashMap?.get(Constants.STORE_LOGO)) View.GONE else View.VISIBLE
        }
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        StaticInstances.sAccountPageSettingsStaticData = mAppSettingsResponseStaticData
        StaticInstances.sAppStoreServicesResponse = mAppStoreServicesResponse
        StaticInstances.sPaymentMethodStr = mAccountPageInfoResponse?.mOnlinePaymentType
        when (view?.id) {
            storeControlView?.id -> {
                if (false == StaticInstances.sPermissionHashMap?.get(Constants.STORE_CONTROLS)) {
                    showStaffFeatureLockedBottomSheet(Constants.NAV_BAR_SETTINGS)
                    return
                }
                launchFragment(MoreControlsFragment.newInstance(mAccountPageInfoResponse), true)
            }
            dukaanNameTextView?.id -> launchProfilePreviewFragment()
            editProfileTextView?.id -> launchProfilePreviewFragment()
            profileStatusRecyclerView?.id -> launchProfilePreviewFragment()
            stepsLeftTextView?.id -> launchProfilePreviewFragment()
            completeProfileTextView?.id -> launchProfilePreviewFragment()
            shapeableImageView?.id -> launchProfilePreviewFragment()
            linearLayout?.id -> {
                if (false == StaticInstances.sPermissionHashMap?.get(Constants.STORE_LOGO)) {
                    showStaffFeatureLockedBottomSheet(Constants.NAV_BAR_SETTINGS)
                    return
                }
                var storeLogo = mAccountInfoResponse?.mStoreInfo?.storeInfo?.logoImage
                if (isNotEmpty(mStoreLogo)) storeLogo = mStoreLogo
                if (isNotEmpty(storeLogo)) launchFragment(ProfilePhotoFragment.newInstance(storeLogo), true, storePhotoImageView) else askCameraPermission()
            }
            viewAllHeading?.id -> launchFragment(NewReleaseFragment.newInstance(mAccountInfoResponse?.mTrendingList, mAccountInfoResponse?.mAccountStaticText), true)
        }
    }

    private fun launchProfilePreviewFragment() {
        if (false == StaticInstances.sPermissionHashMap?.get(Constants.STORE_PROFILE)) {
            showStaffFeatureLockedBottomSheet(Constants.NAV_BAR_SETTINGS)
            return
        }
        launchFragment(ProfilePreviewFragment.newInstance(mAccountPageInfoResponse?.mStoreInfo?.storeInfo?.name), true)
    }

    override fun onImageSelectionResultFile(file: File?, mode: String) {
        if (mode == Constants.MODE_CROP) {
            val fragment = CropPhotoFragment.newInstance(file?.toUri())
            fragment.setTargetFragment(this, Constants.CROP_IMAGE_REQUEST_CODE)
            launchFragment(fragment, true)
            return
        }
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
        }
        file?.run {
            val fileRequestBody = MultipartBody.Part.createFormData("media", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
            val imageTypeRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), Constants.BASE64_STORE_LOGO)
            mProfileService.generateCDNLink(imageTypeRequestBody, fileRequestBody)
        }
    }

    private fun showTrendingOffersBottomSheet() {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(
                R.layout.bottom_sheet_trending_offers,
                it.findViewById(R.id.bottomSheetContainer)
            )
            bottomSheetDialog.apply {
                setContentView(view)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                val bottomSheetBrowserText: TextView = view.findViewById(R.id.bottomSheetBrowserText)
                val bottomSheetHeadingTextView: TextView = view.findViewById(R.id.bottomSheetHeadingTextView)
                val bottomSheetUrl: TextView = view.findViewById(R.id.bottomSheetUrl)
                val bottomSheetClose: View = view.findViewById(R.id.bottomSheetClose)
                bottomSheetBrowserText.text = mAppSettingsResponseStaticData?.mBestViewedText
                val txtSpannable = SpannableString(Constants.DOTPE_OFFICIAL_URL)
                val boldSpan = StyleSpan(Typeface.BOLD)
                txtSpannable.setSpan(boldSpan, 6, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                bottomSheetUrl.text = txtSpannable
                bottomSheetHeadingTextView.setHtmlData(mAppSettingsResponseStaticData?.mBottomSheetText)
                bottomSheetUrl.setOnClickListener { copyDataToClipboard(Constants.DOTPE_OFFICIAL_URL_CLIPBOARD) }
                bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
            }.show()
        }
    }

    private fun showReferAndEarnBottomSheet(response: ReferAndEarnResponse?) {
        mActivity?.let {
            try {
                val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_refer_and_earn_v2, it.findViewById(R.id.bottomSheetContainer))
                bottomSheetDialog.apply {
                    setContentView(view)
                    setBottomSheetCommonProperty()
                    view.run {
                        val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                        val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                        val messageTextView: TextView = findViewById(R.id.messageTextView)
                        val bottomSheetHeading2TextView: TextView = findViewById(R.id.bottomSheetHeading2TextView)
                        val verifyTextView: TextView = findViewById(R.id.verifyTextView)
                        bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                        messageTextView.text = response?.message
                        bottomSheetHeadingTextView.text = response?.heading
                        bottomSheetHeading2TextView.setHtmlData(response?.heading2)
                        verifyTextView.text = response?.referNow
                        verifyTextView.setOnClickListener{
                            bottomSheetDialog.dismiss()
                            mReferEarnOverWhatsAppResponse?.let { response ->
                                shareReferAndEarnWithDeepLink(response.mReferAndEarnData)
                            }
                        }
                    }
                }.show()
            } catch (e: Exception) {
                Log.e(TAG, "showReferAndEarnBottomSheet: ${e.message}", e)
            }
        }
    }

    private fun fetchUserProfile() {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        mProfileService.getUserProfile()
    }

    override fun onToolbarSideIconClicked() {
        try {
            launchFragment(CommonWebViewFragment().newInstance(getString(R.string.help), "${BuildConfig.WEB_VIEW_URL}${WebViewUrls.WEB_VIEW_HELP}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&redirectFrom=settings&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"), true)
        } catch (e: Exception) {
            Log.e(TAG, "onToolbarSideIconClicked: ${e.message}", e)
        }
    }

    override fun onStop() {
        super.onStop()
        ToolBarManager.getInstance()?.setSideIconVisibility(false)
    }

    override fun onProfileResponse(profileResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (swipeRefreshLayout?.isRefreshing == true) swipeRefreshLayout?.isRefreshing = false
            if (profileResponse.mIsSuccessStatus) {
                mAccountInfoResponse = Gson().fromJson<AccountInfoResponse>(profileResponse.mCommonDataStr, AccountInfoResponse::class.java)
                StaticInstances.sIsShareStoreLocked = mAccountInfoResponse?.mIsShareStoreLocked ?: false
                mAccountInfoResponse?.let { setupUIFromProfileResponse(it) }
                mAccountInfoResponse?.mAccountStaticText?.run { mAppSettingsResponseStaticData = this }
                mAccountInfoResponse?.mStoreInfo?.storeServices?.run { mAppStoreServicesResponse = this }
                mAccountInfoResponse?.mStoreInfo?.bankDetails?.run { StaticInstances.sBankDetails = this }
            }
        }
    }

    override fun onReferAndEarnResponse(response: CommonApiResponse) {
        stopProgress()
        val responseModel = Gson().fromJson<ReferAndEarnResponse>(response.mCommonDataStr, ReferAndEarnResponse::class.java)
        mReferAndEarnResponse = responseModel
        CoroutineScopeUtils().runTaskOnCoroutineMain { showReferAndEarnBottomSheet(responseModel) }
    }

    override fun onReferAndEarnOverWhatsAppResponse(response: ReferEarnOverWhatsAppResponse) {
        mReferEarnOverWhatsAppResponse = response
    }

    override fun onResume() {
        super.onResume()
        stopProgress()
    }

    override fun onChangeStoreAndDeliveryStatusResponse(response: CommonApiResponse) = stopProgress()

    override fun onImageCDNLinkGenerateResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            val photoResponse = Gson().fromJson<String>(response.mCommonDataStr, String::class.java)
            mProfileService.updateStoreLogo(StoreLogoRequest(photoResponse))
        }
    }

    override fun onStoreLogoResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val photoResponse = Gson().fromJson<StoreResponse>(response.mCommonDataStr, StoreResponse::class.java)
                mStoreLogo = photoResponse?.storeInfo?.logoImage
                if (isNotEmpty(mStoreLogo)) {
                    storePhotoImageView?.visibility = View.VISIBLE
                    hiddenImageView?.visibility = View.INVISIBLE
                    hiddenTextView?.visibility = View.INVISIBLE
                    storePhotoImageView?.let {
                        Glide.with(this).load(mStoreLogo).into(it)
                    }
                } else {
                    StaticInstances.sIsStoreImageUploaded = false
                    storePhotoImageView?.visibility = View.GONE
                    hiddenImageView?.visibility = View.VISIBLE
                    hiddenTextView?.visibility = View.VISIBLE
                }
                mProfileService.getUserProfile()
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
            } else showToast(response.mMessage)
        }
    }

    override fun onProductShareStoreWAResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mShareDataOverWhatsAppText = Gson().fromJson<String>(response.mCommonDataStr, String::class.java)
            shareOnWhatsApp(mShareDataOverWhatsAppText)
        }
    }

    private fun setupUIFromProfileResponse(infoResponse: AccountInfoResponse) {
        StaticInstances.sIsStoreImageUploaded = (StaticInstances.sStoreInfo?.mStoreLogoStr?.isNotEmpty() == true)
        mAccountPageInfoResponse = infoResponse
        dukaanNameTextView?.text = infoResponse.mStoreInfo.storeInfo.name
        if (infoResponse.mStoreInfo.storeInfo.logoImage?.isNotEmpty() == true) {
            storePhotoImageView?.let {
                Glide.with(this).load(infoResponse.mStoreInfo.storeInfo.logoImage).into(it)
            }
            hiddenImageView?.visibility = View.INVISIBLE
            hiddenTextView?.visibility = View.INVISIBLE
        }
        infoResponse.mFooterImages?.forEachIndexed { index, imageUrl ->
            if (index == 0) {
                autoDataBackupImageView?.let {
                    Glide.with(this).load(imageUrl).into(it)
                }
            } else {
                safeSecureImageView?.let {
                    Glide.with(this).load(imageUrl).into(it)
                }
            }
        }
        if (infoResponse.mTotalSteps == infoResponse.mCompletedSteps) {
            profileStatusRecyclerView?.visibility = View.GONE
            stepsLeftTextView?.visibility = View.GONE
            completeProfileTextView?.visibility = View.GONE
            shapeableImageView?.visibility = View.GONE
            editProfileTextView?.visibility = View.VISIBLE
            editProfileTextView?.text = infoResponse.mAccountStaticText?.mTextEditProfile
        } else {
            stepsLeftTextView?.visibility = View.VISIBLE
            completeProfileTextView?.visibility = View.VISIBLE
            shapeableImageView?.visibility = View.VISIBLE
            editProfileTextView?.visibility = View.GONE
            profileStatusRecyclerView?.apply {
                visibility = View.VISIBLE
                layoutManager = GridLayoutManager(mActivity, infoResponse.mTotalSteps)
                adapter = ProfileStatusAdapter(infoResponse.mTotalSteps, infoResponse.mCompletedSteps, mActivity)
            }
        }
        settingStoreOptionRecyclerView?.apply {
            val settingsAdapter = SettingsStoreAdapter(mActivity, this@SettingsFragment)
            val linearLayoutManager = LinearLayoutManager(mActivity)
            layoutManager = linearLayoutManager
            adapter = settingsAdapter
            settingsAdapter.setSettingsList(infoResponse.mStoreOptions)
        }
        newReleaseRecyclerView?.apply {
            layoutManager = GridLayoutManager(mActivity, 3)
            adapter = NewReleaseAdapter(infoResponse.mTrendingList, this@SettingsFragment, mActivity, 3)
        }
        val remainingSteps = infoResponse.mTotalSteps.minus(infoResponse.mCompletedSteps)
        stepsLeftTextView?.text = if (remainingSteps == 1) "$remainingSteps ${infoResponse.mAccountStaticText?.mStepLeft}" else "$remainingSteps ${infoResponse.mAccountStaticText?.mStepsLeft}"
        completeProfileTextView?.text = infoResponse.mAccountStaticText?.mCompleteProfile
        val storeStatus = "${infoResponse.mAccountStaticText?.mStoreText} :"
        storeTextView?.text = storeStatus
        storeControlMessageTextView?.text = infoResponse.mAccountStaticText?.message_store_controls
        storeControlNewTextView?.text = infoResponse.mAccountStaticText?.mNewText
        storeControlTextView?.text = infoResponse.mAccountStaticText?.mTextStoreControls
        if (infoResponse.mStoreInfo.storeServices.mStoreFlag == 1) {
            storeValueTextView?.text = infoResponse.mAccountStaticText?.mOpenText
            mActivity?.let { context -> storeValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.open_green)) }
        } else {
            storeValueTextView?.text = infoResponse.mAccountStaticText?.mClosedText
            mActivity?.let { context -> storeValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.red)) }
        }
        hiddenTextView?.text = infoResponse.mAccountStaticText?.mTextAddPhoto
        newReleaseHeading?.text = infoResponse.mAccountStaticText?.mNewReleaseText
        viewAllHeading?.text = infoResponse.mAccountStaticText?.mViewAllText
        ToolBarManager.getInstance()?.headerTitle = infoResponse.mAccountStaticText?.page_heading
    }

    private fun checkStoreOptionClick(response: StoreOptionsResponse) {
        Log.d(TAG, "checkStoreOptionClick: $response")
        if (response.mIsStaffFeatureLocked) {
            showStaffFeatureLockedBottomSheet(Constants.NAV_BAR_SETTINGS)
            return
        }
        when (response.mPage) {
            Constants.PAGE_REFER -> {
                if (null != mReferAndEarnResponse) {
                    showReferAndEarnBottomSheet(mReferAndEarnResponse)
                    return
                }
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                mProfileService.getReferAndEarnDataOverWhatsApp()
                showProgressDialog(mActivity)
                mProfileService.getReferAndEarnData()
            }
            Constants.PAGE_MY_PAYMENTS -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_VIEW_MY_PAYMENTS,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.PATH to AFInAppEventParameterName.SETTINGS_PAGE)
                )
                launchFragment(MyPaymentsFragment.newInstance(), true)
            }
            Constants.PAGE_HELP -> onToolbarSideIconClicked()
            Constants.PAGE_FEEDBACK -> openPlayStore()
            Constants.PAGE_APP_SETTINGS -> launchFragment(AppSettingsFragment().newInstance(mAccountPageInfoResponse?.mSubPages, response.mText, mAppSettingsResponseStaticData), true)
            Constants.PAGE_REWARDS -> launchFragment(CommonWebViewFragment().newInstance(getString(R.string.my_rewards), "${BuildConfig.WEB_VIEW_URL}${WebViewUrls.WEB_VIEW_REWARDS}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&redirectFrom=settings&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"), true)
            else -> {
                if (Constants.NEW_RELEASE_TYPE_WEBVIEW == response.mAction) {
                    val url = "${BuildConfig.WEB_VIEW_URL}${response.mPage}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"
                    openWebViewFragmentV3(this, "", url)
                }
            }
        }
    }

    override fun onProfileDataException(e: Exception) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            Log.e(SettingsFragment::class.simpleName, "onProfileDataException", e)
            if (swipeRefreshLayout?.isRefreshing == true) swipeRefreshLayout?.isRefreshing = false
            exceptionHandlingForAPIResponse(e)
        }
    }

    override fun onRefresh() = fetchUserProfile()

    override fun onStoreSettingItemClicked(subPagesResponse: StoreOptionsResponse) = checkStoreOptionClick(subPagesResponse)

    override fun onNewReleaseItemClicked(responseItem: TrendingListResponse?) {
        Log.d(TAG, "onNewReleaseItemClicked: ${responseItem?.mAction}")
        if (true == responseItem?.isStaffFeatureLocked) {
            showStaffFeatureLockedBottomSheet(Constants.NAV_BAR_SETTINGS)
            return
        }
        when (responseItem?.mAction) {
            Constants.NEW_RELEASE_TYPE_WEBVIEW -> {
                if (Constants.NEW_RELEASE_TYPE_GOOGLE_ADS == responseItem.mType) {
                    if (StaticInstances.sIsShareStoreLocked) {
                        getLockedStoreShareDataServerCall(Constants.MODE_GOOGLE_ADS)
                        return
                    }
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_GOOGLE_ADS_EXPLORE,
                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                        data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.SETTINGS_PAGE)
                    )
                    mNewReleaseItemClickResponse = responseItem
                    getLocationForGoogleAds()
                    return
                }
                val eventName = when (responseItem.mType) {
                    Constants.NEW_RELEASE_TYPE_CUSTOM_DOMAIN -> AFInAppEventType.EVENT_DOMAIN_EXPLORE
                    Constants.NEW_RELEASE_TYPE_PREMIUM -> AFInAppEventType.EVENT_GET_PREMIUM_WEBSITE
                    Constants.NEW_RELEASE_TYPE_GOOGLE_ADS -> AFInAppEventType.EVENT_GOOGLE_ADS_EXPLORE
                    else -> AFInAppEventType.EVENT_VIEW_TOP_STORES
                }
                AppEventsManager.pushAppEvents(
                    eventName = eventName,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.IS_SETTINGS_PAGE)
                )
                openWebViewFragment(this, "", BuildConfig.WEB_VIEW_URL + responseItem.mPage)
            }
            Constants.NEW_RELEASE_TYPE_EXTERNAL -> {
                val eventName = when (responseItem.mType) {
                    Constants.NEW_RELEASE_TYPE_CUSTOM_DOMAIN -> AFInAppEventType.EVENT_GET_CUSTOM_DOMAIN
                    Constants.NEW_RELEASE_TYPE_PREMIUM -> AFInAppEventType.EVENT_GET_PREMIUM_WEBSITE
                    else -> AFInAppEventType.EVENT_VIEW_TOP_STORES
                }
                AppEventsManager.pushAppEvents(
                    eventName = eventName,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.SETTINGS_PAGE)
                )
                when (responseItem.mType) {
                    Constants.NEW_RELEASE_TYPE_CUSTOM_DOMAIN -> {
                        openUrlInBrowser(responseItem.mPage + PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                    }
                    Constants.NEW_RELEASE_TYPE_PREPAID_ORDER -> {
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_SET_PREPAID_ORDER,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(
                                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                AFInAppEventParameterName.PATH to AFInAppEventParameterName.NEW_RELEASES
                            )
                        )
                        launchFragment(SetOrderTypeFragment.newInstance(), true)
                    }
                    Constants.NEW_RELEASE_TYPE_PAYMENT_MODES -> {
                        launchFragment(PaymentModesFragment.newInstance(), true)
                    }
                    else -> openUrlInBrowser(responseItem.mPage)
                }

            }
            Constants.ACTION_BILLING_POS -> {
                launchFragment(BillingPosFragment.newInstance(), true)
            }
            else -> showTrendingOffersBottomSheet()
        }
    }

    override fun onBackPressed(): Boolean {
        Log.d(TAG, "onBackPressed: called")
        if (null != fragmentManager && 1 == fragmentManager?.backStackEntryCount) {
            if (true == StaticInstances.sPermissionHashMap?.get(Constants.PAGE_ORDER)) {
                clearFragmentBackStack()
                launchFragment(OrderFragment.newInstance(), true)
            } else {
                if (mIsDoublePressToExit) mActivity?.finish()
                showShortSnackBar(getString(R.string.msg_back_press))
                mIsDoublePressToExit = true
                Handler(Looper.getMainLooper()).postDelayed(
                    { mIsDoublePressToExit = false },
                    Constants.BACK_PRESS_INTERVAL
                )
            }
            return true
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.CROP_IMAGE_REQUEST_CODE) {
            CoroutineScopeUtils().runTaskOnCoroutineMain {
                val file = data?.getSerializableExtra(Constants.MODE_CROP) as File
                onImageSelectionResultFile(file, "")
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

    private fun checkLocationPermissionWithDialog(): Boolean {
        mActivity?.let {
            return if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    AlertDialog.Builder(it).apply {
                        setTitle("Location Permission")
                        setMessage("Please allow Location permission to continue")
                        setPositiveButton(R.string.ok) { _, _ -> ActivityCompat.requestPermissions(it, arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), Constants.LOCATION_REQUEST_CODE)
                        }
                    }.create().show()
                } else ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), Constants.LOCATION_REQUEST_CODE)
                true
            } else false
        }
        return false
    }

    private fun getLocationForGoogleAds() {
        if (checkLocationPermissionWithDialog()) return
        getLocationFromGoogleMap()
    }

    override fun onLocationChanged(lat: Double, lng: Double) {
        openWebViewFragmentWithLocation(this, "", BuildConfig.WEB_VIEW_URL + mNewReleaseItemClickResponse?.mPage + "?storeid=${PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)}&token=${PrefsManager.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&lat=$lat&lng=$lng")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    getLocationFromGoogleMap()
                }
                else -> {
                    showShortSnackBar("Permission was denied", true, R.drawable.ic_close_red)
                    mActivity?.onBackPressed()
                }
            }
        }
        else if (Constants.STORAGE_REQUEST_CODE == requestCode) {
            when {
                grantResults.isEmpty() -> Log.d(TAG, "User interaction was cancelled.")
                PackageManager.PERMISSION_GRANTED == grantResults[0] -> {
                    mReferEarnOverWhatsAppResponse?.let { response -> shareReferAndEarnWithDeepLink(response.mReferAndEarnData) }
                }
                else -> showShortSnackBar("Permission was denied", true, R.drawable.ic_close_red)
            }
        }
    }
}