package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.models.response.ValidateOtpResponse

interface IHomeServiceInterface {

    fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse)

    fun onUserAuthenticationResponse(authenticationUserResponse: ValidateOtpResponse)

    fun onPendingOrdersResponse(getOrderResponse: CommonApiResponse)

    fun onCompletedOrdersResponse(getOrderResponse: CommonApiResponse)

    fun onAnalyticsDataResponse(commonResponse: CommonApiResponse)

    fun onOrderPageInfoResponse(commonResponse: CommonApiResponse)

    fun onSearchOrdersResponse(commonResponse: CommonApiResponse)

    fun onOrdersUpdatedStatusResponse(commonResponse: CommonApiResponse)

    fun onCompleteOrderStatusResponse(commonResponse: CommonApiResponse)

    fun onCustomDomainBottomSheetDataResponse(commonResponse: CommonApiResponse)

    fun onLandingPageCardsResponse(commonResponse: CommonApiResponse)

    fun onDomainSuggestionListResponse(commonResponse: CommonApiResponse)

    fun onHomePageException(e: Exception)

    fun checkStaffInviteResponse(commonResponse: CommonApiResponse)

    fun onGetCartsByFiltersResponse(commonResponse: CommonApiResponse)

    fun onCartFilterOptionsResponse(commonResponse: CommonApiResponse)

}