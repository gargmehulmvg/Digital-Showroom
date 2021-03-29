package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.webviews.WebViewBridge
import kotlinx.android.synthetic.main.layout_common_webview_fragment.*

class CommonWebViewFragment : BaseFragment() {

    private lateinit var mHeaderText: String
    private lateinit var mLoadUrl: String

    fun newInstance(headerText: String, loadUrl:String): CommonWebViewFragment {
        val fragment = CommonWebViewFragment()
        fragment.mHeaderText = headerText
        fragment.mLoadUrl = loadUrl
        return fragment
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_common_webview_fragment, container, false)
        hideBottomNavigationView(true)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, true)
        }
        commonWebView.apply {
            commonWebView.webViewClient = WebViewController()
            settings.javaScriptEnabled = true
            addJavascriptInterface(WebViewBridge(), "Android")
            Log.d(CommonWebViewFragment::class.simpleName, "onViewCreated: $mLoadUrl")
            loadUrl(mLoadUrl)
        }
        showCancellableProgressDialog(mActivity)
        Log.d(CommonWebViewFragment::class.simpleName, "onViewCreated: $mLoadUrl")
        Handler(Looper.getMainLooper()).postDelayed({
            stopProgress()
        }, Constants.TIMER_INTERVAL)
    }

    class WebViewController : WebViewClient() {

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

}