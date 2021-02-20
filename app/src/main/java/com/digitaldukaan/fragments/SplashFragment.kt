package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigatorExtras
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.ToolBarManager
import kotlinx.android.synthetic.main.fragment_splash.*

class SplashFragment : BaseFragment() {

    private val mTagName = SplashFragment::class.simpleName

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.fragment_splash, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        Handler(Looper.getMainLooper()).postDelayed({
            val extras = FragmentNavigatorExtras(splashLogoImageView to "transitionName")
            //mNavController.navigate(R.id.onBoardAuthenticationFragment, null, null, extras)
            launchFragment(LoginFragment(), true)
        }, Constants.SPLASH_TIMER)
    }

    override fun onBackPressed(): Boolean {
        Log.d(mTagName, "onBackPressed: do nothing")
        return true
    }

}