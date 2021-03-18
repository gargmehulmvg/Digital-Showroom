package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IProductServiceInterface
import com.google.gson.Gson

class ProductNetworkService {

    suspend fun getProductPageInfoServerCall(
        authToken: String,
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getProductPageInfo(authToken)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onProductResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onProductResponse(
                            errorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "getProductPageInfoServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

}