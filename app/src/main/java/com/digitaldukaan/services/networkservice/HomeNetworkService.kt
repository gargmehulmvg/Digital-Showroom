package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.models.request.OrdersRequest
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
                        if (request.orderMode == Constants.MODE_PENDING) serviceInterface.onGetOrdersResponse(commonApiResponse)
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

}