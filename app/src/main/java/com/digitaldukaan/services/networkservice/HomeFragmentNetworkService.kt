package com.digitaldukaan.services.networkservice

import com.digitaldukaan.models.request.AuthenticateUserRequest
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IHomeFragmentServiceInterface
import com.google.gson.Gson

class HomeFragmentNetworkService {

    suspend fun authenticateUserServerCall(authToken: String, otpVerificationServiceInterface: IHomeFragmentServiceInterface) {
        val response = RetrofitApi().getServerCallObject()?.authenticateUser(AuthenticateUserRequest(authToken))
        response?.let {
            if (it.isSuccessful) {
                it.body()?.let { validateUserResponse -> otpVerificationServiceInterface.onUserAuthenticationResponse(validateUserResponse) }
            } else {
                val validateOtpError = it.errorBody()
                validateOtpError?.let {
                    val validateOtpErrorResponse = Gson().fromJson(validateOtpError.string(), ValidateOtpErrorResponse::class.java)
                    otpVerificationServiceInterface.onOTPVerificationErrorResponse(validateOtpErrorResponse)
                }
            }
        }
    }

}