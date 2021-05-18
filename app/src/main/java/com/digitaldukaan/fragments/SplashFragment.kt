package com.digitaldukaan.fragments

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.app.ActivityCompat
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.response.AppVersionResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.HelpScreenItemResponse
import com.digitaldukaan.models.response.StaticData
import com.digitaldukaan.services.SplashService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ISplashServiceInterface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SplashFragment : BaseFragment(), ISplashServiceInterface {

    private var mIntentUri: Uri? = null

    companion object {
        private const val TAG = "SplashFragment"
        private val splashService: SplashService = SplashService()
        private var appUpdateDialog: Dialog? = null

        fun newInstance(intentUri: Uri?): SplashFragment {
            val fragment = SplashFragment()
            fragment.mIntentUri = intentUri
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_splash_fragment, container, false)
        hideBottomNavigationView(true)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        fetchContactsIfPermissionGranted()
        splashService.setSplashServiceInterface(this)
        splashService.getStaticData("1")
    }

    private fun fetchContactsIfPermissionGranted() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mActivity?.let {
                if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    getContactsFromStorage2(mActivity)
                }
            }
        }
    }

    override fun onBackPressed(): Boolean {
        mActivity?.finish()
        return true
    }

    override fun onStaticDataResponse(staticDataResponse: CommonApiResponse) {
        val staticData = Gson().fromJson<StaticData>(staticDataResponse.mCommonDataStr, StaticData::class.java)
        StaticInstances.mStaticData = staticData
        splashService.getAppVersion()
    }

    override fun onHelpScreenResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                val listType = object : TypeToken<List<HelpScreenItemResponse>>() {}.type
                StaticInstances.sHelpScreenList = Gson().fromJson<ArrayList<HelpScreenItemResponse>>(commonResponse.mCommonDataStr, listType)
                launchHomeFragment()
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onAppVersionResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                val playStoreLinkStr = Gson().fromJson<AppVersionResponse>(commonResponse.mCommonDataStr, AppVersionResponse::class.java)
                if (playStoreLinkStr.mIsActive) splashService.getHelpScreens() else showVersionUpdateDialog()
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    private fun launchHomeFragment() {
        when {
            mIntentUri != null -> switchToFragmentByDeepLink()
            "" == getStringDataFromSharedPref(Constants.STORE_ID) -> launchFragment(
                OnBoardHelpScreenFragment.newInstance(),
                true
            )
            else -> launchFragment(HomeFragment.newInstance(), true)
        }
    }

    override fun onStaticDataException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onNoInternetButtonClick(isNegativeButtonClick: Boolean) {
        mActivity?.finish()
    }

    private fun showVersionUpdateDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (appUpdateDialog?.isShowing == true) return@runTaskOnCoroutineMain
            mActivity?.let {
                appUpdateDialog = Dialog(it)
                val view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_app_update, null)
                appUpdateDialog?.apply {
                    setContentView(view)
                    setCancelable(false)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    view?.run {
                        val updateTextView: TextView = findViewById(R.id.updateTextView)
                        updateTextView.setOnClickListener {
                            (this@apply).dismiss()
                            openPlayStore()
                        }
                    }
                }?.show()
            }
        }
    }

    private fun switchToFragmentByDeepLink() {
        val intentUriStr = mIntentUri.toString()
        Log.d(TAG, "switchToFragmentByDeepLink:: $intentUriStr")
        clearFragmentBackStack()
        when {
            intentUriStr.contains("digitaldukaan://Settings") -> launchFragment(SettingsFragment.newInstance(), true)
            intentUriStr.contains("digitaldukaan://ProfilePage") -> launchFragment(ProfilePreviewFragment().newInstance(), true)
            intentUriStr.contains("digitaldukaan://ProductList") -> launchFragment(HomeFragment.newInstance(), true)
            intentUriStr.contains("digitaldukaan://OrderList") -> launchFragment(HomeFragment.newInstance(), true)
            intentUriStr.contains("digitaldukaan://ProductAdd") -> launchFragment(ProductFragment.newInstance(), true)
            intentUriStr.contains("digitaldukaan://MarketingBroadCast") -> launchFragment(MarketingFragment.newInstance(), true)
            intentUriStr.contains("digitaldukaan://OTP") -> launchFragment(LoginFragment.newInstance(), true)
            intentUriStr.contains("digitaldukaan://Webview") -> {
                try {
                    var webViewUrl = intentUriStr.split("digitaldukaan://Webview?webURL=")[1]
                    if (webViewUrl.contains("&")) webViewUrl = webViewUrl.split("&")[0]
                    openWebViewFragment(this, "", "${BuildConfig.WEB_VIEW_URL}$webViewUrl")
                } catch (e: Exception) {
                    Log.e(TAG, "switchToFragmentByDeepLink: ${e.message}", e)
                }
            }
            else -> {
                mIntentUri = null
                launchHomeFragment()
            }
        }
    }

}