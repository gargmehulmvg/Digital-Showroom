package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface ISocialMediaServiceInterface {

    fun onSocialMediaPageInfoResponse(commonApiResponse: CommonApiResponse)

    fun onSocialMediaException(e: Exception)
}