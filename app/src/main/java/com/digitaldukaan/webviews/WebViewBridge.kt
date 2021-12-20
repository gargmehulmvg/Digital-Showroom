package com.digitaldukaan.webviews

import android.util.Log
import android.webkit.JavascriptInterface
import com.digitaldukaan.interfaces.IWebViewCallbacks

class WebViewBridge {

    companion object {
        var mWebViewListener: IWebViewCallbacks? = null
        private const val TAG           = "WebViewBridge"
        const val MANAGED_INVENTORY     = "managed_inventory"
        const val AVAILABLE             = "available"
        const val ID                    = "id"
        const val VARIANT_ID            = "variant_id"
        const val DATA                  = "data"
        const val SELECTED_VARIANT      = "selectedVariant"
        const val AVAILABLE_QUANTITY    = "available_quantity"
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

    @JavascriptInterface
    fun showAndroidLog(value: String) {
        Log.d(TAG, "showAndroidLog")
        mWebViewListener?.showAndroidLog(value)
    }
}