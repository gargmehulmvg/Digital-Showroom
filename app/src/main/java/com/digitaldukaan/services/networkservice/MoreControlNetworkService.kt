package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.MoreControlsRequest
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IMoreControlsServiceInterface

class MoreControlNetworkService {

    suspend fun updateDeliveryInfoServerCall(
        authToken:String,
        request : MoreControlsRequest,
        serviceInterface: IMoreControlsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateDeliveryInfo(authToken, request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onMoreControlsResponse(storeNameResponse)
                    }
                } else throw Exception(response.message())
            }
        } catch (e: Exception) {
            Log.e(MoreControlNetworkService::class.java.simpleName, "updateDeliveryInfoServerCall: ", e)
            serviceInterface.onMoreControlsServerException(e)
        }
    }

}