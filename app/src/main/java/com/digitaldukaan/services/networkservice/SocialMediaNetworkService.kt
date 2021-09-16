package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ISocialMediaServiceInterface
import com.google.gson.Gson

class SocialMediaNetworkService {

    suspend fun getSocialMediaPageInfoServerCall(
        loginServiceInterface: ISocialMediaServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getSocialMediaPageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonResponse -> loginServiceInterface.onSocialMediaPageInfoResponse(commonResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(
                        Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        loginServiceInterface.onSocialMediaPageInfoResponse(errorResponse)
                    }
                }
            }
        } catch (e : Exception) {
            Log.e(SocialMediaNetworkService::class.java.simpleName, "getSocialMediaPageInfoServerCall: ", e)
            loginServiceInterface.onSocialMediaException(e)
        }
    }

}