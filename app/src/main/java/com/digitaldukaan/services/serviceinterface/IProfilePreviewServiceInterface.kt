package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.GenerateOtpResponse

interface IProfilePreviewServiceInterface {

    fun onProfilePreviewResponse(commonApiResponse: CommonApiResponse)

    fun onStoreNameResponse(response: CommonApiResponse)

    fun onStoreLinkResponse(response: CommonApiResponse)

    fun onStoreLogoResponse(response: CommonApiResponse)

    fun onImageCDNLinkGenerateResponse(response: CommonApiResponse)

    fun onInitiateKycResponse(response: CommonApiResponse)

    fun onProfilePreviewServerException(e: Exception)

    fun onAppShareDataResponse(apiResponse: CommonApiResponse)

    fun onStoreUserPageInfoResponse(apiResponse: CommonApiResponse)

    fun onSetStoreUserDetailsResponse(apiResponse: CommonApiResponse)

    fun onSetGstResponse(apiResponse: CommonApiResponse)

    fun onGenerateOTPResponse(generateOtpResponse: GenerateOtpResponse)

    fun onVerifyDisplayPhoneNumberResponse(apiResponse: CommonApiResponse)
}