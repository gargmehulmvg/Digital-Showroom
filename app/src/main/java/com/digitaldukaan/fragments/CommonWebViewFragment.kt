package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.webviews.WebViewBridge
import kotlinx.android.synthetic.main.layout_common_webview_fragment.*
import org.json.JSONObject

class CommonWebViewFragment : BaseFragment(), IOnToolbarIconClick,
    PopupMenu.OnMenuItemClickListener {

    private lateinit var mHeaderText: String
    private lateinit var mLoadUrl: String
    private val mTagName = "CommonWebViewFragment"

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
        if (mHeaderText == getString(R.string.my_rewards)) {
            ToolBarManager.getInstance().apply {
                hideToolBar(mActivity, false)
                setHeaderTitle(mHeaderText)
                setSideIconVisibility(true)
                setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_options_menu), this@CommonWebViewFragment)
            }
        } else {
            ToolBarManager.getInstance().apply { hideToolBar(mActivity, true) }
        }
        commonWebView.apply {
            val webViewController = WebViewController()
            webViewController.commonWebView = commonWebView
            commonWebView.webViewClient = webViewController
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
        WebViewBridge.mWebViewListener = this
    }

    override fun onNativeBackPressed() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.onBackPressed()
        }
    }

    override fun sendData(data: String) {
        stopProgress()
        Log.d(mTagName, "sendData: $data")
        val jsonData = JSONObject(data)
        if (jsonData.optBoolean("shareCreative")) {
            if (jsonData.optBoolean("shareSingle")) {
                val base64OriginalStr = jsonData.optString("data")
                val domain = jsonData.optString("domain")
                Log.d(mTagName, "image URL :: $base64OriginalStr")
                val base64Str = base64OriginalStr.split("data:image/png;base64,")[1]
                Log.d(mTagName, "image URL :: $base64Str")
                val bitmap = getBitmapFromBase64V2(base64Str)
                shareDataOnWhatsAppWithImage("Order From - $domain", bitmap)
            } else {
                val imageBase64 = jsonData.optString("data")
                Log.d(mTagName, "image URL :: $imageBase64")
                val domain = jsonData.optString("domain")
                val bitmap = getBitmapFromBase64(imageBase64)
                shareData("Order From - $domain", bitmap)
            }
        } else if (jsonData.optBoolean("downloadImage")) {
            val base64OriginalStr = jsonData.optString("data")
            Log.d(mTagName, "image BASE64 :: $base64OriginalStr")
            val base64Str = base64OriginalStr.split("data:image/png;base64,")[1]
            Log.d(mTagName, "image URL :: $base64Str")
            val bitmap = getBitmapFromBase64V2(base64Str)
            saveMediaToStorage(bitmap, mActivity)
            showToast("Image Saved to Gallery")
        } else if (jsonData.optBoolean("convertImage")) {
            showProgressDialog(mActivity)
            val imageUrl = jsonData.optString("data")
            val image64 = getBase64FromImageURL(imageUrl)
            Log.d(mTagName, "image BASE64 :: $image64")
            CoroutineScopeUtils().runTaskOnCoroutineMain {
                if (jsonData.optString("sharePage") == "business") {
                    commonWebView?.loadUrl("javascript: receiveAndroidData('$image64')")
                } else {
                    commonWebView?.loadUrl("javascript: receiveAndroidSocialData('$image64')")
                }
            }
        }
    }

    class WebViewController : WebViewClient() {

        var commonWebView: WebView? = null

        override fun onPageFinished(view: WebView?, url: String?) {
            Log.d("WebViewController", "onPageFinished: called")
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }

    override fun onToolbarSideIconClicked() {
        val sideView:View = mActivity.findViewById(R.id.sideIconToolbar)
        val optionsMenu = PopupMenu(mActivity, sideView)
        optionsMenu.inflate(R.menu.menu_product_fragment)
        optionsMenu.menu?.add(Menu.NONE, 0, Menu .NONE, getString(R.string.term_and_condition))
        optionsMenu.menu?.add(Menu.NONE, 1, Menu .NONE, getString(R.string.help))
        optionsMenu.setOnMenuItemClickListener(this)
        optionsMenu.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (0 == item?.itemId) openWebViewFragment(this, getString(R.string.help), Constants.WEB_VIEW_TNC, Constants.SETTINGS)
        if (1 == item?.itemId) openWebViewFragment(this, getString(R.string.help), Constants.WEB_VIEW_HELP, Constants.SETTINGS)
        return true
    }

    override fun showAndroidToast(data: String) {
        showToast(data)
    }

}