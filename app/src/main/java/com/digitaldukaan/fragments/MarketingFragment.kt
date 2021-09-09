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
import com.digitaldukaan.adapters.MarketingCardAdapter
import com.digitaldukaan.adapters.MarketingMoreOptionsAdapter
import com.digitaldukaan.adapters.SharePDFAdapter
import com.digitaldukaan.constants.*
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
import io.sentry.Sentry
import kotlinx.android.synthetic.main.layout_marketing_fragment.*

class MarketingFragment : BaseFragment(), IOnToolbarIconClick, IMarketingServiceInterface, LocationListener,
    IAppSettingsItemClicked, IMarketingMoreOptionsItemClicked {

    private var mMarketingItemClickResponse: MarketingCardsItemResponse? = null
    private var mCurrentLatitude = 0.0
    private var mCurrentLongitude = 0.0

    companion object {
        private var mService: MarketingService? = null
        private var mShareStorePDFResponse: ShareStorePDFDataItemResponse? = null
        private const val TAG = "MarketingFragment"

        fun newInstance(): MarketingFragment {
            return MarketingFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        showProgressDialog(mActivity)
        mService?.getMarketingPageInfo()
        WebViewBridge.mWebViewListener = this
    }

    override fun onToolbarSideIconClicked() = openWebViewFragment(this, getString(R.string.help), WebViewUrls.WEB_VIEW_HELP, Constants.SETTINGS)

    override fun onMarketingErrorResponse(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onMarketingResponse(response: CommonApiResponse) {
        stopProgress()
    }

    override fun onMarketingPageInfoResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val marketingPageInfoResponse = Gson().fromJson<MarketingPageInfoResponse>(response.mCommonDataStr, MarketingPageInfoResponse::class.java)
                setupMarketingShareUI(marketingPageInfoResponse?.marketingStoreShare)
                setupMarketingHelpPageUI(marketingPageInfoResponse?.marketingHelpPage)
                ToolBarManager.getInstance()?.apply {
                    setHeaderTitle(marketingPageInfoResponse?.marketingStaticTextResponse?.heading_marketing)
                    moreOptionsHeadingTextView?.text = marketingPageInfoResponse?.marketingStaticTextResponse?.text_more_options
                }
                val optionMenuAdapterAdapter = MarketingMoreOptionsAdapter(this, marketingPageInfoResponse?.marketingMoreOptionsList)
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
                                return if (marketingPageInfoResponse?.marketingItemList?.get(position)?.type == Constants.SPAN_TYPE_FULL_WIDTH) 2 else 1
                            }
                        }
                        layoutManager = gridLayoutManager
                        adapter = MarketingCardAdapter(this@MarketingFragment, marketingPageInfoResponse?.marketingItemList, this@MarketingFragment)
                    }
                }
            } else {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
            }
        }
    }

    private fun setupMarketingHelpPageUI(marketingHelpPage: HelpPageResponse?) {
        ToolBarManager.getInstance()?.apply {
            if (marketingHelpPage?.mIsActive == true) {
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
            shareTextView?.text = response.right_icon_text
            val shareLeftImageView: ImageView? = mContentView?.findViewById(R.id.shareLeftImageView)
            val shareRightImageView: ImageView? = mContentView?.findViewById(R.id.shareRightImageView)
            val expiryLeftImageView: ImageView? = mContentView?.findViewById(R.id.expiryLeftImageView)
            mActivity?.let { context ->
                shareLeftImageView?.let { view -> Glide.with(context).load(response.left_icon_cdn).into(view) }
                shareRightImageView?.let { view -> Glide.with(context).load(response.right_icon_cdn).into(view) }
                if (isEmpty(response.domain_expiry_message)) {
                    separator?.visibility = View.GONE
                    expiryContainer?.visibility = View.GONE
                } else {
                    separator?.visibility = View.VISIBLE
                    expiryContainer?.visibility = View.VISIBLE
                    expiryTextView?.text = response.domain_expiry_message
                    knowMoreTextView?.text = response.heading_know_more
                    expiryLeftImageView?.let { view -> Glide.with(context).load(response.domain_expiry_cdn).into(view) }
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
            showShortSnackBar(
                response.mMessage,
                true,
                if (response.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red
            )
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
        Log.d(TAG, "onMarketingItemClick: ${response?.action}")
        when (response?.action) {
            Constants.ACTION_PROMO_CODE -> {
                launchFragment(PromoCodePageInfoFragment.newInstance(), true)
            }
            Constants.NEW_RELEASE_TYPE_GOOGLE_ADS -> {
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
                openWebViewFragment(this, "", WebViewUrls.WEB_VIEW_SOCIAL_CREATIVE_LIST, Constants.SETTINGS)
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
                openWebViewFragment(this, "", WebViewUrls.WEB_VIEW_QR_DOWNLOAD, Constants.SETTINGS)
            }
            Constants.ACTION_SHARE_DATA -> {
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
                if (mShareStorePDFResponse == null) {
                    showProgressDialog(mActivity)
                    mService?.getShareStorePdfText()
                } else {
                    showPDFShareBottomSheet(mShareStorePDFResponse)
                }
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
                        if (response?.imageUrl?.isNotEmpty() == true) bottomSheetUpperImageView?.let {
                            try {
                                Glide.with(this@MarketingFragment).load(response.imageUrl).into(it)
                            } catch (e: Exception) {
                                Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                            }
                        }
                        bottomSheetUpperImageView.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_share_pdf_whatsapp))
                        bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                        bottomSheetHeadingTextView.text = response?.heading
                        verifyTextView.text = response?.subHeading
                        verifyTextView.setOnClickListener{
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
        mActivity?.let {
            it.runOnUiThread {
                it.onBackPressed()
            }
        }
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
            launchFragment(HomeFragment.newInstance(), true)
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
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(
                    Constants.STORE_ID
                ),
                AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.MARKETING
            )
        )
        openWebViewFragmentWithLocation(this, "", BuildConfig.WEB_VIEW_URL + mMarketingItemClickResponse?.url + "?storeid=${PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)}&token=${PrefsManager.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&lat=$mCurrentLatitude&lng=$mCurrentLongitude")
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
                    mActivity?.onBackPressed()
                    showShortSnackBar("Permission was denied", true, R.drawable.ic_close_red)
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
            Constants.ACTION_SHARE_DATA -> {
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
                if (mShareStorePDFResponse == null) {
                    showProgressDialog(mActivity)
                    mService?.getShareStorePdfText()
                } else {
                    showPDFShareBottomSheet(mShareStorePDFResponse)
                }
            }
        }
    }

    override fun onAppSettingItemClicked(subPagesResponse: SubPagesResponse) {
        Log.d(TAG, "onAppSettingItemClicked: ${subPagesResponse.mAction}")
    }
}