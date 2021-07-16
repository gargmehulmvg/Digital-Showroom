package com.digitaldukaan.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
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
import com.digitaldukaan.adapters.SharePDFAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.MarketingCardsItemResponse
import com.digitaldukaan.models.response.ShareStorePDFDataItemResponse
import com.digitaldukaan.services.MarketingService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IMarketingServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.sentry.Sentry
import kotlinx.android.synthetic.main.layout_marketing_fragment.*


class MarketingFragment : BaseFragment(), IOnToolbarIconClick, IMarketingServiceInterface, LocationListener {

    private var mMarketingItemClickResponse: MarketingCardsItemResponse? = null

    companion object {
        private lateinit var service: MarketingService
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
        service = MarketingService()
        service.setMarketingServiceListener(this)
        mContentView = inflater.inflate(R.layout.layout_marketing_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateNavigationBarState(R.id.menuMarketing)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            setHeaderTitle(getString(R.string.marketing_page_heading))
            setSideIconVisibility(true)
            hideBackPressFromToolBar(mActivity, false)
            onBackPressed(this@MarketingFragment)
            setSecondSideIconVisibility(false)
            setSideIconVisibility(true)
            mActivity?.let { setSideIcon(ContextCompat.getDrawable(it, R.drawable.ic_setting_toolbar), this@MarketingFragment) }
        }
        hideBottomNavigationView(false)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        service.getMarketingCardsData()
        WebViewBridge.mWebViewListener = this
        mActivity?.let { context -> mGoogleApiClient = LocationServices.getFusedLocationProviderClient(context) }
    }

    override fun onToolbarSideIconClicked() = openWebViewFragment(this, getString(R.string.help), WebViewUrls.WEB_VIEW_HELP, Constants.SETTINGS)

    override fun onMarketingErrorResponse(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onMarketingResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val listType = object : TypeToken<List<MarketingCardsItemResponse>>() {}.type
                val list = Gson().fromJson<ArrayList<MarketingCardsItemResponse>>(response.mCommonDataStr, listType)
                marketingCardRecyclerView?.apply {
                    val gridLayoutManager = GridLayoutManager(mActivity, 2)
                    layoutManager = gridLayoutManager
                    gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return if (list[position].type == Constants.SPAN_TYPE_FULL_WIDTH) 2 else 1
                        }
                    }
                    layoutManager = gridLayoutManager
                    adapter = MarketingCardAdapter(this@MarketingFragment, list, this@MarketingFragment)
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
                openWebViewFragment(this, "", BuildConfig.WEB_VIEW_URL + response.pageUrl)
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
                service.getShareStoreData()
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
                    service.getShareStorePdfText()
                } else {
                    showPDFShareBottomSheet(mShareStorePDFResponse)
                }
            }
        }
    }

    private fun getLocationForGoogleAds() {
        if (checkLocationPermissionWithDialog()) return
        getLastLocation()
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
                            service.generateStorePdf()
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

    private var mGoogleApiClient: FusedLocationProviderClient? = null
    private var mCurrentLatitude = 0.0
    private var lastLocation: Location? = null
    private var mCurrentLongitude = 0.0

    private fun getLastLocation() {
        if (checkLocationPermission()) return
        val locationManager = mActivity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1f, this)
        mGoogleApiClient?.lastLocation?.addOnCompleteListener(mActivity) { task ->
            if (task.isSuccessful && task.result != null) {
                lastLocation = task.result
                mCurrentLatitude = lastLocation?.latitude ?: 0.0
                mCurrentLongitude = lastLocation?.longitude ?: 0.0
                openGoogleAds()
            } else {
                if (!isLocationEnabledInSettings(mActivity)) openLocationSettings(true)
                mCurrentLatitude = 0.0
                mCurrentLongitude =  0.0
                openGoogleAds()
            }
        }
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
        openWebViewFragmentWithLocation(this, "", BuildConfig.WEB_VIEW_URL + mMarketingItemClickResponse?.pageUrl + "?storeid=${PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)}&token=${PrefsManager.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&lat=$mCurrentLatitude&lng=$mCurrentLongitude")
    }

    private fun checkLocationPermission(): Boolean {
        mActivity?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Constants.LOCATION_REQUEST_CODE)
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == Constants.LOCATION_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    getLastLocation()
                }
                else -> {
                    mActivity?.onBackPressed()
                    showShortSnackBar("Permission was denied", true, R.drawable.ic_close_red)
                }
            }
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "onLocationChanged() Latitude: " + location.latitude + " , Longitude: " + location.longitude)
        mCurrentLatitude = location.latitude
        mCurrentLongitude = location.longitude
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        Log.d(TAG, "onStatusChanged :: p0 :: $p0, p1 :: $p1, p2:: $p2")
    }

    override fun onProviderEnabled(p0: String?) {
        Log.d(TAG, "onProviderEnabled :: $p0")
    }

    override fun onProviderDisabled(p0: String?) {
        Log.d(TAG, "onProviderDisabled :: $p0")
    }
}