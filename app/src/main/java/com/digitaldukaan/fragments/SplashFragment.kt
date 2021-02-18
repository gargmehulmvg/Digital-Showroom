package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.interfaces.IOnBackPressedListener
import kotlinx.android.synthetic.main.fragment_splash.*

class SplashFragment : BaseFragment(), IOnBackPressedListener {

    private val mTagName = SplashFragment::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mNavController = findNavController()
    }

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
        mActivity.hideToolbarView(false)
        Handler(Looper.getMainLooper()).postDelayed({
            val extras = FragmentNavigatorExtras(splashLogoImageView to "transitionName")
            mNavController.navigate(R.id.onBoardAuthenticationFragment, null, null, extras)
        }, Constants.SPLASH_TIMER)
    }

    override fun onBackPressedToExit() {
        Log.d(mTagName, "onBackPressedToExit: do nothing")
    }

}