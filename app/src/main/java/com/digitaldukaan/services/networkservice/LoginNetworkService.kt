package com.digitaldukaan.services.networkservice

import com.digitaldukaan.models.request.GenerateOtpRequest
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.`interface`.ILoginServiceInterface

class LoginNetworkService {

    suspend fun generateOTPServerCall(
        mobileNumber: String,
        loginServiceInterface: ILoginServiceInterface
    ) {
        val response = RetrofitApi().getServerCallObject()?.generateOTP(GenerateOtpRequest("Digital Dukaan", mobileNumber))
        response?.let {
            if (it.isSuccessful) {
                it.body()?.let { generateOtpResponse -> loginServiceInterface.onGenerateOTPResponse(generateOtpResponse) }
            }
        }
    }

}