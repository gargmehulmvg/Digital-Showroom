package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.AddProductRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IAddProductServiceInterface
import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.RequestBody

class AddProductNetworkService {

    suspend fun getAddOrderBottomSheetDataServerCall(
        authToken: String,
        serviceInterface: IAddProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getMasterCatalogStaticText(authToken)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onAddProductBannerStaticDataResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onAddProductBannerStaticDataResponse(
                            errorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(AddProductNetworkService::class.java.simpleName, "getAddOrderBottomSheetDataServerCall: ", e)
            serviceInterface.onAddProductException(e)
        }
    }

    suspend fun getItemInfoServerCall(
        authToken: String,
        itemId: Int,
        serviceInterface: IAddProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getItemInfo(authToken, itemId)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onGetAddProductDataResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onGetAddProductDataResponse(
                            errorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(AddProductNetworkService::class.java.simpleName, "getItemInfoServerCall: ", e)
            serviceInterface.onAddProductException(e)
        }
    }

    suspend fun setItemServerCall(
        authToken: String,
        request: AddProductRequest,
        serviceInterface: IAddProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setItem(authToken, request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onAddProductDataResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onAddProductDataResponse(
                            errorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(AddProductNetworkService::class.java.simpleName, "getItemInfoServerCall: ", e)
            serviceInterface.onAddProductException(e)
        }
    }

    suspend fun convertFileToLinkServerCall(
        authToken: String,
        imageType: RequestBody,
        imageFile: MultipartBody.Part?,
        serviceInterface: IAddProductServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getImageUploadCdnLink(authToken, imageType, imageFile)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onConvertFileToLinkResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onConvertFileToLinkResponse(
                            errorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(AddProductNetworkService::class.java.simpleName, "convertFileToLinkServerCall: ", e)
            serviceInterface.onAddProductException(e)
        }
    }

}