package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.models.response.ValidateOtpResponse

interface IOtpVerificationServiceInterface {

    fun onOTPVerificationSuccessResponse(validateOtpResponse: ValidateOtpResponse)

    fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse)

    fun onGetOtpModesListResponse(commonApiResponse: CommonApiResponse)

    fun onOTPVerificationDataException(e: Exception)

    fun onGetOtpModesListDataException(e: Exception)

}