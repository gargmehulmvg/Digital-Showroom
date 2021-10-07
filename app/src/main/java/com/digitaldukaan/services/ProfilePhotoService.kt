package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.StoreLogoRequest
import com.digitaldukaan.services.networkservice.ProfilePhotoNetworkService
import com.digitaldukaan.services.serviceinterface.IProfilePhotoServiceInterface
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ProfilePhotoService {

    private val mNetworkService = ProfilePhotoNetworkService()
    private lateinit var mServiceInterface: IProfilePhotoServiceInterface

    fun setServiceInterface(serviceInterface: IProfilePhotoServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun generateCDNLink(imageType: RequestBody, file: MultipartBody.Part?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getImageUploadCdnLinkServerCall(imageType, file, mServiceInterface)
        }
    }

    fun uploadStoreLogo(request: StoreLogoRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.uploadStoreImageServerCall(request, mServiceInterface)
        }
    }

}