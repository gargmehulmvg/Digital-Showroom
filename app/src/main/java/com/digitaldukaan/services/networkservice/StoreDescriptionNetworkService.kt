package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.StoreDescriptionRequest
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IStoreDescriptionServiceInterface

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
                } else serviceInterface.onStoreDescriptionServerException(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(StoreDescriptionNetworkService::class.java.simpleName, "saveStoreDescriptionServerCall: ", e)
            serviceInterface.onStoreDescriptionServerException(e)
        }
    }

}