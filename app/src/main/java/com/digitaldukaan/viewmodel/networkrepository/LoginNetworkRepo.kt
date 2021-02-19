package com.digitaldukaan.viewmodel.networkrepository

import androidx.lifecycle.MutableLiveData
import com.digitaldukaan.models.request.GenerateOtpRequest
import com.digitaldukaan.models.response.GenerateOtpResponse
import com.digitaldukaan.network.RetrofitApi

class LoginNetworkRepo {

    suspend fun generateOTPServerCall(
        mobileNumber: String,
        otpLiveData: MutableLiveData<GenerateOtpResponse>
    ) {
        val response = RetrofitApi().getServerCallObject()?.generateOTP(GenerateOtpRequest("Digital Dukaan", mobileNumber))
        response?.let {
            if (it.isSuccessful) {
                it.body()?.let { generateOtpResponse -> otpLiveData.postValue(generateOtpResponse) }
            }
        }
    }

}