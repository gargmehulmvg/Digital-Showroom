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
import com.digitaldukaan.constants.ToolBarManager
import kotlinx.android.synthetic.main.fragment_splash.*

class SplashFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.fragment_splash, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        appVersionTextView.text = StringBuilder().append("v.").append(BuildConfig.VERSION_CODE)
        Handler(Looper.getMainLooper()).postDelayed({
            launchFragment(LoginFragment(), true, splashLogoImageView)
        }, Constants.SPLASH_TIMER)
    }

    override fun onBackPressed(): Boolean {
        Log.d(SplashFragment::class.java.simpleName, "onBackPressed: do nothing")
        return true
    }

}