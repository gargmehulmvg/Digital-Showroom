package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.webviews.WebViewBridge
import kotlinx.android.synthetic.main.layout_common_webview_fragment.*


class PremiumFragment : BaseFragment() {

    companion object {
        fun newInstance(): PremiumFragment {
            return PremiumFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_premium_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val mLoadUrl = ""
        updateNavigationBarState(R.id.menuPremium)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, true)
        }
        commonWebView.apply {
            commonWebView.webViewClient = CommonWebViewFragment.WebViewController()
            settings.javaScriptEnabled = true
            addJavascriptInterface(WebViewBridge(), "Android")
            Log.d(CommonWebViewFragment::class.simpleName, "onViewCreated: $mLoadUrl")
            loadUrl(mLoadUrl)
        }
        showCancellableProgressDialog(mActivity)
        Log.d(PremiumFragment::class.simpleName, "onViewCreated: $mLoadUrl")
        Handler(Looper.getMainLooper()).postDelayed({
            stopProgress()
        }, Constants.TIMER_INTERVAL)
        hideBottomNavigationView(false)
        WebViewBridge.mWebViewListener = this
    }

    override fun onNativeBackPressed() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.onBackPressed()
        }
    }
}