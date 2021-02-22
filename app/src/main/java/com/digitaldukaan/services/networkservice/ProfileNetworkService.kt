package com.digitaldukaan.services.networkservice

import android.util.Log
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
                }
            }
        } catch (e: Exception) {
            Log.e(ProfileNetworkService::class.java.simpleName, "generateOTPServerCall: ", e)
            serviceInterface.onProfileDataException(e)
        }
    }

}