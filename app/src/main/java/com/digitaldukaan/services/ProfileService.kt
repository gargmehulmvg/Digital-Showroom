package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.ProfileNetworkService
import com.digitaldukaan.services.serviceinterface.IProfileServiceInterface

class ProfileService {

    private val mNetworkService = ProfileNetworkService()
    private lateinit var mProfileServiceInterface: IProfileServiceInterface

    fun setProfileServiceInterface(serviceInterface: IProfileServiceInterface) {
        mProfileServiceInterface = serviceInterface
    }

    fun getUserProfile(storeId: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getProfileServerCall(storeId, mProfileServiceInterface)
        }
    }
}