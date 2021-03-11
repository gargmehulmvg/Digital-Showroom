package com.digitaldukaan.services.serviceinterface

import com.digitaldukaan.models.response.AppShareDataResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.MarketingCardsItemResponse
import com.digitaldukaan.models.response.ShareStorePDFDataResponse
import okhttp3.ResponseBody

interface IMarketingServiceInterface {

    fun onMarketingErrorResponse(e: Exception)

    fun onMarketingResponse(response: CommonApiResponse)

    fun onAppShareDataResponse(response: AppShareDataResponse)

    fun onGenerateStorePdfResponse(response: ResponseBody)

    fun onShareStorePdfDataResponse(response: ShareStorePDFDataResponse)

    fun onMarketingItemClick(response: MarketingCardsItemResponse?)

}