package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.UpdateOrderRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ISendBillPhotoServiceInterface
import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.RequestBody

class SendBillPhotoNetworkService {

    suspend fun convertFileToLinkServerCall(
        imageType: RequestBody,
        imageFile: MultipartBody.Part?,
        serviceInterface: ISendBillPhotoServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getImageUploadCdnLink(imageType, imageFile)
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
            Log.e(SendBillPhotoNetworkService::class.java.simpleName, "convertFileToLinkServerCall: ", e)
            serviceInterface.onSendBillPhotoException(e)
        }
    }

    suspend fun updateOrderServerCall(
        authToken: String,
        request: UpdateOrderRequest,
        serviceInterface: ISendBillPhotoServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateOrder(authToken, request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onUpdateOrderResponse(commonApiResponse)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onUpdateOrderResponse(
                            errorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(SendBillPhotoNetworkService::class.java.simpleName, "updateOrderServerCall: ", e)
            serviceInterface.onSendBillPhotoException(e)
        }
    }

}