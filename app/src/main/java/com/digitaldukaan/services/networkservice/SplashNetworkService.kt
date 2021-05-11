package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.BuildConfig
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
                } else splashServiceInterface.onStaticDataException(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(SplashNetworkService::class.java.simpleName, "getAppStaticTextServerCall: ", e)
            splashServiceInterface.onStaticDataException(e)
        }
    }

    suspend fun getHelpScreensServerCall(
        splashServiceInterface: ISplashServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getHelpScreens()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { helpScreenResponse ->
                        splashServiceInterface.onHelpScreenResponse(helpScreenResponse)
                    }
                } else splashServiceInterface.onStaticDataException(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(SplashNetworkService::class.java.simpleName, "getHelpScreensServerCall :: ", e)
            splashServiceInterface.onStaticDataException(e)
        }
    }

    suspend fun getAppVersionServerCall(
        splashServiceInterface: ISplashServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getAppVersion("ds_android", BuildConfig.VERSION_NAME)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse ->
                        splashServiceInterface.onAppVersionResponse(commonApiResponse)
                    }
                } else splashServiceInterface.onStaticDataException(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(SplashNetworkService::class.java.simpleName, "getAppVersionServerCall :: ", e)
            splashServiceInterface.onStaticDataException(e)
        }
    }

}