package com.digitaldukaan.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.dto.ConvertMultiImageDTO
import com.digitaldukaan.webviews.WebViewBridge
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_common_webview_fragment.*
import org.json.JSONObject


class CommonWebViewFragment : BaseFragment(), IOnToolbarIconClick,
    PopupMenu.OnMenuItemClickListener {

    private var mHeaderText: String = ""
    private var mLoadUrl: String = ""
    private val mTagName = "CommonWebViewFragment"
    private var mDomainName = ""

    fun newInstance(headerText: String, loadUrl:String): CommonWebViewFragment {
        val fragment = CommonWebViewFragment()
        fragment.mHeaderText = headerText
        fragment.mLoadUrl = loadUrl
        fragment.mLoadUrl ="${fragment.mLoadUrl}&app_version=${BuildConfig.VERSION_NAME}"
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
            ToolBarManager.getInstance()?.apply {
                hideToolBar(mActivity, false)
                setHeaderTitle(mHeaderText)
                setSideIconVisibility(true)
                mActivity?.let { setSideIcon(ContextCompat.getDrawable(it, R.drawable.ic_options_menu), this@CommonWebViewFragment) }
            }
        } else {
            ToolBarManager.getInstance().apply { hideToolBar(mActivity, true) }
        }
        commonWebView?.apply {
            val webViewController = WebViewController()
            webViewController.commonWebView = commonWebView
            webViewController.activity = mActivity
            commonWebView.webViewClient = webViewController
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            setLayerType(View.LAYER_TYPE_HARDWARE, null)
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(WebViewBridge(), "Android")
            Log.d(CommonWebViewFragment::class.simpleName, "onViewCreated: $mLoadUrl")
            triggerWebViewOpenEvent()
            loadUrl(mLoadUrl)
        }
        showProgressDialog(mActivity)
        WebViewBridge.mWebViewListener = this
    }

    private fun triggerWebViewOpenEvent() {
        val eventName =
            when {
                mLoadUrl.contains(WebViewUrls.WEB_VIEW_RE_ARRANGE) -> AFInAppEventType.EVENT_RE_ARRANGE_PAGE_OPEN
                mLoadUrl.contains(WebViewUrls.WEB_VIEW_HELP) -> AFInAppEventType.EVENT_HELP_SCREEN_OPEN
                else -> ""
            }
        AppEventsManager.pushAppEvents(
            eventName = eventName,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
    }

    override fun onNativeBackPressed() {
        mActivity?.let {
            it.runOnUiThread {
                it.onBackPressed()
            }
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
                shareOnWhatsApp("Order From - ${if (domain.isEmpty()) mDomainName else domain}", bitmap)
            } else {
                val imageBase64 = jsonData.optString("data")
                Log.d(mTagName, "image URL :: $imageBase64")
                val domain = jsonData.optString("domain")
                val bitmap = getBitmapFromBase64(imageBase64)
                shareData("Order From - ${if (domain.isEmpty()) mDomainName else domain}", bitmap)
            }
        } else if (jsonData.optBoolean("downloadImage")) {
            val base64OriginalStr = jsonData.optString("data")
            Log.d(mTagName, "image BASE64 :: $base64OriginalStr")
            val base64Str = base64OriginalStr.split("data:image/png;base64,")[1]
            Log.d(mTagName, "image URL :: $base64Str")
            val bitmap = getBitmapFromBase64V2(base64Str)
            bitmap?.let {
                downloadMediaToStorage(it, mActivity)
                val file = downloadBillInGallery(it, "my-qr")
                file?.run { showDownloadNotification(this, "MyQR") }
            }
        } else if (jsonData.optBoolean("redirectBrowser")) {
            openUrlInBrowser(jsonData.optString("data"))
        } else if (jsonData.optBoolean("scratchClaimed")) {
            mActivity?.launchInAppReviewDialog()
        } else if (jsonData.optBoolean("shareQRCode")) {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_QR_SHARED,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
            )
            val imageBase64 = jsonData.optString("data")
            Log.d(mTagName, "image URL :: $imageBase64")
            val domain = jsonData.optString("domain")
            val bitmap = getBitmapFromBase64(imageBase64)
            shareData("Order From - ${if (domain.isEmpty()) mDomainName else domain}", bitmap)
        } else if (jsonData.optBoolean("redirectHomePage")) {
            launchFragment(HomeFragment.newInstance(), true)
        } else if (jsonData.optBoolean("startLoader")) {
            showProgressDialog(mActivity)
        } else if (jsonData.optBoolean("unauthorizedAccess")) {
            logoutFromApplication()
        } else if (jsonData.optBoolean("openUPIIntent")) {
            val intent = Intent()
            intent.data = Uri.parse(jsonData.optString("data"))
            val chooser = Intent.createChooser(intent, "Pay with...")
            startActivityForResult(chooser, 1, null)
        } else if (jsonData.optBoolean("openUPI")) {
            val packageName = jsonData.optString("packageName")
            val uri: Uri = Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", jsonData.optString("pa"))
                .appendQueryParameter("pn", jsonData.optString("pn"))
                .appendQueryParameter("tr", jsonData.optString("tr"))
                .appendQueryParameter("tn", jsonData.optString("tn"))
                .appendQueryParameter("am", jsonData.optString("am"))
                .appendQueryParameter("cu", "INR")
                .build()
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = uri
            intent.setPackage(packageName)
            mActivity?.startActivityForResult(intent, 123)
        } else if (jsonData.optBoolean("convertImage")) {
            showProgressDialog(mActivity)
            val imageUrl = jsonData.optString("data")
            mDomainName = jsonData.optString("domain")
            val image64 = getBase64FromImageURL(imageUrl)
            Log.d(mTagName, "image BASE64 :: $image64")
            CoroutineScopeUtils().runTaskOnCoroutineMain {
                if ("social" == jsonData.optString("sharePage")) {
                    commonWebView?.loadUrl("javascript: receiveAndroidSocialData('$image64')")
                } else {
                    commonWebView?.loadUrl("javascript: receiveAndroidData('$image64')")
                }
            }
        } else if (jsonData.optBoolean("convertMultipleImage")) {
            showProgressDialog(mActivity)
            val imageArray = jsonData.optJSONArray("imageArray")
            val listType = object : TypeToken<ArrayList<ConvertMultiImageDTO>>() {}.type
            val convertMultipleImageList = Gson().fromJson<ArrayList<ConvertMultiImageDTO>>("$imageArray", listType)
            convertMultipleImageList?.forEachIndexed { _, imageDTO ->
                val str = getBase64FromImageURL(imageDTO.src)
                str?.run { imageDTO.src = "data:image/png;base64,$str" }
            }
            val finalConvertedStr = Gson().toJson(convertMultipleImageList)
            Log.d(mTagName, "image BASE64 :: $finalConvertedStr")
            CoroutineScopeUtils().runTaskOnCoroutineMain {
                commonWebView?.loadUrl("javascript: receiveAndroidData($finalConvertedStr)")
            }
        } else if (jsonData.optBoolean("trackEventData")) {
            val eventName = jsonData.optString("eventName")
            val additionalData = jsonData.optString("additionalData")
            val map = Gson().fromJson<HashMap<String, String>>(additionalData.toString(), HashMap::class.java)
            Log.d(mTagName, "sendData: working $map")
            AppEventsManager.pushAppEvents(
                eventName = eventName,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = map
            )
        }
    }

    class WebViewController : WebViewClient() {

        var activity: MainActivity? = null
        var commonWebView: WebView? = null
        private val mTagName = WebViewController::class.java.simpleName

        override fun onPageFinished(view: WebView?, url: String?) {
            try {
                Log.d("WebViewController", "onPageFinished: called")
                val contactListJson = Gson().toJson(StaticInstances.sUserContactList)
                commonWebView?.loadUrl("javascript: receiveContactData($contactListJson)")
                activity?.getCurrentFragment()?.stopProgress()
            } catch (e: Exception) {
                Log.e("WebViewController", "onPageFinished: ${e.message}", e)
            }
        }

        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return when {
                url.startsWith("tel:") -> {
                    try {
                        activity?.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
                    } catch (e: Exception) {
                        Log.e(mTagName, "shouldOverrideUrlLoading :: tel :: ${e.message}", e)
                    }
                    true
                }
                url.contains("mailto:") -> {
                    try {
                        view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        Log.e(mTagName, "shouldOverrideUrlLoading :: mailto :: ${e.message}", e)
                    }
                    true
                }
                url.contains("whatsapp:") -> {
                    try {
                        view.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } catch (e: Exception) {
                        Log.e(mTagName, "shouldOverrideUrlLoading :: whatsapp :: ${e.message}", e)
                    }
                    true
                }
                else -> {
                    view.loadUrl(url)
                    true
                }
            }
        }
    }

    override fun onToolbarSideIconClicked() {
        try {
            val sideView:View? = mActivity?.findViewById(R.id.sideIconToolbar)
            val optionsMenu: PopupMenu? = PopupMenu(mActivity, sideView)
            optionsMenu?.apply {
                inflate(R.menu.menu_product_fragment)
                menu?.add(Menu.NONE, 0, Menu .NONE, getString(R.string.term_and_condition))
                menu?.add(Menu.NONE, 1, Menu .NONE, getString(R.string.help))
                setOnMenuItemClickListener(this@CommonWebViewFragment)
            }?.show()
        } catch (e: Exception) {
            Log.e(mTagName, "onToolbarSideIconClicked: ${e.message}", e)
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        if (0 == item?.itemId) openWebViewFragment(this, getString(R.string.help), WebViewUrls.WEB_VIEW_TNC, Constants.SETTINGS)
        if (1 == item?.itemId) openWebViewFragment(this, getString(R.string.help), WebViewUrls.WEB_VIEW_HELP, Constants.SETTINGS)
        return true
    }

    override fun showAndroidToast(data: String) {
        showToast(data)
    }

    override fun showAndroidLog(data: String) {
        Log.d(mTagName, "showAndroidLog :: $data")
    }

    override fun onBackPressed(): Boolean {
        return try {
            Log.d(mTagName, "onBackPressed :: called")
            if(fragmentManager != null && fragmentManager?.backStackEntryCount == 1) {
                clearFragmentBackStack()
                launchFragment(HomeFragment.newInstance(), true)
                true
            } else {
                if (commonWebView?.canGoBack() == true) {
                    commonWebView?.goBack()
                    true
                } else false
            }
        } catch (e: Exception) {
            false
        }
    }
}