package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.StoreNameRequest
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IProfilePreviewServiceInterface

class ProfilePreviewNetworkService {

    suspend fun getProfilePreviewServerCall(
        storeId : String,
        serviceInterface: IProfilePreviewServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getProfilePreviewResponse(storeId)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { profilePreviewResponse ->
                        serviceInterface.onProfilePreviewResponse(profilePreviewResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProfilePreviewNetworkService::class.java.simpleName, "getProfilePreviewServerCall: ", e)
            serviceInterface.onProfilePreviewServerException(e)
        }
    }

    suspend fun updateStoreNameServerCall(
        authToken:String,
        storeNameRequest : StoreNameRequest,
        serviceInterface: IProfilePreviewServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setStoreName(authToken, storeNameRequest)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { profilePreviewResponse ->
                        serviceInterface.onStoreNameResponse(profilePreviewResponse)
                    }
                } else throw Exception(response.message())
            }
        } catch (e: Exception) {
            Log.e(ProfilePreviewNetworkService::class.java.simpleName, "updateStoreNameServerCall: ", e)
            serviceInterface.onProfilePreviewServerException(e)
        }
    }

}