package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.AuthenticateUserRequest
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface
import com.google.gson.Gson

class HomeNetworkService {

    suspend fun authenticateUserServerCall(
        authToken: String,
        serviceInterface: IHomeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()
                ?.authenticateUser(AuthenticateUserRequest(authToken))
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { validateUserResponse ->
                        serviceInterface.onUserAuthenticationResponse(
                            validateUserResponse
                        )
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
            Log.e(HomeNetworkService::class.java.simpleName, "authenticateUserServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getOrdersServerCall(
        storeId: String,
        page: Int,
        serviceInterface: IHomeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getPendingOrders(storeId, page)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { validateUserResponse ->
                        serviceInterface.onGetOrdersResponse(validateUserResponse)
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

    suspend fun getCompletedOrdersServerCall(
        storeId: String,
        page: Int,
        serviceInterface: IHomeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getCompletedOrders(storeId, page)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { validateUserResponse ->
                        serviceInterface.onCompletedOrdersResponse(validateUserResponse)
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
                        val validateOtpErrorResponse = Gson().fromJson(validateOtpError.string(), ValidateOtpErrorResponse::class.java)
                        serviceInterface.onOTPVerificationErrorResponse(validateOtpErrorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(HomeNetworkService::class.java.simpleName, "getAnalyticsDataServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

}