package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.StoreAddressRequest
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IStoreAddressServiceInterface

class StoreAddressNetworkService {

    suspend fun updateStoreAddressServerCall(
        storeAddressRequest: StoreAddressRequest,
        serviceInterface: IStoreAddressServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateStoreAddress(storeAddressRequest)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onStoreAddressResponse(storeNameResponse)
                    }
                } else throw Exception(response.message())
            }
        } catch (e: Exception) {
            Log.e(StoreAddressNetworkService::class.java.simpleName, "updateStoreAddressServerCall: ", e)
            serviceInterface.onStoreAddressServerException(e)
        }
    }

}