package com.digitaldukaan.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.NewReleaseAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IStoreSettingsItemClicked
import com.digitaldukaan.models.response.AccountStaticTextResponse
import com.digitaldukaan.models.response.StoreOptionsResponse
import com.digitaldukaan.models.response.TrendingListResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics

class NewReleaseFragment: BaseFragment(), IStoreSettingsItemClicked {

    private var mNewReleaseList: ArrayList<TrendingListResponse>? = null
    private var mHeadingStr: String? = null
    private var mSettingsStaticData: AccountStaticTextResponse? = null
    private var mNewReleaseItemClickResponse: TrendingListResponse? = null

    companion object {

        fun newInstance(trendingList: ArrayList<TrendingListResponse>?, staticTextResponse: AccountStaticTextResponse?): NewReleaseFragment {
            val fragment = NewReleaseFragment()
            fragment.mNewReleaseList = trendingList
            fragment.mSettingsStaticData = staticTextResponse
            fragment.mHeadingStr = staticTextResponse?.heading_new_releases
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "NewReleaseFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_new_release_fragment, container, false)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            headerTitle = mHeadingStr
            onBackPressed(this@NewReleaseFragment)
            setSideIconVisibility(false)
            setSecondSideIconVisibility(false)
        }
        val newReleaseRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.newReleaseRecyclerView)
        newReleaseRecyclerView?.apply {
            layoutManager = GridLayoutManager(mActivity, 3)
            adapter = NewReleaseAdapter(mNewReleaseList, this@NewReleaseFragment, mActivity, mNewReleaseList?.size ?: 0)
        }
        hideBottomNavigationView(true)
        return mContentView
    }

    override fun onStoreSettingItemClicked(subPagesResponse: StoreOptionsResponse) {
        Log.d(TAG, "onStoreSettingItemClicked: do nothing")
    }

    override fun onNewReleaseItemClicked(responseItem: TrendingListResponse?) {
        Log.d(TAG, "onNewReleaseItemClicked :: responseItem?.mAction :: ${responseItem?.mAction}")
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
                    Constants.NEW_RELEASE_TYPE_CUSTOM_DOMAIN -> AFInAppEventType.EVENT_GET_CUSTOM_DOMAIN
                    Constants.NEW_RELEASE_TYPE_PREMIUM -> AFInAppEventType.EVENT_GET_PREMIUM_WEBSITE
                    else -> AFInAppEventType.EVENT_VIEW_TOP_STORES
                }
                AppEventsManager.pushAppEvents(
                    eventName = eventName,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.SETTINGS_PAGE)
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
                Log.d(TAG, "onNewReleaseItemClicked :: responseItem.mType :: ${responseItem.mType}")
                when (responseItem.mType) {
                    Constants.NEW_RELEASE_TYPE_CUSTOM_DOMAIN -> openUrlInBrowser(responseItem.mPage + PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                    Constants.NEW_RELEASE_TYPE_PREPAID_ORDER -> {
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_SET_PREPAID_ORDER,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.PATH to AFInAppEventParameterName.NEW_RELEASES)
                        )
                        launchFragment(SetOrderTypeFragment.newInstance(), true)
                    }
                    Constants.NEW_RELEASE_TYPE_PAYMENT_MODES -> launchFragment(PaymentModesFragment.newInstance(), true)
                    else -> openUrlInBrowser(responseItem.mPage)
                }

            }
            else -> showTrendingOffersBottomSheet()
        }
    }

    private fun showTrendingOffersBottomSheet() {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_trending_offers, it.findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                view?.run {
                    val bottomSheetBrowserText: TextView = findViewById(R.id.bottomSheetBrowserText)
                    val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                    val bottomSheetUrl: TextView = findViewById(R.id.bottomSheetUrl)
                    val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                    bottomSheetBrowserText.text = mSettingsStaticData?.mBestViewedText
                    val txtSpannable = SpannableString(Constants.DOTPE_OFFICIAL_URL)
                    val boldSpan = StyleSpan(Typeface.BOLD)
                    txtSpannable.setSpan(boldSpan, 6, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    bottomSheetUrl.text = txtSpannable
                    bottomSheetHeadingTextView.setHtmlData(mSettingsStaticData?.mBottomSheetText)
                    bottomSheetUrl.setOnClickListener { copyDataToClipboard(Constants.DOTPE_OFFICIAL_URL_CLIPBOARD) }
                    bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                }
            }.show()
        }
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
    }

}
