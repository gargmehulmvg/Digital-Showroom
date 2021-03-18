package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IOrderDetailServiceInterface
import com.google.gson.Gson

class OrderDetailNetworkService {

    suspend fun getOrderDetailServerCall(
        authToken: String,
        orderId: String,
        serviceInterface: IOrderDetailServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getOrderDetails(authToken, orderId)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onOrderDetailResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onOrderDetailResponse(
                            errorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderDetailNetworkService::class.java.simpleName, "getOrderDetailServerCall: ", e)
            serviceInterface.onOrderDetailException(e)
        }
    }

}