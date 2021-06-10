package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.PaymentModesNetworkService
import com.digitaldukaan.services.serviceinterface.IPaymentModesServiceInterface

class PaymentModesService {

    private val mNetworkService = PaymentModesNetworkService()
    private lateinit var mServiceInterface: IPaymentModesServiceInterface

    fun setServiceInterface(serviceInterface: IPaymentModesServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getPaymentModesPageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getPaymentModesPageInfo(mServiceInterface)
        }
    }

}