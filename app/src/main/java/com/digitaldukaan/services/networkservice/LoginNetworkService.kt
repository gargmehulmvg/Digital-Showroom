package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.GenerateOtpRequest
import com.digitaldukaan.models.request.ValidateUserRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.GenerateOtpResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ILoginServiceInterface
import com.google.gson.Gson

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
                } else {
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), GenerateOtpResponse::class.java)
                        loginServiceInterface.onGenerateOTPResponse(errorResponse)
                    }
                }
            }
        } catch (e : Exception) {
            Log.e(LoginNetworkService::class.java.simpleName, "generateOTPServerCall: ", e)
            loginServiceInterface.onGenerateOTPException(e)
        }
    }

    suspend fun validateUserServerCall(
        request: ValidateUserRequest,
        loginServiceInterface: ILoginServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.validateUser(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { validateUserResponse -> loginServiceInterface.onValidateUserResponse(validateUserResponse) }
                } else {
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        loginServiceInterface.onValidateUserResponse(errorResponse)
                    }
                }
            }
        } catch (e : Exception) {
            Log.e(LoginNetworkService::class.java.simpleName, "validateUserServerCall: ", e)
            loginServiceInterface.onGenerateOTPException(e)
        }
    }

}