package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.GenerateOtpResponse

interface ILoginServiceInterface {

    fun onGenerateOTPResponse(generateOtpResponse: GenerateOtpResponse)
    fun onGenerateOTPException(e: Exception)
}