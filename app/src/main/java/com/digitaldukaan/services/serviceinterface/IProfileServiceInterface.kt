package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.ProfileResponse
import com.digitaldukaan.models.response.StoreDeliveryStatusChangeResponse

interface IProfileServiceInterface {

    fun onProfileResponse(profileResponse: ProfileResponse)

    fun onChangeStoreAndDeliveryStatusResponse(response: StoreDeliveryStatusChangeResponse)

    fun onProfileDataException(e: Exception)
}