package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.AppShareDataResponse
import com.digitaldukaan.models.response.MarketingCardsItemResponse
import com.digitaldukaan.models.response.MarketingCardsResponse

interface IMarketingServiceInterface {

    fun onMarketingErrorResponse(e: Exception)

    fun onMarketingResponse(response: MarketingCardsResponse)

    fun onAppShareDataResponse(response: AppShareDataResponse)

    fun onMarketingItemClick(response: MarketingCardsItemResponse?)

}