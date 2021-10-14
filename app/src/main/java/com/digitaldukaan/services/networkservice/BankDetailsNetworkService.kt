package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.BankDetailsRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IBankDetailsServiceInterface
import com.google.gson.Gson

class BankDetailsNetworkService {

    suspend fun setBankDetailsServerCall(request: BankDetailsRequest, serviceInterface: IBankDetailsServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setBankDetails(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody -> serviceInterface.onBankDetailsResponse(responseBody) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(
                        Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
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
                    it.body()?.let { responseBody -> serviceInterface.onBankDetailsPageInfoResponse(responseBody) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val validateOtpErrorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onBankDetailsPageInfoResponse(validateOtpErrorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(BankDetailsNetworkService::class.java.simpleName, "getBankDetailsPageInfoServerCall: ", e)
            serviceInterface.onBankDetailsServerException(e)
        }
    }

}