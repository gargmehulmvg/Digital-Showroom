package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.StoreDeliveryStatusChangeRequest
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
                } else serviceInterface.onProfileDataException(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(ProfileNetworkService::class.java.simpleName, "getProfileServerCall: ", e)
            serviceInterface.onProfileDataException(e)
        }
    }

    suspend fun changeStoreAndDeliveryStatusServerCall(
        request: StoreDeliveryStatusChangeRequest,
        serviceInterface: IProfileServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.changeStoreAndDeliveryStatus(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { responseBody ->
                        serviceInterface.onChangeStoreAndDeliveryStatusResponse(responseBody)
                    }
                } else {
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onChangeStoreAndDeliveryStatusResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProfileNetworkService::class.java.simpleName, "changeStoreAndDeliveryStatusServerCall: ", e)
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
                } else serviceInterface.onProfileDataException(Exception(response.message()))
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
        authToken: String,
        request: StoreLogoRequest,
        serviceInterface: IProfileServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setStoreLogo(authToken, request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeLinkResponse ->
                        serviceInterface.onStoreLogoResponse(storeLinkResponse)
                    }
                } else throw Exception(response.message())
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