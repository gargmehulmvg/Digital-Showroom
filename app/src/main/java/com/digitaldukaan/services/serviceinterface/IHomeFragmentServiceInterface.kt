package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.models.response.ValidateOtpResponse

interface IHomeFragmentServiceInterface {

    fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse)

    fun onUserAuthenticationResponse(authenticationUserResponse: ValidateOtpResponse)

}