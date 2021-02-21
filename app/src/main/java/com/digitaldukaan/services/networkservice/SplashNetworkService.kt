package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.GenerateOtpRequest
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ILoginServiceInterface

class LoginNetworkService {

    suspend fun generateOTPServerCall(
        mobileNumber: String,
        loginServiceInterface: ILoginServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.generateOTP(GenerateOtpRequest("Digital Dukaan", mobileNumber))
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { generateOtpResponse -> loginServiceInterface.onGenerateOTPResponse(generateOtpResponse) }
                }
            }
        } catch (e : Exception) {
            Log.e(LoginNetworkService::class.java.simpleName, "generateOTPServerCall: ", e)
            loginServiceInterface.onGenerateOTPException(e)
        }
    }

}