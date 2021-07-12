package com.digitaldukaan.fragments

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.appsflyer.CreateOneLinkHttpTask
import com.appsflyer.share.ShareInviteHelper
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
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_settings_fragment.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class SettingsFragment : BaseFragment(), IOnToolbarIconClick, IProfileServiceInterface,
    SwipeRefreshLayout.OnRefreshListener, IStoreSettingsItemClicked {

    companion object {
        private const val TAG = "SettingsFragment"
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
    private var mProfileResponse: AccountInfoResponse? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        StaticInstances.sAccountPageSettingsStaticData = mAppSettingsResponseStaticData
        StaticInstances.sAppStoreServicesResponse = mAppStoreServicesResponse
        StaticInstances.sPaymentMethodStr = mProfileResponse?.mOnlinePaymentType
        when (view?.id) {
            storeControlView?.id -> launchFragment(MoreControlsFragment.newInstance(mAppSettingsResponseStaticData, mProfileResponse?.mIsOrderNotificationOn), true)
            dukaanNameTextView?.id -> launchProfilePreviewFragment()
            editProfileTextView?.id -> launchProfilePreviewFragment()
            profileStatusRecyclerView?.id -> launchProfilePreviewFragment()
            stepsLeftTextView?.id -> launchProfilePreviewFragment()
            completeProfileTextView?.id -> launchProfilePreviewFragment()
            shapeableImageView?.id -> launchProfilePreviewFragment()
            linearLayout?.id -> {
                var storeLogo = mAccountInfoResponse?.mStoreInfo?.storeInfo?.logoImage
                if (mStoreLogo?.isNotEmpty() == true) storeLogo = mStoreLogo
                if (storeLogo?.isNotEmpty() == true) launchFragment(ProfilePhotoFragment.newInstance(storeLogo), true, storePhotoImageView) else askCameraPermission()
            }
            viewAllHeading?.id -> launchFragment(NewReleaseFragment.newInstance(mAccountInfoResponse?.mTrendingList, mAccountInfoResponse?.mAccountStaticText), true)
        }
    }

    private fun launchProfilePreviewFragment() = launchFragment(ProfilePreviewFragment.newInstance(mProfileResponse?.mStoreInfo?.storeInfo?.name), true)

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
                        val bottomSheetUpperImageView: ImageView = findViewById(R.id.bottomSheetUpperImageView)
                        val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                        val messageTextView: TextView = findViewById(R.id.messageTextView)
                        val bottomSheetHeading2TextView: TextView = findViewById(R.id.bottomSheetHeading2TextView)
                        val verifyTextView: TextView = findViewById(R.id.verifyTextView)
                        bottomSheetUpperImageView.let {view ->

                        }
                        bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                        messageTextView.text = response?.message
                        bottomSheetHeadingTextView.text = response?.heading
                        bottomSheetHeading2TextView.setHtmlData(response?.heading2)
                        verifyTextView.text = response?.referNow
                        verifyTextView.setOnClickListener{
                            mReferEarnOverWhatsAppResponse?.run {
                                shareReferAndEarnWithDeepLink(this)
                                bottomSheetDialog.dismiss()
                            }
                        }
                    }
                }.show()
            } catch (e: Exception) {
                Log.e(TAG, "showReferAndEarnBottomSheet: ${e.message}", e)
            }
        }
    }

    private fun shareReferAndEarnWithDeepLink(referEarnOverWhatsAppResponse: ReferEarnOverWhatsAppResponse) {
        ShareInviteHelper.generateInviteUrl(mActivity).apply {
            channel = "whatsapp"
            campaign = "sharing"
            setReferrerCustomerId(PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER))
            generateLink(mActivity, object : CreateOneLinkHttpTask.ResponseListener {
                override fun onResponse(p0: String?) {
                    Log.d(TAG, "onResponse: $p0")
                    if (referEarnOverWhatsAppResponse.mReferAndEarnData.isShareStoreBanner == true) {
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_SETTINGS_REFERRAL,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.LINK to p0)
                        )
                        shareDataOnWhatsAppWithImage("${referEarnOverWhatsAppResponse.mReferAndEarnData.whatsAppText} $p0", referEarnOverWhatsAppResponse.mReferAndEarnData.imageUrl)
                    } else {
                        shareOnWhatsApp("${referEarnOverWhatsAppResponse.mReferAndEarnData.whatsAppText} $p0")
                    }
                }
                override fun onResponseError(p0: String?) {
                    Log.d(TAG, "onResponseError: $p0")
                }
            })
        }
    }

    private fun fetchUserProfile() {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity, "Fetching user profile...")
        mProfileService.getUserProfile()
    }

    override fun onToolbarSideIconClicked() = launchFragment(CommonWebViewFragment().newInstance(getString(R.string.help), "${BuildConfig.WEB_VIEW_URL}${WebViewUrls.WEB_VIEW_HELP}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&redirectFrom=settings&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"), true)

    override fun onStop() {
        super.onStop()
        ToolBarManager.getInstance()?.setSideIconVisibility(false)
    }

    override fun onProfileResponse(commonResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (swipeRefreshLayout?.isRefreshing == true) swipeRefreshLayout?.isRefreshing = false
            if (commonResponse.mIsSuccessStatus) {
                mAccountInfoResponse = Gson().fromJson<AccountInfoResponse>(commonResponse.mCommonDataStr, AccountInfoResponse::class.java)
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
            mProfileService.updateStoreLogo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), StoreLogoRequest(photoResponse))
        }
    }

    override fun onStoreLogoResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val photoResponse = Gson().fromJson<StoreResponse>(response.mCommonDataStr, StoreResponse::class.java)
                mStoreLogo = photoResponse?.storeInfo?.logoImage
                if (mStoreLogo?.isNotEmpty() == true) {
                    storePhotoImageView?.visibility = View.VISIBLE
                    hiddenImageView?.visibility = View.INVISIBLE
                    hiddenTextView?.visibility = View.INVISIBLE
                    storePhotoImageView?.let {
                        try {
                            Glide.with(this).load(mStoreLogo).into(it)
                        } catch (e: Exception) {
                            Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                        }
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

    override fun onProductShareStoreWAResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mShareDataOverWhatsAppText = Gson().fromJson<String>(commonResponse.mCommonDataStr, String::class.java)
            shareOnWhatsApp(mShareDataOverWhatsAppText)
        }
    }

    private fun setupUIFromProfileResponse(infoResponse: AccountInfoResponse) {
        StaticInstances.sIsStoreImageUploaded = (StaticInstances.sStoreInfo?.mStoreLogoStr?.isNotEmpty() == true)
        mProfileResponse = infoResponse
        dukaanNameTextView?.text = infoResponse.mStoreInfo.storeInfo.name
        if (infoResponse.mStoreInfo.storeInfo.logoImage?.isNotEmpty() == true) {
            storePhotoImageView?.let {
                try {
                    Glide.with(this).load(infoResponse.mStoreInfo.storeInfo.logoImage).into(it)
                } catch (e: Exception) {
                    Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                }
            }
            hiddenImageView?.visibility = View.INVISIBLE
            hiddenTextView?.visibility = View.INVISIBLE
        }
        infoResponse.mFooterImages?.forEachIndexed { index, imageUrl ->
            if (index == 0) {
                autoDataBackupImageView?.let {
                    try {
                        Glide.with(this).load(imageUrl).into(it)
                    } catch (e: Exception) {
                        Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                    }
                }
            } else {
                safeSecureImageView?.let {
                    try {
                        Glide.with(this).load(imageUrl).into(it)
                    } catch (e: Exception) {
                        Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                    }
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
            val settingsAdapter = SettingsStoreAdapter(this@SettingsFragment, this@SettingsFragment)
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
        val deliveryStatus = "${infoResponse.mAccountStaticText?.mTextDeliveryStatus} :"
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
        ToolBarManager.getInstance()?.setHeaderTitle(infoResponse.mAccountStaticText?.page_heading)
    }

    private fun checkStoreOptionClick(response: StoreOptionsResponse) {
        Log.d(TAG, "checkStoreOptionClick: $response")
        when (response.mPage) {
            Constants.PAGE_REFER -> {
                if (mReferAndEarnResponse != null) {
                    showReferAndEarnBottomSheet(mReferAndEarnResponse)
                    return
                }
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                showProgressDialog(mActivity)
                mProfileService.getReferAndEarnDataOverWhatsApp()
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
            Constants.PAGE_APP_SETTINGS -> launchFragment(AppSettingsFragment().newInstance(mProfileResponse?.mSubPages, response.mText, mAppSettingsResponseStaticData), true)
            Constants.PAGE_REWARDS -> launchFragment(CommonWebViewFragment().newInstance(getString(R.string.my_rewards), "${BuildConfig.WEB_VIEW_URL}${WebViewUrls.WEB_VIEW_REWARDS}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&redirectFrom=settings&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"), true)
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

    override fun onStoreSettingItemClicked(storeResponse: StoreOptionsResponse) = checkStoreOptionClick(storeResponse)

    override fun onNewReleaseItemClicked(responseItem: TrendingListResponse?) {
        Log.d(TAG, "onNewReleaseItemClicked: ${responseItem?.mAction}")
        when (responseItem?.mAction) {
            Constants.NEW_RELEASE_TYPE_WEBVIEW -> {
                val eventName = when (responseItem.mType) {
                    Constants.NEW_RELEASE_TYPE_CUSTOM_DOMAIN -> AFInAppEventType.EVENT_DOMAIN_EXPLORE
                    Constants.NEW_RELEASE_TYPE_PREMIUM -> AFInAppEventType.EVENT_GET_PREMIUM_WEBSITE
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
            else -> showTrendingOffersBottomSheet()
        }
    }

    override fun onBackPressed(): Boolean {
        Log.d(TAG, "onBackPressed: called")
        if(fragmentManager != null && fragmentManager?.backStackEntryCount == 1) {
            launchFragment(HomeFragment.newInstance(), true)
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
}