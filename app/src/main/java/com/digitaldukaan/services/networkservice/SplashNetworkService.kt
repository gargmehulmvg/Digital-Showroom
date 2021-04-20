package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.models.response.HelpScreenItemResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ISplashServiceInterface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

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
                        val listType = object : TypeToken<List<HelpScreenItemResponse>>() {}.type
                        StaticInstances.sHelpScreenList = Gson().fromJson<ArrayList<HelpScreenItemResponse>>(helpScreenResponse.mCommonDataStr, listType)
                    }
                } else splashServiceInterface.onStaticDataException(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(SplashNetworkService::class.java.simpleName, "getHelpScreensServerCall :: ", e)
            splashServiceInterface.onStaticDataException(e)
        }
    }

}