package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ReferEarnOverWhatsAppResponse
import com.digitaldukaan.models.response.ReferEarnResponse

interface IProfileServiceInterface {

    fun onProfileResponse(profileResponse: CommonApiResponse)

    fun onReferAndEarnResponse(response: ReferEarnResponse)

    fun onReferAndEarnOverWhatsAppResponse(response: ReferEarnOverWhatsAppResponse)

    fun onChangeStoreAndDeliveryStatusResponse(response: CommonApiResponse)

    fun onProfileDataException(e: Exception)
}