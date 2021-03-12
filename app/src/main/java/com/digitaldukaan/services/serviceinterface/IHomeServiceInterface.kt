package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.OrdersResponse
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.models.response.ValidateOtpResponse

interface IHomeServiceInterface {

    fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse)

    fun onUserAuthenticationResponse(authenticationUserResponse: ValidateOtpResponse)

    //fun onGetOrdersResponse(getOrderResponse: OrdersResponse)
    fun onGetOrdersResponse(getOrderResponse: CommonApiResponse)

    fun onCompletedOrdersResponse(getOrderResponse: OrdersResponse)

    fun onAnalyticsDataResponse(commonResponse: CommonApiResponse)

    fun onOrderPageInfoResponse(commonResponse: CommonApiResponse)

    fun onHomePageException(e: Exception)

}