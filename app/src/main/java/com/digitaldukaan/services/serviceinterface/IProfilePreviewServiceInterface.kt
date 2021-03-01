package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.ProfilePreviewResponse
import com.digitaldukaan.models.response.StoreDescriptionResponse

interface IProfilePreviewServiceInterface {

    fun onProfilePreviewResponse(profilePreviewResponse: ProfilePreviewResponse)

    fun onStoreNameResponse(response: StoreDescriptionResponse)

    fun onStoreLinkResponse(response: StoreDescriptionResponse)

    fun onStoreLogoResponse(response: StoreDescriptionResponse)

    fun onProfilePreviewServerException(e: Exception)
}