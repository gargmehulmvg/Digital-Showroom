package com.digitaldukaan.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.PremiumPageInfoResponse
import com.digitaldukaan.models.response.PremiumPageInfoStaticTextResponse
import com.digitaldukaan.services.PremiumPageInfoService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IPremiumPageInfoServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_premium_fragment.*
import org.json.JSONObject


class PremiumPageInfoFragment : BaseFragment(), IPremiumPageInfoServiceInterface {

    private var mPremiumPageInfoResponse: PremiumPageInfoResponse? = null
    private val mService: PremiumPageInfoService = PremiumPageInfoService()
    private var mIsDoublePressToExit = false

    companion object {
        private var mStaticText: PremiumPageInfoStaticTextResponse? = null

        fun newInstance(): PremiumPageInfoFragment = PremiumPageInfoFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "PremiumPageInfoFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_premium_fragment, container, false)
        mService.setServiceInterface(this)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateNavigationBarState(R.id.menuPremium)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, true)
        }
        WebViewBridge.mWebViewListener = this
        hideBottomNavigationView(false)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        mService.getPremiumPageInfo()
    }

    override fun onNativeBackPressed() {
        mActivity?.let { context -> context.runOnUiThread { context.onBackPressed() } }
    }

    override fun onPremiumPageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mPremiumPageInfoResponse = Gson().fromJson<PremiumPageInfoResponse>(response.mCommonDataStr, PremiumPageInfoResponse::class.java)
            StaticInstances.sIsShareStoreLocked = mPremiumPageInfoResponse?.isShareStoreLocked ?: false
            mStaticText = mPremiumPageInfoResponse?.staticText
            commonWebView?.apply {
                clearHistory()
                clearCache(true)
                settings?.run {
                    allowFileAccess = true
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    javaScriptCanOpenWindowsAutomatically = true
                }
                addJavascriptInterface(WebViewBridge(), Constants.KEY_ANDROID)
                val isBottomNavBarActive = mPremiumPageInfoResponse?.mIsBottomNavBarActive ?: false
                hideBottomNavigationView(!isBottomNavBarActive)
                val url = BuildConfig.WEB_VIEW_URL + mPremiumPageInfoResponse?.premium?.mUrl + "?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&app_version=${BuildConfig.VERSION_NAME}&app_version_code${BuildConfig.VERSION_CODE}"
                Log.d(PremiumPageInfoFragment::class.simpleName, "onViewCreated: $url")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        Log.d(TAG, "onPageFinished: called")
                        stopProgress()
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean =
                        onCommonWebViewShouldOverrideUrlLoading(url, view)
                }
                loadUrl(url)
            }
        }
    }

    override fun sendData(data: String) {
        Log.d(TAG, "sendData: $data")
        val jsonData = JSONObject(data)
        when {
            jsonData.optBoolean("redirectNative") -> launchFragment(EditPremiumFragment.newInstance(mStaticText, mPremiumPageInfoResponse), true)
            jsonData.optBoolean("redirectBrowser") -> openUrlInBrowser(jsonData.optString("data"))
            jsonData.optBoolean("unauthorizedAccess") -> logoutFromApplication()
            jsonData.optBoolean("openUPIIntent") -> {
                val intent = Intent()
                intent.data = Uri.parse(jsonData.optString("data"))
                val chooser = Intent.createChooser(intent, "Pay with...")
                startActivityForResult(chooser, 1, null)
            }
            jsonData.optBoolean("openUPI") -> {
                val packageName = jsonData.optString("packageName")
                val uri: Uri = Uri.Builder()
                    .scheme("upi")
                    .authority("pay")
                    .appendQueryParameter("pa", jsonData.optString("pa"))
                    .appendQueryParameter("pn", jsonData.optString("pn"))
                    .appendQueryParameter("tr", jsonData.optString("tr"))
                    .appendQueryParameter("tn", jsonData.optString("tn"))
                    .appendQueryParameter("am", jsonData.optString("am"))
                    .appendQueryParameter("cu", "INR")
                    .build()
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = uri
                intent.setPackage(packageName)
                mActivity?.startActivityForResult(intent, 123)
            }
            jsonData.optBoolean("trackEventData") -> {
                val eventName = jsonData.optString("eventName")
                val additionalData = jsonData.optString("additionalData")
                val map = Gson().fromJson<HashMap<String, String>>(additionalData.toString(), HashMap::class.java)
                Log.d(TAG, "sendData: working $map")
                AppEventsManager.pushAppEvents(eventName = eventName, isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true, data = map)
            }
            jsonData.optBoolean("refreshToken") -> {
                //mService.getPremiumPageInfo()
            }
            jsonData.optBoolean("redirectHomePage") -> {
                launchFragment(OrderFragment.newInstance(), true)
            }
            jsonData.optBoolean("openDomainPurchaseBottomSheet") -> {
                openDomainPurchaseBottomSheetServerCall()
            }
            jsonData.optBoolean("addAddress") -> {
                launchFragment(StoreMapLocationFragment.newInstance(0, true), true)
            }
            jsonData.optBoolean("openAppByPackage") -> {
                val packageName = jsonData.optString("data")
                openAppByPackageName(packageName, mActivity)
            }
            jsonData.optBoolean("stopLoader") -> {
                stopProgress()
            }
            jsonData.optBoolean("shareTextOnWhatsApp") -> {
                val text = jsonData.optString("data")
                val mobileNumber = jsonData.optString("mobileNumber")
                shareDataOnWhatsAppByNumber(mobileNumber, text)
            }
        }
    }

    override fun onPremiumPageInfoServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun showAndroidToast(data: String) = showToast(data)

    override fun showAndroidLog(data: String) {
        Log.d(TAG, "showAndroidLog :: $data")
    }

    override fun onBackPressed(): Boolean {
        return try {
            Log.d(TAG, "onBackPressed :: called")
            if(null != fragmentManager && 1 == fragmentManager?.backStackEntryCount) {
                clearFragmentBackStack()
                if (true == StaticInstances.sPermissionHashMap?.get(Constants.PAGE_ORDER)) {
                    launchFragment(OrderFragment.newInstance(), true)
                } else {
                    launchFragment(SettingsFragment.newInstance(), true)
                }
                return true
            } else {
                if (true == commonWebView?.canGoBack()) {
                    commonWebView?.goBack()
                    true
                } else false
            }
        } catch (e: Exception) {
            false
        }
    }
}