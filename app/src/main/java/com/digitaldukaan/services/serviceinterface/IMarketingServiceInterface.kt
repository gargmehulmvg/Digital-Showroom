package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.MarketingCardsItemResponse

interface IMarketingServiceInterface {

    fun onMarketingErrorResponse(e: Exception)

    fun onMarketingResponse(response: CommonApiResponse)

    fun onMarketingPageInfoResponse(response: CommonApiResponse)

    fun onAppShareDataResponse(response: CommonApiResponse)

    fun onGenerateStorePdfResponse(response: CommonApiResponse)

    fun onShareStorePdfDataResponse(response: CommonApiResponse)

    fun onMarketingItemClick(response: MarketingCardsItemResponse?)

    fun onMarketingSuggestedDomainsResponse(response: CommonApiResponse)

}