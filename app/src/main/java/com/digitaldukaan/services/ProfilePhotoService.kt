package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.StoreLogoRequest
import com.digitaldukaan.services.networkservice.ProfilePhotoNetworkService
import com.digitaldukaan.services.serviceinterface.IProfilePhotoServiceInterface

class ProfilePhotoService {

    private val mNetworkService = ProfilePhotoNetworkService()
    private lateinit var mServiceInterface: IProfilePhotoServiceInterface

    fun setServiceInterface(serviceInterface: IProfilePhotoServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun updateStoreLogo(authToken: String, request: StoreLogoRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateStoreLogoServerCall(authToken, request, mServiceInterface)
        }
    }

}