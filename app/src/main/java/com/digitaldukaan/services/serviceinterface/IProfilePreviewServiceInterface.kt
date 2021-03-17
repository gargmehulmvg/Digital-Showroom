package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.StoreDescriptionResponse

interface IProfilePreviewServiceInterface {

    fun onProfilePreviewResponse(commonApiResponse: CommonApiResponse)

    fun onStoreNameResponse(response: StoreDescriptionResponse)

    fun onStoreLinkResponse(response: StoreDescriptionResponse)

    fun onStoreLogoResponse(response: CommonApiResponse)

    fun onInitiateKycResponse(response: CommonApiResponse)

    fun onProfilePreviewServerException(e: Exception)
}