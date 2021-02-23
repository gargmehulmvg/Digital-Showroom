package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.ProfilePreviewNetworkService
import com.digitaldukaan.services.serviceinterface.IProfilePreviewServiceInterface

class ProfilePreviewService {

    private val mNetworkService = ProfilePreviewNetworkService()
    private lateinit var mServiceInterface: IProfilePreviewServiceInterface

    fun setServiceInterface(serviceInterface: IProfilePreviewServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getProfilePreviewData(storeId: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getProfilePreviewServerCall(storeId, mServiceInterface)
        }
    }

}