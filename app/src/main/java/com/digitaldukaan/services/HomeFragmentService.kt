package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.HomeFragmentNetworkService
import com.digitaldukaan.services.serviceinterface.IHomeFragmentServiceInterface

class HomeFragmentService {

    private lateinit var mServiceInterface : IHomeFragmentServiceInterface

    private val mNetworkService = HomeFragmentNetworkService()

    fun setHomeFragmentServiceListener(listener: IHomeFragmentServiceInterface) {
        mServiceInterface = listener
    }

    fun verifyUserAuthentication(authToken:String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.authenticateUserServerCall(authToken, mServiceInterface)
        }
    }

}