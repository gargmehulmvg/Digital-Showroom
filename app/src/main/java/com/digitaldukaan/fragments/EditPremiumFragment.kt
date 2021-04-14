package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.PremiumPageInfoResponse
import com.digitaldukaan.models.response.PremiumPageInfoStaticTextResponse
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.webviews.WebViewBridge
import kotlinx.android.synthetic.main.layout_edit_premium_fragment.*

class EditPremiumFragment : BaseFragment() {

    private var mStaticText: PremiumPageInfoStaticTextResponse? = null
    private var mPremiumPageInfoResponse: PremiumPageInfoResponse? = null

    companion object {
        fun newInstance(
            staticText: PremiumPageInfoStaticTextResponse?,
            premiumPageInfoResponse: PremiumPageInfoResponse?
        ): EditPremiumFragment{
            val fragment = EditPremiumFragment()
            fragment.mStaticText = staticText
            fragment.mPremiumPageInfoResponse = premiumPageInfoResponse
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_edit_premium_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, true)
        }
        WebViewBridge.mWebViewListener = this
        hideBottomNavigationView(true)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        editPremiumWebView.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(WebViewBridge(), "Android")
            val url = "${BuildConfig.WEB_VIEW_PREVIEW_URL}${mPremiumPageInfoResponse?.domain}"
            Log.d(EditPremiumFragment::class.simpleName, "onViewCreated: $url")
            loadUrl(url)
        }
        appTitleTextView.text = mStaticText?.heading_edit_theme
        textView3.text = mStaticText?.text_customer_will_see
        editColorTextView.text = mStaticText?.text_edit_colors
        changeImageTextView.text = mStaticText?.text_edit_image
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            backButtonToolbar.id -> mActivity.onBackPressed()
        }
    }

}