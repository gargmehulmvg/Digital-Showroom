package com.digitaldukaan.services.`interface`

import com.digitaldukaan.models.response.GenerateOtpResponse

interface ILoginServiceInterface {

    fun onGenerateOTPResponse(generateOtpResponse: GenerateOtpResponse)
}