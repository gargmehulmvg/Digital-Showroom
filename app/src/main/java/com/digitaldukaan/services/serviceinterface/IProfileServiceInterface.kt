package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ReferEarnOverWhatsAppResponse

interface IProfileServiceInterface {

    fun onProfileResponse(profileResponse: CommonApiResponse)

    fun onReferAndEarnResponse(response: CommonApiResponse)

    fun onReferAndEarnOverWhatsAppResponse(response: ReferEarnOverWhatsAppResponse)

    fun onChangeStoreAndDeliveryStatusResponse(response: CommonApiResponse)

    fun onProfileDataException(e: Exception)
}