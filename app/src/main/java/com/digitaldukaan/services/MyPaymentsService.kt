package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.MyPaymentsNetworkService
import com.digitaldukaan.services.serviceinterface.IMyPaymentsServiceInterface

class MyPaymentsService {

    private val mNetworkService = MyPaymentsNetworkService()
    private lateinit var mServiceInterface: IMyPaymentsServiceInterface

    fun setServiceInterface(serviceInterface: IMyPaymentsServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getMyPaymentsList(pageNo: Int, startDate: String?, endDate: String?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getMyPaymentsListServerCall(pageNo, startDate, endDate, mServiceInterface)
        }
    }

}