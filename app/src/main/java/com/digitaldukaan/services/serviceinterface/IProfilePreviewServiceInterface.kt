package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.ProfilePreviewResponse
import okhttp3.ResponseBody

interface IProfilePreviewServiceInterface {

    fun onProfilePreviewResponse(profilePreviewResponse: ProfilePreviewResponse)

    fun onStoreNameResponse(response: ResponseBody)

    fun onProfilePreviewServerException(e: Exception)
}