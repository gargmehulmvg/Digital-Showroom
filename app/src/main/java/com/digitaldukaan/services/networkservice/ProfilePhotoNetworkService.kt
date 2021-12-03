package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.StoreLogoRequest
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IProfilePhotoServiceInterface
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfilePhotoNetworkService {

    suspend fun getImageUploadCdnLinkServerCall(
        imageType: RequestBody,
        imageFile: MultipartBody.Part?,
        serviceInterface: IProfilePhotoServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getImageUploadCdnLink(imageType, imageFile)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeLinkResponse ->
                        serviceInterface.onImageCDNLinkGenerateResponse(storeLinkResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    throw Exception(response.message())
                }
            }
        } catch (e: Exception) {
            Log.e(ProfilePhotoNetworkService::class.java.simpleName, "updateStoreLogoServerCall: ", e)
            serviceInterface.onProfilePhotoServerException(e)
        }
    }

    suspend fun uploadStoreImageServerCall(
        request: StoreLogoRequest,
        serviceInterface: IProfilePhotoServiceInterface
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
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    throw Exception(response.message())
                }
            }
        } catch (e: Exception) {
            Log.e(ProfilePhotoNetworkService::class.java.simpleName, "updateStoreLogoServerCall: ", e)
            serviceInterface.onProfilePhotoServerException(e)
        }
    }

}