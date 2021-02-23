package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.ProfilePreviewResponse

interface IProfilePreviewServiceInterface {

    fun onProfilePreviewResponse(profilePreviewResponse: ProfilePreviewResponse)

    fun onProfilePreviewServerException(e: Exception)
}