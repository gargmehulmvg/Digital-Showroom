package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.SearchOrdersRequest
import com.digitaldukaan.models.request.UpdateOrderStatusRequest
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

    suspend fun updateOrderStatusServerCall(
        authToken: String,
        statusRequest: UpdateOrderStatusRequest,
        serviceInterface: ISearchOrderServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateOrderStatus(authToken, statusRequest)
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.onOrdersUpdatedStatusResponse(commonApiResponse) }
                else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onOrdersUpdatedStatusResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(HomeNetworkService::class.java.simpleName, "updateOrderStatusServerCall: ", e)
            serviceInterface.onSearchOrderException(e)
        }
    }

}