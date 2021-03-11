package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.StoreDescriptionRequest
import com.digitaldukaan.models.response.StoreDescriptionResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IStoreDescriptionServiceInterface
import com.google.gson.Gson

class StoreDescriptionNetworkService {

    suspend fun saveStoreDescriptionServerCall(
        authToken: String,
        request: StoreDescriptionRequest,
        serviceInterface: IStoreDescriptionServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setStoreDescription(authToken, request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { profilePreviewResponse -> serviceInterface.onStoreDescriptionResponse(profilePreviewResponse) }
                } else {
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), StoreDescriptionResponse::class.java)
                        serviceInterface.onStoreDescriptionResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(StoreDescriptionNetworkService::class.java.simpleName, "saveStoreDescriptionServerCall: ", e)
            serviceInterface.onStoreDescriptionServerException(e)
        }
    }

}