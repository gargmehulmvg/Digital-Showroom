package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IMarketingServiceInterface

class MarketingNetworkService {

    suspend fun getMarketingSuggestedDomainsServerCall(serviceInterface: IMarketingServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getMarketingSuggestedDomains()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { generateOtpResponse -> serviceInterface.onMarketingSuggestedDomainsResponse(generateOtpResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    serviceInterface.onMarketingErrorResponse(Exception(response.message()))
                }
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getMarketingSuggestedDomainsServerCall: ", e)
            serviceInterface.onMarketingErrorResponse(e)
        }
    }

    suspend fun getMarketingPageInfoServerCall(serviceInterface: IMarketingServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getStoreMarketingPageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { generateOtpResponse -> serviceInterface.onMarketingPageInfoResponse(generateOtpResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    serviceInterface.onMarketingErrorResponse(Exception(response.message()))
                }
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getMarketingPageInfoServerCall: ", e)
            serviceInterface.onMarketingErrorResponse(e)
        }
    }

    suspend fun getShareStoreDataServerCall(serviceInterface: IMarketingServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getShareStore()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody -> serviceInterface.onAppShareDataResponse(responseBody) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    serviceInterface.onMarketingErrorResponse(Exception(response.message()))
                }
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getShareStoreDataServerCall: ", e)
            serviceInterface.onMarketingErrorResponse(e)
        }
    }

    suspend fun generateStorePdfServerCall(serviceInterface: IMarketingServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.generateStorePdf()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody -> serviceInterface.onGenerateStorePdfResponse(responseBody) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    serviceInterface.onMarketingErrorResponse(Exception(response.message()))
                }
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getShareStoreDataServerCall: ", e)
            serviceInterface.onMarketingErrorResponse(e)
        }
    }

    suspend fun getShareStorePdfTextServerCall(serviceInterface: IMarketingServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getShareStorePdfText()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody -> serviceInterface.onShareStorePdfDataResponse(responseBody) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    serviceInterface.onMarketingErrorResponse(Exception(response.message()))
                }
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getShareStoreDataServerCall: ", e)
            serviceInterface.onMarketingErrorResponse(e)
        }
    }

}