package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.LoginNetworkService
import com.digitaldukaan.services.serviceinterface.ILoginServiceInterface

class LoginService {

    private val mLoginNetworkService: LoginNetworkService = LoginNetworkService()

    private lateinit var mLoginServiceInterface: ILoginServiceInterface

    fun setLoginServiceInterface(loginServiceInterface: ILoginServiceInterface) {
        mLoginServiceInterface = loginServiceInterface
    }

    fun generateOTP(mobileNumber: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mLoginNetworkService.generateOTPServerCall(mobileNumber, mLoginServiceInterface)
        }
    }
}