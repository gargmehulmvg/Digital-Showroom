package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.SearchOrdersRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ISearchOrderServiceInterface
import com.google.gson.Gson

class SearchOrderNetworkService {

    suspend fun getSearchOrdersServerCall(
        authToken: String,
        request: SearchOrdersRequest,
        serviceInterface: ISearchOrderServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getSearchOrdersList(authToken, request)
            response?.let {
                if (it.isSuccessful) it.body()?.let { validateUserResponse -> serviceInterface.onSearchOrderResponse(validateUserResponse) }
                else {
                    val validateOtpError = it.errorBody()
                    validateOtpError?.let {
                        val errorResponse = Gson().fromJson(validateOtpError.string(), CommonApiResponse::class.java)
                        serviceInterface.onSearchOrderResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(SearchOrderNetworkService::class.java.simpleName, "getSearchOrdersServerCall: ", e)
            serviceInterface.onSearchOrderException(e)
        }
    }

}