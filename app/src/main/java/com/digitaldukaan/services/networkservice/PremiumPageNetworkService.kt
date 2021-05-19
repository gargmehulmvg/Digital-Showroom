package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IPremiumPageInfoServiceInterface
import com.google.gson.Gson

class PremiumPageNetworkService {

    suspend fun getPremiumPageInfoServerCall(
        serviceInterface: IPremiumPageInfoServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getPremiumPageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { apiResponse ->
                        serviceInterface.onPremiumPageInfoResponse(apiResponse)
                    }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) {
                        throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    }
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onPremiumPageInfoResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(PremiumPageNetworkService::class.java.simpleName, "getPremiumPageInfoServerCall: ", e)
            serviceInterface.onPremiumPageInfoServerException(e)
        }
    }

}