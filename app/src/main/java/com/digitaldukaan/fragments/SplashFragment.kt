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
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.constants.getContactsFromStorage2
import com.digitaldukaan.models.response.StaticTextResponse
import com.digitaldukaan.services.SplashService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ISplashServiceInterface
import kotlinx.android.synthetic.main.layout_splash_fragment.*

class SplashFragment : BaseFragment(), ISplashServiceInterface {

    private var mIntentUri: Uri? = null

    companion object {
        private const val TAG = "SplashFragment"
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
        val splashService = SplashService()
        splashService.setSplashServiceInterface(this)
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
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            Handler(Looper.getMainLooper()).postDelayed({
                mStaticData = staticDataResponse
                stopProgress()
                launchHomeFragment()
            }, Constants.SPLASH_TIMER)
        }
    }

    private fun launchHomeFragment() {
        when {
            mIntentUri != null -> switchToFragmentByDeepLink()
            "" == getStringDataFromSharedPref(Constants.STORE_ID) -> launchFragment(
                LoginFragment.newInstance(),
                true
            )
            else -> launchFragment(HomeFragment.newInstance(), true)
        }
    }

    override fun onStaticDataException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
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