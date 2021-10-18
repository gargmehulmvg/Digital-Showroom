package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.PrefsManager
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.SocialMediaTemplateListItemResponse
import com.digitaldukaan.webviews.WebViewBridge
import kotlinx.android.synthetic.main.layout_edit_premium_fragment.*
import kotlinx.android.synthetic.main.layout_edit_social_media_template_fragment.*

class EditSocialMediaTemplateFragment : BaseFragment() {

    private var mSocialMediaTemplateResponse: SocialMediaTemplateListItemResponse? = null

    companion object {
        private const val TAG = "EditSocialMediaTemplateFragment"

        fun newInstance(item: SocialMediaTemplateListItemResponse?): EditSocialMediaTemplateFragment {
            val fragment = EditSocialMediaTemplateFragment()
            fragment.mSocialMediaTemplateResponse = item
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_edit_social_media_template_fragment, container, false)
        hideBottomNavigationView(true)
        WebViewBridge.mWebViewListener = this
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("Edit & Share")
            setSideIconVisibility(false)
            onBackPressed(this@EditSocialMediaTemplateFragment)
        }
        Log.d(TAG, "onViewCreated: mSocialMediaTemplateResponse :: $mSocialMediaTemplateResponse")
        webView?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(WebViewBridge(), "Android")
            var url = mSocialMediaTemplateResponse?.html?.htmlText ?: ""

            url = url.replace("id=\"business_creative_storename\"> Store Name</div>", "id=\"business_creative_storename\">${PrefsManager.getStringDataFromSharedPref(Constants.STORE_NAME)}</div>")

            Log.d(TAG, "onViewCreated: text :: $url")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Log.d(TAG, "onPageFinished: called")
                    stopProgress()
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    return onCommonWebViewShouldOverrideUrlLoading(url, view)
                }
            }
            loadData(url, "text/html", "UTF-8")
        }
    }

}