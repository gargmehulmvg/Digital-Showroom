package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.models.response.ValidateOtpResponse
import okhttp3.ResponseBody

interface IHomeFragmentServiceInterface {

    fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse)

    fun onUserAuthenticationResponse(authenticationUserResponse: ValidateOtpResponse)

    fun onGetOrdersResponse(getOrderResponse: ResponseBody)

    fun onHomePageException(e: Exception)

}