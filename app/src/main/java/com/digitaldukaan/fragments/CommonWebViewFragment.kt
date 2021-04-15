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
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.constants.openWebViewFragment
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.webviews.WebViewBridge
import kotlinx.android.synthetic.main.layout_common_webview_fragment.*

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
        WebViewBridge.mWebViewListener = this
    }

    override fun onNativeBackPressed() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.onBackPressed()
        }
    }

    override fun sendData(data: String) {
        Log.d(mTagName, "sendData: $data")
    }

    class WebViewController : WebViewClient() {

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

}