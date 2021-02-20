package com.digitaldukaan.services

import android.content.Context
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.LoginNetworkService
import com.digitaldukaan.services.serviceinterface.ILoginServiceInterface

class LoginService(private val context: Context) {

    private val mLoginNetworkService: LoginNetworkService = LoginNetworkService()

    private lateinit var mLoginServiceInterface: ILoginServiceInterface

    fun setLoginServiceInterface(loginServiceInterface: ILoginServiceInterface) {
        mLoginServiceInterface = loginServiceInterface
    }

    fun generateOTP(mobileNumber: String) {
        if (isInternetConnectionAvailable(context)) {
            mLoginServiceInterface.noInternetConnection()
        } else {
            CoroutineScopeUtils().runTaskOnCoroutineBackground {
                mLoginNetworkService.generateOTPServerCall(mobileNumber, mLoginServiceInterface)
            }
        }
    }
}