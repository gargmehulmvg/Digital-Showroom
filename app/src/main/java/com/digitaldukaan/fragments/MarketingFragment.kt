package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.sentry.Sentry
import kotlinx.android.synthetic.main.layout_marketing_fragment.*


class MarketingFragment : BaseFragment(), IOnToolbarIconClick, IMarketingServiceInterface {

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
    }

    override fun onToolbarSideIconClicked() = openWebViewFragment(this, getString(R.string.help), WebViewUrls.WEB_VIEW_HELP, Constants.SETTINGS)

    override fun onMarketingErrorResponse(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

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
        when (response?.action) {
            Constants.ACTION_BUSINESS_CREATIVE -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_MARKET_VIEW_NOW,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), "type" to AFInAppEventParameterName.SOCIAL)
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
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), "type" to AFInAppEventParameterName.SOCIAL)
                )
                openWebViewFragment(this, "", WebViewUrls.WEB_VIEW_SOCIAL_CREATIVE_LIST, Constants.SETTINGS)
            }
            Constants.ACTION_CATALOG_PREMIUM -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_PREMIUM_PAGE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.CHANNEL to "isMarketing"
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
                        AFInAppEventParameterName.IS_MARKETING_PAGE to "true"
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
                        AFInAppEventParameterName.IS_MARKETING_PAGE to "true"
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
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.onBackPressed()
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
}