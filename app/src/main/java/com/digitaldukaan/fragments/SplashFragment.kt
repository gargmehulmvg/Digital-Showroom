package com.digitaldukaan.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

    companion object {
        fun newInstance(): SplashFragment{
            return SplashFragment()
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
        if ("" == getStringDataFromSharedPref(Constants.STORE_ID)) launchFragment(
            LoginFragment.newInstance(),
            true
        ) else launchFragment(HomeFragment.newInstance(), true)
    }

    override fun onStaticDataException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}