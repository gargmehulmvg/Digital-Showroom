package com.digitaldukaan.webviews

import android.webkit.JavascriptInterface
import com.digitaldukaan.interfaces.IWebViewCallbacks

class WebViewBridge {

    companion object {
        var mWebViewListener: IWebViewCallbacks? = null
        val tag = WebViewBridge::class.simpleName
    }

    @JavascriptInterface
    fun onNativeBackPress() {
        mWebViewListener?.onNativeBackPressed()
    }
}