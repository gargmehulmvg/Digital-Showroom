package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.SocialMediaTemplateFavouriteRequest
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
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
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

    suspend fun getSocialMediaTemplateListServerCall(
        id: String,
        page: Int,
        serviceInterface: ISocialMediaServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getSocialMediaTemplateList(id = id, page = page)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonResponse -> serviceInterface.onSocialMediaTemplateListResponse(commonResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onSocialMediaTemplateListResponse(errorResponse)
                    }
                }
            }
        } catch (e : Exception) {
            Log.e(SocialMediaNetworkService::class.java.simpleName, "getSocialMediaTemplateListServerCall: ", e)
            serviceInterface.onSocialMediaException(e)
        }
    }

    suspend fun setSocialMediaFavouriteServerCall(
        request: SocialMediaTemplateFavouriteRequest,
        serviceInterface: ISocialMediaServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setSocialMediaFavourite(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonResponse -> serviceInterface.onSocialMediaTemplateFavouriteResponse(commonResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onSocialMediaTemplateFavouriteResponse(errorResponse)
                    }
                }
            }
        } catch (e : Exception) {
            Log.e(SocialMediaNetworkService::class.java.simpleName, "setSocialMediaFavouriteServerCall: ", e)
            serviceInterface.onSocialMediaException(e)
        }
    }

    suspend fun getMarketingPageInfoServerCall(serviceInterface: ISocialMediaServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getStoreMarketingPageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { generateOtpResponse -> serviceInterface.onMarketingPageInfoResponse(generateOtpResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    serviceInterface.onSocialMediaException(Exception(response.message()))
                }
            }
        } catch (e : Exception) {
            Log.e(MarketingNetworkService::class.java.simpleName, "getMarketingPageInfoServerCall: ", e)
            serviceInterface.onSocialMediaException(e)
        }
    }

}