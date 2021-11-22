package com.digitaldukaan.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.*
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.interfaces.IAppSettingsItemClicked
import com.digitaldukaan.interfaces.IMarketingMoreOptionsItemClicked
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.MarketingService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IMarketingServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.sentry.Sentry
import kotlinx.android.synthetic.main.layout_marketing_fragment.*

class MarketingFragment : BaseFragment(), IOnToolbarIconClick, IMarketingServiceInterface, LocationListener,
    IAppSettingsItemClicked, IMarketingMoreOptionsItemClicked {

    private var mMarketingItemClickResponse: MarketingCardsItemResponse? = null
    private var mCurrentLatitude = 0.0
    private var mCurrentLongitude = 0.0
    private var mKnowMoreCustomDomainRecyclerView: RecyclerView? = null
    private var mKnowMoreBottomSheetDialog: BottomSheetDialog? = null
    private var mProgressBarView: View? = null
    private var mMarketingPageInfoResponse: MarketingPageInfoResponse? = null
    private var mService: MarketingService? = null

    companion object {
        private var mShareStorePDFResponse: ShareStorePDFDataItemResponse? = null

        fun newInstance(): MarketingFragment = MarketingFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "MarketingFragment"
        mService = MarketingService()
        mService?.setMarketingServiceListener(this)
        mContentView = inflater.inflate(R.layout.layout_marketing_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateNavigationBarState(R.id.menuMarketing)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            onBackPressed(this@MarketingFragment)
            setSecondSideIconVisibility(false)
            setSideIconVisibility(false)
        }
        hideBottomNavigationView(false)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }

        if(StaticInstances.sIsInvitationShown == true){
            showStaffInvitationDialog(StaticInstances.sStaffInvitation)
        }else{
            showProgressDialog(mActivity)
            mService?.getMarketingPageInfo()
        }
        WebViewBridge.mWebViewListener = this
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            shareStoreClickContainer?.id -> {
                if (StaticInstances.sIsShareStoreLocked) {
                    getLockedStoreShareDataServerCall(Constants.MODE_SHARE_STORE)
                } else shareStoreOverWhatsAppServerCall()
            }
        }
    }

    override fun onToolbarSideIconClicked() = openWebViewFragment(this, getString(R.string.help), WebViewUrls.WEB_VIEW_HELP, Constants.SETTINGS)

    override fun onMarketingErrorResponse(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onMarketingResponse(response: CommonApiResponse) = stopProgress()

    override fun onMarketingPageInfoResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                mMarketingPageInfoResponse = Gson().fromJson<MarketingPageInfoResponse>(response.mCommonDataStr, MarketingPageInfoResponse::class.java)
                StaticInstances.sIsShareStoreLocked = mMarketingPageInfoResponse?.isShareStoreLocked ?: false
                setupMarketingShareUI(mMarketingPageInfoResponse?.marketingStoreShare)
                setupMarketingHelpPageUI(mMarketingPageInfoResponse?.marketingHelpPage)
                ToolBarManager.getInstance()?.apply {
                    headerTitle = mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_marketing
                    moreOptionsHeadingTextView?.text = mMarketingPageInfoResponse?.marketingStaticTextResponse?.text_more_options
                }
                val optionMenuAdapterAdapter = MarketingMoreOptionsAdapter(this, mMarketingPageInfoResponse?.marketingMoreOptionsList)
                marketingMoreOptionsRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    adapter = optionMenuAdapterAdapter
                }
                marketingCardRecyclerView?.apply {
                    marketingCardRecyclerView?.apply {
                        val gridLayoutManager = GridLayoutManager(mActivity, 2)
                        layoutManager = gridLayoutManager
                        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                            override fun getSpanSize(position: Int): Int {
                                return if (Constants.SPAN_TYPE_FULL_WIDTH == mMarketingPageInfoResponse?.marketingItemList?.get(position)?.type) 2 else 1
                            }
                        }
                        layoutManager = gridLayoutManager
                        mMarketingPageInfoResponse?.marketingItemList?.forEachIndexed { _, itemResponse -> itemResponse?.isStaffFeatureLocked = true }
                        adapter = MarketingCardAdapter(this@MarketingFragment, mMarketingPageInfoResponse?.marketingItemList, this@MarketingFragment)
                    }
                }
            } else {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
            }
        }
    }

    private fun setupMarketingHelpPageUI(marketingHelpPage: HelpPageResponse?) {
        ToolBarManager.getInstance()?.apply {
            if (true == marketingHelpPage?.mIsActive) {
                setSideIconVisibility(true)
                mActivity?.let { setSideIcon(ContextCompat.getDrawable(it, R.drawable.ic_setting_toolbar), this@MarketingFragment) }
            } else {
                setSideIconVisibility(false)
            }
        }
    }

    private fun setupMarketingShareUI(shareResponse: MarketingStoreShareResponse?) {
        shareResponse?.let { response ->
            shareHeadingTextView?.text = response.heading
            domainTextView?.text = response.domain
            shareTextView?.text = response.rightIconText
            val shareLeftImageView: ImageView? = mContentView?.findViewById(R.id.shareLeftImageView)
            val shareRightImageView: ImageView? = mContentView?.findViewById(R.id.shareRightImageView)
            val expiryLeftImageView: ImageView? = mContentView?.findViewById(R.id.expiryLeftImageView)
            mActivity?.let { context ->
                shareLeftImageView?.let { view -> Glide.with(context).load(response.leftIconCdn).into(view) }
                shareRightImageView?.let { view -> Glide.with(context).load(response.rightIconCdn).into(view) }
                if (isEmpty(response.domainExpiryMessage)) {
                    separator?.visibility = View.GONE
                    expiryContainer?.visibility = View.GONE
                } else {
                    separator?.visibility = View.VISIBLE
                    expiryContainer?.visibility = View.VISIBLE
                    expiryTextView?.text = response.domainExpiryMessage
                    knowMoreTextView?.apply {
                        if (shareResponse.isKnowMoreEnable) {
                            visibility = View.VISIBLE
                            text = response.headingKnowMore
                        } else {
                            text = null
                            visibility = View.GONE
                        }
                    }
                    expiryContainer?.setOnClickListener {
                        if (StaticInstances.sIsShareStoreLocked) {
                            getLockedStoreShareDataServerCall(Constants.MODE_SHARE_STORE)
                            return@setOnClickListener
                        }
                        showKnowMoreBottomSheet(response.knowMore)
                    }
                    expiryLeftImageView?.let { view -> Glide.with(context).load(response.domainExpiryCdn).into(view) }
                }
            }
        }
    }

    override fun onAppShareDataResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            shareOnWhatsApp(Gson().fromJson<String>(response.mCommonDataStr, String::class.java))
        }
    }

    override fun onGenerateStorePdfResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showShortSnackBar(response.mMessage, true, if (response.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red)
        }
    }

    override fun onShareStorePdfDataResponse(response: CommonApiResponse) {
        mShareStorePDFResponse = Gson().fromJson<ShareStorePDFDataItemResponse>(response.mCommonDataStr, ShareStorePDFDataItemResponse::class.java)
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showPDFShareBottomSheet(mShareStorePDFResponse)
        }
    }

    override fun onMarketingItemClick(response: MarketingCardsItemResponse?) {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        if (true == response?.isStaffFeatureLocked) {
            showStaffFeatureLockedBottomSheet(Constants.NAV_BAR_MARKETING)
            return
        }
        Log.d(TAG, "onMarketingItemClick: ${response?.action}")
        when (response?.action) {
            Constants.ACTION_PROMO_CODE -> {
                launchFragment(PromoCodePageInfoFragment.newInstance(), true)
            }
            Constants.NEW_RELEASE_TYPE_GOOGLE_ADS -> {
                if (StaticInstances.sIsShareStoreLocked) {
                    getLockedStoreShareDataServerCall(Constants.MODE_GOOGLE_ADS)
                    return
                }
                mMarketingItemClickResponse = response
                getLocationForGoogleAds()
            }
            Constants.ACTION_BUSINESS_CREATIVE -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_MARKET_VIEW_NOW,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.TYPE to AFInAppEventParameterName.SOCIAL)
                )
                openWebViewFragment(this, "", WebViewUrls.WEB_VIEW_CREATIVE_LIST, Constants.SETTINGS)
            }
            Constants.NEW_RELEASE_TYPE_CUSTOM_DOMAIN -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_DOMAIN_EXPLORE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.IS_MARKETING_PAGE)
                )
                openWebViewFragment(this, "", BuildConfig.WEB_VIEW_URL + response.url)
            }
            Constants.ACTION_SOCIAL_CREATIVE -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_MARKET_VIEW_NOW,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.TYPE to AFInAppEventParameterName.SOCIAL)
                )
                launchFragment(SocialMediaFragment.newInstance(mMarketingPageInfoResponse), true)
            }
            Constants.ACTION_THEME_DISCOVER -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_GET_PREMIUM_WEBSITE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.IS_MARKETING
                    )
                )
                openWebViewFragment(this, "", BuildConfig.WEB_VIEW_URL + response.action)
            }
            Constants.ACTION_THEME_EXPLORE -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_GET_PREMIUM_WEBSITE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.IS_MARKETING
                    )
                )
                openWebViewFragment(this, "", BuildConfig.WEB_VIEW_URL + response.action)
            }
            Constants.ACTION_QR_DOWNLOAD -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_QR_DOWNLOAD,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.IS_MARKETING_PAGE to AFInAppEventParameterName.TRUE
                    )
                )
                openWebViewFragment(this, "", response.url, Constants.SETTINGS)
            }
            Constants.ACTION_SHARE_STORE -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_STORE_SHARE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.IS_MARKETING_PAGE to AFInAppEventParameterName.TRUE
                    )
                )
                showProgressDialog(mActivity)
                mService?.getShareStoreData()
            }
            Constants.ACTION_CATALOG_WHATSAPP -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SHARE_PDF_CATALOG,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PATH to AFInAppEventParameterName.MARKETING
                    )
                )
                if (null == mShareStorePDFResponse) {
                    showProgressDialog(mActivity)
                    mService?.getShareStorePdfText()
                } else {
                    showPDFShareBottomSheet(mShareStorePDFResponse)
                }
            }
        }
    }

    override fun onMarketingSuggestedDomainsResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mProgressBarView?.visibility = View.GONE
            if (response.mIsSuccessStatus) {
                val listType = object : TypeToken<ArrayList<MarketingSuggestedDomainItemResponse?>>() {}.type
                val list = Gson().fromJson<ArrayList<MarketingSuggestedDomainItemResponse?>>(response.mCommonDataStr, listType)
                mKnowMoreCustomDomainRecyclerView?.visibility = View.VISIBLE
                mKnowMoreCustomDomainRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                    adapter = MarketingKnowMoreItemAdapter(mActivity, list, object : IAdapterItemClickListener {
                        override fun onAdapterItemClickListener(position: Int) {
                            mKnowMoreBottomSheetDialog?.dismiss()
                            val item = list?.get(position)
                            when(item?.cta?.action) {
                                Constants.NEW_RELEASE_TYPE_WEBVIEW -> {
                                    openWebViewFragment(this@MarketingFragment, "", BuildConfig.WEB_VIEW_URL + item.cta.url)
                                }
                            }
                        }
                    })
                }
            } else {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
            }
        }
    }

    private fun getLocationForGoogleAds() {
        if (checkLocationPermissionWithDialog()) return
        getLocationFromGoogleMap()
    }

    private fun showPDFShareBottomSheet(response: ShareStorePDFDataItemResponse?) {
        try {
            mActivity?.let {
                val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(it).inflate(
                    R.layout.bottom_sheet_refer_and_earn,
                    it.findViewById(R.id.bottomSheetContainer)
                )
                bottomSheetDialog.apply {
                    setContentView(view)
                    setBottomSheetCommonProperty()
                    view.run {
                        val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                        val bottomSheetUpperImageView: ImageView = findViewById(R.id.bottomSheetUpperImageView)
                        val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                        val verifyTextView: TextView = findViewById(R.id.verifyTextView)
                        val referAndEarnRecyclerView: RecyclerView = findViewById(R.id.referAndEarnRecyclerView)
                        if (isNotEmpty(response?.imageUrl))
                            Glide.with(this@MarketingFragment).load(response?.imageUrl).into(bottomSheetUpperImageView)
                        bottomSheetUpperImageView.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_share_pdf_whatsapp))
                        bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                        bottomSheetHeadingTextView.text = response?.heading
                        verifyTextView.text = response?.subHeading
                        verifyTextView.setOnClickListener{
                            if (StaticInstances.sIsShareStoreLocked) {
                                getLockedStoreShareDataServerCall(Constants.MODE_GET_MY_CATALOGUE)
                                return@setOnClickListener
                            }
                            showProgressDialog(mActivity)
                            mService?.generateStorePdf()
                            AppEventsManager.pushAppEvents(
                                eventName = AFInAppEventType.EVENT_GET_PDF_CATALOG,
                                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                data = mapOf(
                                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                    AFInAppEventParameterName.PATH to AFInAppEventParameterName.MARKETING
                                )
                            )
                            bottomSheetDialog.dismiss()
                        }
                        referAndEarnRecyclerView.apply {
                            layoutManager = LinearLayoutManager(mActivity)
                            adapter = SharePDFAdapter(response?.howItWorks)
                        }
                    }
                }.show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "showPDFShareBottomSheet: ${e.message}", e)
            Sentry.captureException(e, "showPDFShareBottomSheet: exception")
        }
    }

    override fun onNativeBackPressed() {
        mActivity?.let { context -> context.runOnUiThread { context.onBackPressed() } }
    }

    override fun sendData(data: String) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            Log.d(TAG, "sendData: $data")
        }
    }

    override fun onBackPressed(): Boolean {
        Log.d(TAG, "onBackPressed: called")
        if(fragmentManager != null && fragmentManager?.backStackEntryCount == 1) {
            clearFragmentBackStack()
            launchFragment(OrderFragment.newInstance(), true)
            return true
        }
        return false
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

    private fun openGoogleAds() {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_GOOGLE_ADS_EXPLORE,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.MARKETING
            )
        )
        openWebViewFragmentWithLocation(this, "", BuildConfig.WEB_VIEW_URL + mMarketingItemClickResponse?.url + "?storeid=${PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)}&token=${PrefsManager.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&lat=$mCurrentLatitude&lng=$mCurrentLongitude")
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (Constants.LOCATION_REQUEST_CODE == requestCode) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                PackageManager.PERMISSION_GRANTED == grantResults[0] -> {
                    getLocationFromGoogleMap()
                }
                else -> {
                    showShortSnackBar("Permission was denied", true, R.drawable.ic_close_red)
                    mActivity?.onBackPressed()
                }
            }
        }
    }

    override fun onLocationChanged(lat: Double, lng: Double) {
        mCurrentLatitude = lat
        mCurrentLongitude = lng
        openGoogleAds()
    }

    override fun onMarketingMoreOptionsItemClicked(itemResponse: MarketingMoreOptionsItemResponse?) {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        Log.d(TAG, "onMarketingMoreOptionsItemClicked: ${itemResponse?.action}")
        when (itemResponse?.action) {
            Constants.ACTION_QR_DOWNLOAD -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_QR_DOWNLOAD,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.IS_MARKETING_PAGE to AFInAppEventParameterName.TRUE
                    )
                )
                openWebViewFragment(this, "", WebViewUrls.WEB_VIEW_QR_DOWNLOAD, Constants.SETTINGS)
            }
            Constants.ACTION_SHARE_STORE -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_STORE_SHARE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.IS_MARKETING_PAGE to AFInAppEventParameterName.TRUE
                    )
                )
                showProgressDialog(mActivity)
                mService?.getShareStoreData()
            }
            Constants.ACTION_CATALOG_WHATSAPP -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SHARE_PDF_CATALOG,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PATH to AFInAppEventParameterName.MARKETING
                    )
                )
                if (null == mShareStorePDFResponse) {
                    showProgressDialog(mActivity)
                    mService?.getShareStorePdfText()
                } else {
                    showPDFShareBottomSheet(mShareStorePDFResponse)
                }
            }
            Constants.ACTION_BOTTOM_SHEET -> {
                showMoreOptionsBottomSheet(itemResponse)
            }
        }
    }

    private fun showMoreOptionsBottomSheet(itemResponse: MarketingMoreOptionsItemResponse) {
        try {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SHARE_PRODUCT_MARKETING,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
            )
            mActivity?.let {
                val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(it).inflate(
                    R.layout.bottom_sheet_marketing_more_options,
                    it.findViewById(R.id.bottomSheetContainer)
                )
                bottomSheetDialog.apply {
                    setContentView(view)
                    view.run {
                        val headingTextView: TextView = findViewById(R.id.headingTextView)
                        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
                        headingTextView.text = itemResponse.expandableDataHeading
                        recyclerView.apply {
                            layoutManager = GridLayoutManager(mActivity, 2)
                            adapter = MarketingMoreOptionsBottomSheetItemAdapter(this@MarketingFragment, itemResponse.expandableData, object : IAdapterItemClickListener {

                                override fun onAdapterItemClickListener(position: Int) {
                                    val item = itemResponse.expandableData?.get(position)
                                    AppEventsManager.pushAppEvents(
                                        eventName = item?.eventName, isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                        data = item?.eventParameter ?: HashMap()
                                    )
                                    Log.d(TAG, "showMoreOptionsBottomSheet :: item clicked :: $item")
                                    bottomSheetDialog.dismiss()
                                    val headingStr = when(item?.action) {
                                        Constants.ACTION_BESTSELLER -> mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_new_launches_and_bestsellers
                                        Constants.ACTION_PRODUCT_DISCOUNT -> mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_product_discount
                                        else -> ""
                                    }
                                    launchFragment(EditSocialMediaTemplateFragment.newInstance(headingStr, null, true, mMarketingPageInfoResponse), true)
                                }

                            })
                        }
                    }
                }.show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "showMoreOptionsBottomSheet: ${e.message}", e)
        }
    }

    private fun showKnowMoreBottomSheet(itemResponse: MarketingDomainKnowMoreResponse?) {
        try {
            mActivity?.let {
                mKnowMoreBottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(it).inflate(
                    R.layout.bottom_sheet_marketing_know_more,
                    it.findViewById(R.id.bottomSheetContainer)
                )
                mKnowMoreBottomSheetDialog?.apply {
                    setContentView(view)
                    view.run {
                        val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                        mProgressBarView = findViewById(R.id.progressBar)
                        val headingTextView: TextView = findViewById(R.id.headingTextView)
                        val domainTextView: TextView = findViewById(R.id.domainTextView)
                        val offerMessageTextView: TextView = findViewById(R.id.offerMessageTextView)
                        val expiryMessageTextView: TextView = findViewById(R.id.expiryMessageTextView)
                        mKnowMoreCustomDomainRecyclerView = findViewById(R.id.recyclerView)
                        bottomSheetClose.setOnClickListener {
                            mKnowMoreBottomSheetDialog?.dismiss()
                        }
                        mProgressBarView?.visibility = View.VISIBLE
                        mService?.getMarketingSuggestedDomains()
                        headingTextView.text = itemResponse?.headingYourDomain
                        domainTextView.text = itemResponse?.domainName
                        expiryMessageTextView.text = itemResponse?.domainExpiryMessage
                        offerMessageTextView.text = itemResponse?.messageGetBestDomain
                    }
                }?.show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "showMoreOptionsBottomSheet: ${e.message}", e)
        }
    }

    override fun onAppSettingItemClicked(subPagesResponse: SubPagesResponse) {
        Log.d(TAG, "onAppSettingItemClicked: ${subPagesResponse.mAction}")
    }

    override fun onLockedStoreShareSuccessResponse(lockedShareResponse: LockedStoreShareResponse) = showLockedStoreShareBottomSheet(lockedShareResponse)

    override fun onProviderEnabled(provider: String) {}

    override fun onProviderDisabled(provider: String) {}

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {}

}