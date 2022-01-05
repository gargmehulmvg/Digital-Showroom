package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse

interface ISocialMediaServiceInterface {

    fun onSocialMediaPageInfoResponse(commonApiResponse: CommonApiResponse)

    fun onSocialMediaTemplateListResponse(commonApiResponse: CommonApiResponse)

    fun onSocialMediaTemplateFavouriteResponse(commonApiResponse: CommonApiResponse)

    fun onMarketingPageInfoResponse(commonApiResponse: CommonApiResponse)

    fun onSocialMediaException(e: Exception)
}