package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.CompleteOrderRequest
import com.digitaldukaan.models.request.SearchOrdersRequest
import com.digitaldukaan.models.request.UpdateOrderStatusRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ISearchOrderServiceInterface
import com.google.gson.Gson

class SearchOrderNetworkService {

    suspend fun getSearchOrdersServerCall(
        request: SearchOrdersRequest,
        serviceInterface: ISearchOrderServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getSearchOrdersList(request)
            response?.let {
                if (it.isSuccessful) it.body()?.let { validateUserResponse -> serviceInterface.onSearchOrderResponse(validateUserResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
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
        statusRequest: UpdateOrderStatusRequest,
        serviceInterface: ISearchOrderServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateOrderStatus(statusRequest)
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.onOrdersUpdatedStatusResponse(commonApiResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
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

    suspend fun completeOrderServerCall(
        request: CompleteOrderRequest,
        serviceInterface: ISearchOrderServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.completeOrder(request)
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.onCompleteOrderStatusResponse(commonApiResponse) }
                else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onCompleteOrderStatusResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(HomeNetworkService::class.java.simpleName, "completeOrderServerCall: ", e)
            serviceInterface.onSearchOrderException(e)
        }
    }

}