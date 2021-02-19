package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.`interface`.ILoginServiceInterface
import com.digitaldukaan.services.networkservice.LoginNetworkService

class LoginService {

    private val mLoginNetworkService: LoginNetworkService = LoginNetworkService()

    private lateinit var mLoginServiceInterface: ILoginServiceInterface

    fun setLoginServiceInterface(loginServiceInterface: ILoginServiceInterface) {
        mLoginServiceInterface = loginServiceInterface
    }

    fun generateOTP(mobileNumber: String) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mLoginNetworkService.generateOTPServerCall(mobileNumber, mLoginServiceInterface)
        }
    }

}