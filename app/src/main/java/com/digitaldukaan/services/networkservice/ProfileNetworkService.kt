package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.StoreDeliveryStatusChangeRequest
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IProfileServiceInterface

class ProfileNetworkService {

    suspend fun getProfileServerCall(
        storeId : String,
        serviceInterface: IProfileServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getProfileResponse(storeId)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { staticTextResponse ->
                        serviceInterface.onProfileResponse(staticTextResponse)
                    }
                } else serviceInterface.onProfileDataException(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(ProfileNetworkService::class.java.simpleName, "getProfileServerCall: ", e)
            serviceInterface.onProfileDataException(e)
        }
    }

    suspend fun changeStoreAndDeliveryStatusServerCall(
        request: StoreDeliveryStatusChangeRequest,
        serviceInterface: IProfileServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.changeStoreAndDeliveryStatus(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody ->
                        serviceInterface.onChangeStoreAndDeliveryStatusResponse(responseBody)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProfileNetworkService::class.java.simpleName, "changeStoreAndDeliveryStatusServerCall: ", e)
            serviceInterface.onProfileDataException(e)
        }
    }

}