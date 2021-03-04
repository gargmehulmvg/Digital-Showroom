package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.StoreDescriptionResponse

interface IProfilePhotoServiceInterface {

    fun onStoreLogoResponse(response: StoreDescriptionResponse)

    fun onProfilePhotoServerException(e: Exception)
}