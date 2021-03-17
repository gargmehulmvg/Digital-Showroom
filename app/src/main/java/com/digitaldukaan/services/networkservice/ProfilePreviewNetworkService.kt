package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.StoreLinkRequest
import com.digitaldukaan.models.request.StoreLogoRequest
import com.digitaldukaan.models.request.StoreNameRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IProfilePreviewServiceInterface
import com.google.gson.Gson

class ProfilePreviewNetworkService {

    suspend fun getProfilePreviewServerCall(
        authToken : String,
        serviceInterface: IProfilePreviewServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getProfilePreviewResponse(authToken)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { profilePreviewResponse ->
                        serviceInterface.onProfilePreviewResponse(profilePreviewResponse)
                    }
                } else serviceInterface.onProfilePreviewServerException(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(ProfilePreviewNetworkService::class.java.simpleName, "getProfilePreviewServerCall: ", e)
            serviceInterface.onProfilePreviewServerException(e)
        }
    }

    suspend fun updateStoreNameServerCall(
        authToken:String,
        storeNameRequest : StoreNameRequest,
        serviceInterface: IProfilePreviewServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setStoreName(authToken, storeNameRequest)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onStoreNameResponse(storeNameResponse)
                    }
                } else throw Exception(response.message())
            }
        } catch (e: Exception) {
            Log.e(ProfilePreviewNetworkService::class.java.simpleName, "updateStoreNameServerCall: ", e)
            serviceInterface.onProfilePreviewServerException(e)
        }
    }

    suspend fun updateStoreLinkServerCall(
        authToken:String,
        storeLinkRequest: StoreLinkRequest,
        serviceInterface: IProfilePreviewServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateStoreDomain(authToken, storeLinkRequest)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeLinkResponse ->
                        serviceInterface.onStoreLinkResponse(storeLinkResponse)
                    }
                } else throw Exception(response.message())
            }
        } catch (e: Exception) {
            Log.e(ProfilePreviewNetworkService::class.java.simpleName, "updateStoreLinkServerCall: ", e)
            serviceInterface.onProfilePreviewServerException(e)
        }
    }

    suspend fun updateStoreLogoServerCall(
        authToken: String,
        request: StoreLogoRequest,
        serviceInterface: IProfilePreviewServiceInterface
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
            serviceInterface.onProfilePreviewServerException(e)
        }
    }

    suspend fun initiateKycServerCall(
        authToken: String,
        serviceInterface: IProfilePreviewServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.initiateKyc(authToken)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeLinkResponse ->
                        serviceInterface.onInitiateKycResponse(storeLinkResponse)
                    }
                } else {
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val validateOtpErrorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onInitiateKycResponse(validateOtpErrorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(ProfilePreviewNetworkService::class.java.simpleName, "updateStoreLogoServerCall: ", e)
            serviceInterface.onProfilePreviewServerException(e)
        }
    }

}