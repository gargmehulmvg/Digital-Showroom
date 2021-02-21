package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.SplashNetworkService
import com.digitaldukaan.services.serviceinterface.ISplashServiceInterface

class SplashService {

    private val mSplashNetworkService = SplashNetworkService()
    private lateinit var mSplashServiceInterface: ISplashServiceInterface

    fun setSplashServiceInterface(serviceInterface: ISplashServiceInterface) {
        mSplashServiceInterface = serviceInterface
    }

    fun getStaticData(languageId: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mSplashNetworkService.getAppStaticTextServerCall(languageId, mSplashServiceInterface)
        }
    }
}