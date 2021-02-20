package com.digitaldukaan.services.networkservice

import com.digitaldukaan.models.request.ValidateOtpRequest
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IOtpVerificationServiceInterface
import com.google.gson.Gson

class OtpVerificationNetworkService {

    suspend fun verifyOTPServerCall(
        mobileNumber: String,
        otpStr: Int,
        otpVerificationServiceInterface: IOtpVerificationServiceInterface
    ) {
        val response = RetrofitApi().getServerCallObject()?.validateOTP(ValidateOtpRequest(otpStr, mobileNumber))
        response?.let {
            if (it.isSuccessful) {
                it.body()?.let { validateOtpSuccessResponse -> otpVerificationServiceInterface.onOTPVerificationSuccessResponse(validateOtpSuccessResponse) }
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