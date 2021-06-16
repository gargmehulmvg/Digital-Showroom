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

    fun getTransactionsList(pageNo: Int, startDate: String?, endDate: String?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getTransactionsListServerCall(pageNo, startDate, endDate, mServiceInterface)
        }
    }

    fun getSettlementsList(pageNo: Int, startDate: String?, endDate: String?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getSettlementListServerCall(pageNo, startDate, endDate, mServiceInterface)
        }
    }

}