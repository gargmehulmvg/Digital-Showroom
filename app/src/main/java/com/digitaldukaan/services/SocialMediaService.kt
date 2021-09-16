package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.SocialMediaNetworkService
import com.digitaldukaan.services.serviceinterface.ISocialMediaServiceInterface

class SocialMediaService {

    private val mNetworkService = SocialMediaNetworkService()

    private lateinit var mServiceInterface: ISocialMediaServiceInterface

    fun setSocialMediaServiceInterface(serviceInterface: ISocialMediaServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getSocialMediaPageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getSocialMediaPageInfoServerCall(mServiceInterface)
        }
    }

}