package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.GenerateOtpResponse

interface ILoginServiceInterface {

    fun onGenerateOTPResponse(generateOtpResponse: GenerateOtpResponse)

    fun onValidateUserResponse(validateUserResponse: CommonApiResponse)

    fun onGenerateOTPException(e: Exception)
}