package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ProductPageResponse
import com.digitaldukaan.services.ProductService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProductServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.gson.Gson
import kotlinx.android.synthetic.main.common_webview_fragment.commonWebView
import kotlinx.android.synthetic.main.product_fragment.*

class ProductFragment : BaseFragment(), IProductServiceInterface {

    private lateinit var mService: ProductService

    companion object {
        fun newInstance(): ProductFragment {
            return ProductFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mService = ProductService()
        mService.setOrderDetailServiceListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.product_fragment, container, false)
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else mService.getProductPageInfo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            onBackPressed(this@ProductFragment)
        }
        return mContentView
    }

    override fun onProductResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showToast(commonResponse.toString())
            val productResponse = Gson().fromJson(commonResponse.mCommonDataStr, ProductPageResponse::class.java)
            var url: String
            ToolBarManager.getInstance().setHeaderTitle(productResponse?.static_text?.product_page_heading)
            commonWebView.apply {
                webViewClient = CommonWebViewFragment.WebViewController()
                settings.javaScriptEnabled = true
                addJavascriptInterface(WebViewBridge(), "Android")
                url = BuildConfig.WEB_VIEW_URL + productResponse.product_page_url + "?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&" + "&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"
                loadUrl(url)
            }
            productResponse.shareShop.run {
                shareButtonTextView.text = this.mText
            }
            productResponse.addProduct.run {
                addProductTextView.text = this.mText
            }
            showCancellableProgressDialog(mActivity)
            Log.d(ProductFragment::class.simpleName, "onViewCreated: $url")
            Handler(Looper.getMainLooper()).postDelayed({ stopProgress() }, Constants.TIMER_INTERVAL)
        }
    }

    override fun onProductException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}