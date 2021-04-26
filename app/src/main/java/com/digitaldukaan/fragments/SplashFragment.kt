package com.digitaldukaan.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.constants.getContactsFromStorage2
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.StaticTextResponse
import com.digitaldukaan.services.SplashService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ISplashServiceInterface
import kotlinx.android.synthetic.main.layout_splash_fragment.*

class SplashFragment : BaseFragment(), ISplashServiceInterface {

    private var mIntentUri: Uri? = null

    companion object {
        private const val TAG = "SplashFragment"
        private val splashService: SplashService = SplashService()

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
        appVersionTextView.text = StringBuilder().append("v.").append(BuildConfig.VERSION_CODE)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        fetchContactsIfPermissionGranted()
        splashService.setSplashServiceInterface(this)
        splashService.getHelpScreens()
        splashService.getStaticData("0")
    }

    private fun fetchContactsIfPermissionGranted() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                getContactsFromStorage2(mActivity)
            }
        }
    }

    override fun onBackPressed(): Boolean {
        mActivity.finish()
        return true
    }

    override fun onStaticDataResponse(staticDataResponse: StaticTextResponse) {
        mStaticData = staticDataResponse
        splashService.getAppVersion()
    }

    override fun onAppVersionResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            launchHomeFragment()
//            if (commonResponse.mIsSuccessStatus) {
//                val playStoreLinkStr = Gson().fromJson<AppVersionResponse>(commonResponse.mCommonDataStr, AppVersionResponse::class.java)
//                if (!playStoreLinkStr.mIsActive) {
//                    openPlayStore()
//                } else launchHomeFragment()
//            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
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
        mActivity.finish()
    }

    private fun switchToFragmentByDeepLink() {
        Log.d(TAG, "switchToFragmentByDeepLink: $mIntentUri")
        when(mIntentUri.toString()) {
            "digitaldukaan://Settings" -> launchFragment(SettingsFragment.newInstance(), true)
            "digitaldukaan://ProfilePage" -> launchFragment(ProfilePreviewFragment().newInstance(""), true)
            "digitaldukaan://ProductList" -> launchFragment(HomeFragment.newInstance(), true)
            "digitaldukaan://OrderList" -> launchFragment(HomeFragment.newInstance(), true)
            "digitaldukaan://ProductAdd" -> launchFragment(ProductFragment.newInstance(), true)
            "digitaldukaan://MarketingBroadCast" -> launchFragment(MarketingFragment.newInstance(), true)
            "digitaldukaan://OTP" -> launchFragment(LoginFragment.newInstance(), true)
        }
    }

}