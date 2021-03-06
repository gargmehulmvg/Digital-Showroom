package com.digitaldukaan.fragments

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
import com.digitaldukaan.models.response.AddProductStaticText
import com.digitaldukaan.webviews.WebViewBridge
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.layout_common_webview_fragment.*
import kotlinx.android.synthetic.main.layout_view_as_customer.*

class ViewAsCustomerFragment: BaseFragment() {

    private var mDomainName = ""
    private var mIsPremiumEnable: Boolean = false
    private var mAddProductStaticData: AddProductStaticText? = null

    companion object {
        fun newInstance(domain: String, isPremiumEnable: Boolean, addProductStaticData: AddProductStaticText?): ViewAsCustomerFragment {
            val fragment = ViewAsCustomerFragment()
            fragment.mDomainName = domain
            fragment.mIsPremiumEnable = isPremiumEnable
            fragment.mAddProductStaticData = addProductStaticData
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "ViewAsCustomerFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_view_as_customer, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            headerTitle = mAddProductStaticData?.heading_view_store_as_customer
            onBackPressed(this@ViewAsCustomerFragment)
            setSideIconVisibility(false)
        }
        getPremiumTextView?.setHtmlData(mAddProductStaticData?.message_get_premium_website_for_your_showroom)
        getStartedTextView?.text = mAddProductStaticData?.text_get_started
        hideBottomNavigationView(true)
        showProgressDialog(mActivity)
        premiumBottomContainer?.visibility = if (mIsPremiumEnable) View.GONE else View.VISIBLE
        webView?.apply {
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Log.d(TAG, "onPageFinished: called")
                    stopProgress()
                }
            }
            clearHistory()
            clearCache(true)
            settings.allowFileAccess = true
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.javaScriptCanOpenWindowsAutomatically = true
            addJavascriptInterface(WebViewBridge(), Constants.KEY_ANDROID)
            Log.d(TAG, "onViewCreated: $mDomainName")
            loadUrl(mDomainName)
        }
        premiumBottomContainer?.visibility = if (true == StaticInstances.sPermissionHashMap?.get(Constants.PAGE_PREMIUM)) View.VISIBLE else View.GONE
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            getStartedTextView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_GET_PREMIUM_WEBSITE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.IS_CATALOG
                    )
                )
                val url = "${BuildConfig.WEB_VIEW_URL}theme-discover?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"
                launchFragment(CommonWebViewFragment().newInstance("", url), true)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        return try {
            Log.d(TAG, "onBackPressed :: called")
            if(null != fragmentManager && 1 == fragmentManager?.backStackEntryCount) {
                clearFragmentBackStack()
                launchFragment(OrderFragment.newInstance(), true)
                true
            } else {
                if (true == webView?.canGoBack()) {
                    webView?.goBack()
                    true
                } else false
            }
        } catch (e: Exception) {
            false
        }
    }

}