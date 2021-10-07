package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.StoreLogoRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IProfileServiceInterface
import com.google.gson.Gson
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfileNetworkService {

    suspend fun getProfileServerCall(serviceInterface: IProfileServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getProfileResponse()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { staticTextResponse ->
                        serviceInterface.onProfileResponse(staticTextResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    serviceInterface.onProfileDataException(Exception(response.message()))
                }
            }
        } catch (e: Exception) {
            Log.e(ProfileNetworkService::class.java.simpleName, "getProfileServerCall: ", e)
            serviceInterface.onProfileDataException(e)
        }
    }

    suspend fun getReferAndEarnDataServerCall(
        serviceInterface: IProfileServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getReferAndEarnData()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody ->
                        serviceInterface.onReferAndEarnResponse(responseBody)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onReferAndEarnResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProfileNetworkService::class.java.simpleName, "changeStoreAndDeliveryStatusServerCall: ", e)
            serviceInterface.onProfileDataException(e)
        }
    }

    suspend fun getReferAndEarnDataForWhatsAppServerCall(
        serviceInterface: IProfileServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getReferAndEarnDataOverWhatsApp()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody ->
                        serviceInterface.onReferAndEarnOverWhatsAppResponse(responseBody)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    serviceInterface.onProfileDataException(Exception(response.message()))
                }
            }
        } catch (e: Exception) {
            Log.e(ProfileNetworkService::class.java.simpleName, "changeStoreAndDeliveryStatusServerCall: ", e)
            serviceInterface.onProfileDataException(e)
        }
    }

    suspend fun getImageUploadCdnLinkServerCall(
        imageType: RequestBody,
        imageFile: MultipartBody.Part?,
        serviceInterface: IProfileServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getImageUploadCdnLink(imageType, imageFile)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeLinkResponse ->
                        serviceInterface.onImageCDNLinkGenerateResponse(storeLinkResponse)
                    }
                } else throw Exception(response.message())
            }
        } catch (e: Exception) {
            Log.e(ProfilePhotoNetworkService::class.java.simpleName, "getImageUploadCdnLinkServerCall: ", e)
            serviceInterface.onProfileDataException(e)
        }
    }

    suspend fun updateStoreLogoServerCall(
        request: StoreLogoRequest,
        serviceInterface: IProfileServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setStoreLogo(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeLinkResponse ->
                        serviceInterface.onStoreLogoResponse(storeLinkResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    throw Exception(response.message())
                }
            }
        } catch (e: Exception) {
            Log.e(ProfilePreviewNetworkService::class.java.simpleName, "updateStoreLogoServerCall: ", e)
            serviceInterface.onProfileDataException(e)
        }
    }

    suspend fun getProductShareStoreDataServerCall(
        serviceInterface: IProfileServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getProductShareStoreData()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onProductShareStoreWAResponse(commonApiResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onProductShareStoreWAResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProductNetworkService::class.java.simpleName, "getProductShareStoreDataServerCall: ", e)
            serviceInterface.onProfileDataException(e)
        }
    }

}