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

    suspend fun getProductShareStorePdfTextServerCall(
        authToken: String,
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getProductShareStorePdfText(authToken)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onProductShareStorePDFDataResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onProductShareStorePDFDataResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "getProductShareStorePdfTextServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

    suspend fun generateProductStorePdfServerCall(
        authToken: String,
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.generateProductStorePdf(authToken)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onProductPDFGenerateResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onProductPDFGenerateResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "generateProductStorePdfServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

    suspend fun getProductShareStoreDataServerCall(
        authToken: String,
        serviceInterface: IProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getProductShareStoreData(authToken)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onProductShareStoreWAResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onProductShareStoreWAResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "getProductShareStoreDataServerCall: ", e)
            serviceInterface.onProductException(e)
        }
    }

}