package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.models.response.ValidateOtpResponse
import com.digitaldukaan.services.HomeFragmentService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IHomeFragmentServiceInterface
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.otp_verification_fragment.*

class HomeFragment : BaseFragment(), IHomeFragmentServiceInterface{

    private var mIsDoublePressToExit = false
    private lateinit var mHomeFragmentService: HomeFragmentService

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mHomeFragmentService = HomeFragmentService()
        mHomeFragmentService.setHomeFragmentServiceListener(this)
        mContentView = inflater.inflate(R.layout.home_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, true)
            setHeaderTitle(getString(R.string.app_name))
            setSideIconVisibility(false)
        }
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
        } else {
            showProgressDialog(mActivity, getString(R.string.authenticating_user))
            CoroutineScopeUtils().runTaskOnCoroutineBackground {
                mHomeFragmentService.verifyUserAuthentication(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            settingsImageView.id -> launchFragment(SettingsFragment.newInstance(), true)
        }
    }

    override fun onBackPressed(): Boolean {
        if (mIsDoublePressToExit) mActivity.finish()
        showShortSnackBar(getString(R.string.msg_back_press))
        mIsDoublePressToExit = true
        Handler(Looper.getMainLooper()).postDelayed(
            { mIsDoublePressToExit = false },
            Constants.BACK_PRESS_INTERVAL
        )
        return true
    }

    override fun onUserAuthenticationResponse(authenticationUserResponse: ValidateOtpResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            saveUserDetailsInPref(authenticationUserResponse)
            if (!authenticationUserResponse.mIsSuccessStatus) showShortSnackBar(authenticationUserResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    private fun saveUserDetailsInPref(validateOtpResponse: ValidateOtpResponse) {
        storeStringDataInSharedPref(Constants.USER_AUTH_TOKEN, validateOtpResponse.mUserAuthToken)
        storeStringDataInSharedPref(Constants.USER_MOBILE_NUMBER, validateOtpResponse.mUserPhoneNumber)
        validateOtpResponse.mStore?.run {
            storeStringDataInSharedPref(Constants.STORE_ID, storeId.toString())
            storeStringDataInSharedPref(Constants.STORE_NAME, storeInfo.name)
            if (this.storeInfo.logoImage?.isNotEmpty() == true) StaticInstances.sIsStoreImageUploaded = true
        }
    }

    override fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            otpEditText.clearOTP()
            showToast(validateOtpErrorResponse.mMessage)
        }
    }

}