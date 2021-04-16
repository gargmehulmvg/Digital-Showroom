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
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.AddProductStaticText
import com.digitaldukaan.webviews.WebViewBridge
import kotlinx.android.synthetic.main.layout_view_as_customer.*

class ViewAsCustomerFragment: BaseFragment() {

    private var mDomainName = ""
    private var mIsPremiumEnable: Boolean = false
    private var addProductStaticData: AddProductStaticText? = null

    companion object {
        private const val TAG = "ViewAsCustomerFragment"
        fun newInstance(domain: String, isPremiumEnable: Boolean, addProductStaticData: AddProductStaticText?): ViewAsCustomerFragment {
            val fragment = ViewAsCustomerFragment()
            fragment.mDomainName = domain
            fragment.mIsPremiumEnable = isPremiumEnable
            fragment.addProductStaticData = addProductStaticData
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_view_as_customer, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            setHeaderTitle(addProductStaticData?.heading_view_store_as_customer)
            onBackPressed(this@ViewAsCustomerFragment)
            setSideIconVisibility(false)
        }
        getPremiumTextView.setHtmlData(addProductStaticData?.message_get_premium_website_for_your_showroom)
        getStartedTextView.text = addProductStaticData?.text_get_started
        hideBottomNavigationView(true)
        showProgressDialog(mActivity)
        premiumBottomContainer.visibility = if (mIsPremiumEnable) View.VISIBLE else View.GONE
        webView.apply {
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
            addJavascriptInterface(WebViewBridge(), "Android")
            Log.d(TAG, "onViewCreated: $mDomainName")
            loadUrl(mDomainName)
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            getStartedTextView.id -> {
                val url = "${BuildConfig.WEB_VIEW_URL}theme-discover?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"
                launchFragment(CommonWebViewFragment().newInstance("", url), true)
            }
        }
    }

}