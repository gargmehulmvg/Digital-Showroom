package com.digitaldukaan.webviews

import android.util.Log
import android.webkit.JavascriptInterface
import com.digitaldukaan.interfaces.IWebViewCallbacks

class WebViewBridge {

    companion object {
        var mWebViewListener: IWebViewCallbacks? = null
        private const val TAG = "WebViewBridge"
    }

    @JavascriptInterface
    fun onNativeBackPress() {
        Log.d(TAG, "onNativeBackPress")
        mWebViewListener?.onNativeBackPressed()
    }

    @JavascriptInterface
    fun sendData(value: String) {
        Log.d(TAG, "sendData")
        mWebViewListener?.sendData(value)
    }

    @JavascriptInterface
    fun showAndroidToast(value: String) {
        Log.d(TAG, "showAndroidToast")
        mWebViewListener?.showAndroidToast(value)
    }
}