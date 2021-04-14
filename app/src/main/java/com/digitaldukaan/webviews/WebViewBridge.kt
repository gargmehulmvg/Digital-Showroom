package com.digitaldukaan.webviews

import android.util.Log
import android.webkit.JavascriptInterface
import com.digitaldukaan.interfaces.IWebViewCallbacks

class WebViewBridge {

    companion object {
        var mWebViewListener: IWebViewCallbacks? = null
        val tag = WebViewBridge::class.simpleName
    }

    @JavascriptInterface
    fun onNativeBackPress() {
        Log.d(WebViewBridge::class.simpleName, "onNativeBackPress")
        mWebViewListener?.onNativeBackPressed()
    }

    @JavascriptInterface
    fun sendData(value: String) {
        Log.d(WebViewBridge::class.simpleName, "sendData")
        mWebViewListener?.sendData(value)
    }
}