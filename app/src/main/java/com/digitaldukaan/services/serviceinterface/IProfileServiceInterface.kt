package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ReferEarnOverWhatsAppResponse
import com.digitaldukaan.models.response.ReferEarnResponse
import com.digitaldukaan.models.response.StoreDeliveryStatusChangeResponse

interface IProfileServiceInterface {

    fun onProfileResponse(profileResponse: CommonApiResponse)

    fun onReferAndEarnResponse(response: ReferEarnResponse)

    fun onReferAndEarnOverWhatsAppResponse(response: ReferEarnOverWhatsAppResponse)

    fun onChangeStoreAndDeliveryStatusResponse(response: StoreDeliveryStatusChangeResponse)

    fun onProfileDataException(e: Exception)
}