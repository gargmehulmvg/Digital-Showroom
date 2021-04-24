package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.BankDetailsRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IBankDetailsServiceInterface
import com.google.gson.Gson

class BankDetailsNetworkService {

    suspend fun setBankDetailsServerCall(
        authToken:String,
        request : BankDetailsRequest,
        serviceInterface: IBankDetailsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setBankDetails(authToken, request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody -> serviceInterface.onBankDetailsResponse(responseBody) }
                } else {
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val validateOtpErrorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onBankDetailsResponse(validateOtpErrorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(BankDetailsNetworkService::class.java.simpleName, "saveStoreDescriptionServerCall: ", e)
            serviceInterface.onBankDetailsServerException(e)
        }
    }

    suspend fun getBankDetailsPageInfoServerCall(
        serviceInterface: IBankDetailsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getBankDetailsPageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody -> serviceInterface.onBankDetailsResponse(responseBody) }
                } else {
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val validateOtpErrorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onBankDetailsResponse(validateOtpErrorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(BankDetailsNetworkService::class.java.simpleName, "saveStoreDescriptionServerCall: ", e)
            serviceInterface.onBankDetailsServerException(e)
        }
    }

}