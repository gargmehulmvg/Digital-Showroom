package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ISplashServiceInterface

class SplashNetworkService {

    suspend fun getAppStaticTextServerCall(
        languageId : String,
        splashServiceInterface: ISplashServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getAppStaticText(languageId)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { staticTextResponse ->
                        splashServiceInterface.onStaticDataResponse(staticTextResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(SplashNetworkService::class.java.simpleName, "getAppStaticTextServerCall: ", e)
            splashServiceInterface.onStaticDataException(e)
        }
    }

}