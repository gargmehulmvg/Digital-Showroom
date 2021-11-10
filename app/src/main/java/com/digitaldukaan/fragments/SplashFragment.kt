package com.digitaldukaan.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.response.AppVersionResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.HelpScreenItemResponse
import com.digitaldukaan.models.response.StaticTextResponse
import com.digitaldukaan.services.SplashService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ISplashServiceInterface
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SplashFragment : BaseFragment(), ISplashServiceInterface {

    private var mIntentUri: Uri? = null
    private val mSplashService: SplashService = SplashService()

    companion object {
        fun newInstance(intentUri: Uri?): SplashFragment {
            val fragment = SplashFragment()
            fragment.mIntentUri = intentUri
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "SplashFragment"
        mContentView = inflater.inflate(R.layout.layout_splash_fragment, container, false)
        hideBottomNavigationView(true)
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        Handler(Looper.getMainLooper()).postDelayed({
            if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
            } else mSplashService.getStaticData("1")
        }, Constants.TIMER_INTERVAL)
        fetchContactsIfPermissionGranted()
        mSplashService.setSplashServiceInterface(this)
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
        val staticData = Gson().fromJson<StaticTextResponse>(staticDataResponse.mCommonDataStr, StaticTextResponse::class.java)
        StaticInstances.sStaticData = staticData
        mSplashService.getAppVersion()
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
                if (playStoreLinkStr.mIsActive) mSplashService.getHelpScreens() else showVersionUpdateDialog()
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    private fun launchHomeFragment() = when {
        null != mIntentUri -> switchToFragmentByDeepLink()
        "" == getStringDataFromSharedPref(Constants.STORE_ID) -> launchFragment(LoginFragmentV2.newInstance(), true)
        else -> launchFragment(OrderFragment.newInstance(), true)
    }

    override fun onStaticDataException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onNoInternetButtonClick(isNegativeButtonClick: Boolean) {
        mActivity?.finish()
    }

    private fun switchToFragmentByDeepLink() {
        val intentUriStr = mIntentUri.toString()
        Log.d(TAG, "switchToFragmentByDeepLink:: $intentUriStr")
        val deepLinkStr = "digitaldukaan://"
        clearFragmentBackStack()
        when {
            intentUriStr.contains("${deepLinkStr}Settings") -> launchFragment(SettingsFragment.newInstance(), true)
            intentUriStr.contains("${deepLinkStr}ProfilePage") -> launchFragment(ProfilePreviewFragment.newInstance(), true)
            intentUriStr.contains("${deepLinkStr}ProductList") -> launchFragment(OrderFragment.newInstance(), true)
            intentUriStr.contains("${deepLinkStr}OrderList") -> launchFragment(OrderFragment.newInstance(), true)
            intentUriStr.contains("${deepLinkStr}ProductAdd") -> launchFragment(ProductFragment.newInstance(), true)
            intentUriStr.contains("${deepLinkStr}MarketingBroadCast") -> launchFragment(MarketingFragment.newInstance(), true)
            intentUriStr.contains("${deepLinkStr}OTP") -> launchFragment(LoginFragmentV2.newInstance(), true)
            intentUriStr.contains("${deepLinkStr}PaymentOptions") -> launchFragment(PaymentModesFragment.newInstance(), true)
            intentUriStr.contains("${deepLinkStr}Webview") -> {
                try {
                    var webViewUrl = intentUriStr.split("${deepLinkStr}Webview?webURL=")[1]
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