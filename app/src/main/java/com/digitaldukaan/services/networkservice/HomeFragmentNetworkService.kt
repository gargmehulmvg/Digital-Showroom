package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.AuthenticateUserRequest
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IHomeFragmentServiceInterface
import com.google.gson.Gson

class HomeFragmentNetworkService {

    suspend fun authenticateUserServerCall(
        authToken: String,
        serviceInterface: IHomeFragmentServiceInterface
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
            Log.e(HomeFragmentNetworkService::class.java.simpleName, "authenticateUserServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getOrdersServerCall(
        storeId: String,
        page: Int,
        serviceInterface: IHomeFragmentServiceInterface
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
            Log.e(HomeFragmentNetworkService::class.java.simpleName, "getOrdersServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getCompletedOrdersServerCall(
        storeId: String,
        page: Int,
        serviceInterface: IHomeFragmentServiceInterface
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
            Log.e(HomeFragmentNetworkService::class.java.simpleName, "getOrdersServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

}