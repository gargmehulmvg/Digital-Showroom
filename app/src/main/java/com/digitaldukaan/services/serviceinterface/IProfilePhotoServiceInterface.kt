package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface IProfilePhotoServiceInterface {

    fun onStoreLogoResponse(response: CommonApiResponse)

    fun onImageCDNLinkGenerateResponse(response: CommonApiResponse)

    fun onProfilePhotoServerException(e: Exception)
}