package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.BillingPosNetworkService
import com.digitaldukaan.services.serviceinterface.IBillingPosServiceInterface

class BillingPosService {

    private val mNetworkService = BillingPosNetworkService()
    private lateinit var mServiceInterface: IBillingPosServiceInterface

    fun setServiceInterface(serviceInterface: IBillingPosServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getAddressFieldsPageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getPosBillingPageInfoServerCall(mServiceInterface)
        }
    }

    fun requestACallBack(isYesButtonClicked: Boolean) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.requestACallBackServerCall(mServiceInterface, isYesButtonClicked)
        }
    }

}