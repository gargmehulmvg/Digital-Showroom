package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.BankDetailsRequest
import com.digitaldukaan.services.networkservice.BankDetailsNetworkService
import com.digitaldukaan.services.serviceinterface.IBankDetailsServiceInterface

class BankDetailsService {

    private val mNetworkService = BankDetailsNetworkService()
    private lateinit var mServiceInterface: IBankDetailsServiceInterface

    fun setServiceInterface(serviceInterface: IBankDetailsServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun setBankDetails(request: BankDetailsRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.setBankDetailsServerCall(request, mServiceInterface)
        }
    }

    fun getBankDetailsPageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getBankDetailsPageInfoServerCall(mServiceInterface)
        }
    }

}