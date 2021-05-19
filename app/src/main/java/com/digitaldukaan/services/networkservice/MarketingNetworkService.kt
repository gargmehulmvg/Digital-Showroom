package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
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
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) {
                        throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    }
                    serviceInterface.onMarketingErrorResponse(Exception(response.message()))
                }
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getMarketingCardsDataServerCall: ", e)
            serviceInterface.onMarketingErrorResponse(e)
        }
    }

    suspend fun getShareStoreDataServerCall(
        serviceInterface: IMarketingServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getShareStore()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody -> serviceInterface.onAppShareDataResponse(responseBody) }
                } else serviceInterface.onMarketingErrorResponse(Exception(response.message()))
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getShareStoreDataServerCall: ", e)
            serviceInterface.onMarketingErrorResponse(e)
        }
    }

    suspend fun generateStorePdfServerCall(
        serviceInterface: IMarketingServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.generateStorePdf()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody -> serviceInterface.onGenerateStorePdfResponse(responseBody) }
                } else serviceInterface.onMarketingErrorResponse(Exception(response.message()))
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getShareStoreDataServerCall: ", e)
            serviceInterface.onMarketingErrorResponse(e)
        }
    }

    suspend fun getShareStorePdfTextServerCall(
        serviceInterface: IMarketingServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getShareStorePdfText()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody -> serviceInterface.onShareStorePdfDataResponse(responseBody) }
                } else serviceInterface.onMarketingErrorResponse(Exception(response.message()))
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getShareStoreDataServerCall: ", e)
            serviceInterface.onMarketingErrorResponse(e)
        }
    }

}