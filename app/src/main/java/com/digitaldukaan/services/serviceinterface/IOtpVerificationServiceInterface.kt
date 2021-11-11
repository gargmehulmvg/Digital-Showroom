package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ValidateOtpErrorResponse

interface IOtpVerificationServiceInterface {

    fun onOTPVerificationSuccessResponse(commonApiResponse: CommonApiResponse)

    fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse)

    fun onGetOtpModesListResponse(commonApiResponse: CommonApiResponse)

    fun onOTPVerificationDataException(e: Exception)

    fun onGetOtpModesListDataException(e: Exception)

}