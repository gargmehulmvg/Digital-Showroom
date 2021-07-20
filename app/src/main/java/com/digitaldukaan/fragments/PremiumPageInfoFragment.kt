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
import com.digitaldukaan.constants.AppEventsManager
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.PremiumPageInfoResponse
import com.digitaldukaan.models.response.PremiumPageInfoStaticTextResponse
import com.digitaldukaan.services.PremiumPageInfoService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IPremiumPageInfoServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_premium_fragment.*
import org.json.JSONObject


class PremiumPageInfoFragment : BaseFragment(), IPremiumPageInfoServiceInterface {

    companion object {
        private const val TAG = "PremiumPageInfoFragment"
        private val mService: PremiumPageInfoService = PremiumPageInfoService()
        private var mStaticText: PremiumPageInfoStaticTextResponse? = null
        private var premiumPageInfoResponse: PremiumPageInfoResponse? = null

        fun newInstance(): PremiumPageInfoFragment {
            return PremiumPageInfoFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
        mActivity?.let {
            it.runOnUiThread {
                it.onBackPressed()
            }
        }
    }

    override fun onPremiumPageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            premiumPageInfoResponse = Gson().fromJson<PremiumPageInfoResponse>(response.mCommonDataStr, PremiumPageInfoResponse::class.java)
            mStaticText = premiumPageInfoResponse?.staticText
            commonWebView?.apply {
                clearHistory()
                clearCache(true)
                settings?.run {
                    allowFileAccess = true
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    javaScriptCanOpenWindowsAutomatically = true
                }
                addJavascriptInterface(WebViewBridge(), "Android")
                val isBottomNavBarActive = premiumPageInfoResponse?.mIsBottomNavBarActive ?: false
                hideBottomNavigationView(!isBottomNavBarActive)
                val url = BuildConfig.WEB_VIEW_URL + premiumPageInfoResponse?.premium?.mUrl + "?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&app_version=${BuildConfig.VERSION_NAME}"
                Log.d(PremiumPageInfoFragment::class.simpleName, "onViewCreated: $url")
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        Log.d(TAG, "onPageFinished: called")
                        stopProgress()
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        return when {
                            url.startsWith("tel:") -> {
                                try {
                                    view.context?.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                                } catch (e: Exception) {
                                    Log.e(TAG, "shouldOverrideUrlLoading :: tel :: ${e.message}", e)
                                }
                                true
                            }
                            url.contains("mailto:") -> {
                                try {
                                    view.context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (e: Exception) {
                                    Log.e(TAG, "shouldOverrideUrlLoading :: mailto :: ${e.message}", e)
                                }
                                true
                            }
                            url.contains("whatsapp:") -> {
                                try {
                                    view.context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (e: Exception) {
                                    Log.e(TAG, "shouldOverrideUrlLoading :: whatsapp :: ${e.message}", e)
                                }
                                true
                            }
                            else -> {
                                view.loadUrl(url)
                                true
                            }
                        }
                    }
                }
                loadUrl(url)
            }
        }
    }

    override fun sendData(data: String) {
        Log.d(TAG, "sendData: $data")
        val jsonData = JSONObject(data)
        when {
            jsonData.optBoolean("redirectNative") -> launchFragment(EditPremiumFragment.newInstance(mStaticText, premiumPageInfoResponse), true)
            jsonData.optBoolean("redirectBrowser") -> openUrlInBrowser(jsonData.optString("data"))
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
            if(fragmentManager != null && fragmentManager?.backStackEntryCount == 1) {
                clearFragmentBackStack()
                launchFragment(HomeFragment.newInstance(), true)
                true
            } else {
                if (commonWebView?.canGoBack() == true) {
                    commonWebView?.goBack()
                    true
                } else false
            }
        } catch (e: Exception) {
            false
        }
    }
}