package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.models.request.CompleteOrderRequest
import com.digitaldukaan.models.request.OrdersRequest
import com.digitaldukaan.models.request.SearchOrdersRequest
import com.digitaldukaan.models.request.UpdateOrderStatusRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface
import com.google.gson.Gson

class HomeNetworkService {

    suspend fun getOrdersServerCall(
        authToken: String,
        request: OrdersRequest,
        serviceInterface: IHomeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getOrdersList(authToken, request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse ->
                        if (request.orderMode == Constants.MODE_PENDING) serviceInterface.onPendingOrdersResponse(commonApiResponse)
                        else serviceInterface.onCompletedOrdersResponse(commonApiResponse)
                    }
                } else {
                    val validateOtpError = it.errorBody()
                    validateOtpError?.let {
                        val validateOtpErrorResponse = Gson().fromJson(
                            validateOtpError.string(),
                            ValidateOtpErrorResponse::class.java
                        )
                        serviceInterface.onOTPVerificationErrorResponse(
                            validateOtpErrorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(HomeNetworkService::class.java.simpleName, "getOrdersServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getAnalyticsDataServerCall(
        authToken: String,
        serviceInterface: IHomeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getAnalyticsData(authToken)
            response?.let {
                if (it.isSuccessful) it.body()?.let { validateUserResponse -> serviceInterface.onAnalyticsDataResponse(validateUserResponse) }
                else {
                    val validateOtpError = it.errorBody()
                    validateOtpError?.let {
                        val errorResponse = Gson().fromJson(validateOtpError.string(), CommonApiResponse::class.java)
                        serviceInterface.onAnalyticsDataResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(HomeNetworkService::class.java.simpleName, "getAnalyticsDataServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getOrderPageInfoServerCall(
        authToken: String,
        serviceInterface: IHomeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getOrderPageInfo(authToken)
            response?.let {
                if (it.isSuccessful) it.body()?.let { validateUserResponse -> serviceInterface.onOrderPageInfoResponse(validateUserResponse) }
                else {
                    val validateOtpError = it.errorBody()
                    validateOtpError?.let {
                        val errorResponse = Gson().fromJson(validateOtpError.string(), CommonApiResponse::class.java)
                        serviceInterface.onOrderPageInfoResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(HomeNetworkService::class.java.simpleName, "getOrderPageInfoServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getSearchOrdersServerCall(
        authToken: String,
        request: SearchOrdersRequest,
        serviceInterface: IHomeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getSearchOrdersList(authToken, request)
            response?.let {
                if (it.isSuccessful) it.body()?.let { validateUserResponse -> serviceInterface.onSearchOrdersResponse(validateUserResponse) }
                else {
                    val validateOtpError = it.errorBody()
                    validateOtpError?.let {
                        val errorResponse = Gson().fromJson(validateOtpError.string(), CommonApiResponse::class.java)
                        serviceInterface.onSearchOrdersResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(HomeNetworkService::class.java.simpleName, "getSearchOrdersServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun updateOrderStatusServerCall(
        authToken: String,
        statusRequest: UpdateOrderStatusRequest,
        serviceInterface: IHomeServiceInterface
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
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun completeOrderServerCall(
        request: CompleteOrderRequest,
        serviceInterface: IHomeServiceInterface
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
            serviceInterface.onHomePageException(e)
        }
    }

}