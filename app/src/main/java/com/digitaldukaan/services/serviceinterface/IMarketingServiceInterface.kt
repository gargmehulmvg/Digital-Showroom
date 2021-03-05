package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.MarketingCardsResponse

interface IMarketingServiceInterface {

    fun onMarketingErrorResponse(e: Exception)

    fun onMarketingResponse(response: MarketingCardsResponse)

}