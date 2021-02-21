package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.models.response.ValidateOtpResponse

interface IOtpVerificationServiceInterface {

    fun onOTPVerificationSuccessResponse(validateOtpResponse: ValidateOtpResponse)

    fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse)

    fun onUserAuthenticationResponse(authenticationUserResponse: ValidateOtpResponse)

}