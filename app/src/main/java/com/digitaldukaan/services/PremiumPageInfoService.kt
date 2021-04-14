package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.PremiumPageNetworkService
import com.digitaldukaan.services.serviceinterface.IPremiumPageInfoServiceInterface

class PremiumPageInfoService {

    private val mNetworkService = PremiumPageNetworkService()
    private lateinit var mServiceInterface: IPremiumPageInfoServiceInterface

    fun setServiceInterface(serviceInterface: IPremiumPageInfoServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getPremiumPageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getPremiumPageInfoServerCall(mServiceInterface)
        }
    }

}