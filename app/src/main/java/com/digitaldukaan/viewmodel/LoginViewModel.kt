package com.digitaldukaan.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.response.GenerateOtpResponse
import com.digitaldukaan.viewmodel.networkrepository.LoginNetworkRepo

class LoginViewModel : ViewModel() {

    private val mLoginNetworkRepo : LoginNetworkRepo = LoginNetworkRepo()

    var mGenerateOtpResponse: MutableLiveData<GenerateOtpResponse> = MutableLiveData()

    fun generateOTP(mobileNumber:String) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mLoginNetworkRepo.generateOTPServerCall(mobileNumber, mGenerateOtpResponse)
        }
    }

}