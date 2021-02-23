package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.StaticTextResponse
import com.digitaldukaan.services.SplashService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ISplashServiceInterface
import kotlinx.android.synthetic.main.fragment_splash.*

class SplashFragment : BaseFragment(), ISplashServiceInterface {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.fragment_splash, container, false)
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
        val splashService = SplashService()
        splashService.setSplashServiceInterface(this)
        splashService.getStaticData("0")
    }

    override fun onBackPressed(): Boolean {
        mActivity.finish()
        return true
    }

    override fun onStaticDataResponse(staticDataResponse: StaticTextResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mStaticData = staticDataResponse
            //launchFragment(LoginFragment(), true, splashLogoImageView)
            launchFragment(HomeFragment.newInstance(), true)
        }
    }

    override fun onStaticDataException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}