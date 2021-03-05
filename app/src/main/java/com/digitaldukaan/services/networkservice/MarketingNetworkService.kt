package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IMarketingServiceInterface

class MarketingNetworkService {

    suspend fun getMarketingCardsDataServerCall(
        serviceInterface: IMarketingServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getMarketingCardsData()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { generateOtpResponse -> serviceInterface.onMarketingResponse(generateOtpResponse) }
                } else serviceInterface.onMarketingErrorResponse(Exception(response.message()))
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getMarketingCardsDataServerCall: ", e)
            serviceInterface.onMarketingErrorResponse(e)
        }
    }

}