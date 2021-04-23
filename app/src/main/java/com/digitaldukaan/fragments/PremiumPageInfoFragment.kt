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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_premium_fragment, container, false)
        mService.setServiceInterface(this)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateNavigationBarState(R.id.menuPremium)
        ToolBarManager.getInstance().apply {
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
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.onBackPressed()
        }
    }

    override fun onPremiumPageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            premiumPageInfoResponse = Gson().fromJson<PremiumPageInfoResponse>(response.mCommonDataStr, PremiumPageInfoResponse::class.java)
            mStaticText = premiumPageInfoResponse?.staticText
            commonWebView.apply {
                clearHistory()
                clearCache(true)
                settings.allowFileAccess = true
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.javaScriptCanOpenWindowsAutomatically = true
                addJavascriptInterface(WebViewBridge(), "Android")
                val url = BuildConfig.WEB_VIEW_URL + premiumPageInfoResponse?.premium?.mUrl + "?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}" + "&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"
                Log.d(PremiumPageInfoFragment::class.simpleName, "onViewCreated: $url")
                loadUrl(url)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        Log.d(TAG, "onPageFinished: called")
                        stopProgress()
                    }

                    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                        return when {
                            url.startsWith("tel:") -> {
                                val tel = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                                activity?.startActivity(tel)
                                true
                            }
                            url.contains("mailto:") -> { view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                true
                            }
                            else -> {
                                view.loadUrl(url)
                                true
                            }
                        }
                    }
                }
            }
        }
    }

    override fun sendData(data: String) {
        Log.d(TAG, "sendData: $data")
        val jsonData = JSONObject(data)
        if (jsonData.optBoolean("redirectNative")) {
            launchFragment(EditPremiumFragment.newInstance(mStaticText, premiumPageInfoResponse), true)
        } else if (jsonData.optBoolean("redirectBrowser")) {
            openUrlInBrowser(jsonData.optString("data"))
        }
    }

    override fun onPremiumPageInfoServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun showAndroidToast(data: String) {
        showToast(data)
    }

    override fun showAndroidLog(data: String) {
        Log.d(TAG, "showAndroidLog :: $data")
    }
}