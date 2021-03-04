package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.ProfileResponse
import com.digitaldukaan.models.response.ReferEarnOverWhatsAppResponse
import com.digitaldukaan.models.response.ReferEarnResponse
import com.digitaldukaan.models.response.StoreDeliveryStatusChangeResponse

interface IProfileServiceInterface {

    fun onProfileResponse(profileResponse: ProfileResponse)

    fun onReferAndEarnResponse(response: ReferEarnResponse)

    fun onReferAndEarnOverWhatsAppResponse(response: ReferEarnOverWhatsAppResponse)

    fun onChangeStoreAndDeliveryStatusResponse(response: StoreDeliveryStatusChangeResponse)

    fun onProfileDataException(e: Exception)
}