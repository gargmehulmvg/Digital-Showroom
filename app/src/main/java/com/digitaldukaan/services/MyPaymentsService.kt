package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.TransactionRequest
import com.digitaldukaan.services.networkservice.MyPaymentsNetworkService
import com.digitaldukaan.services.serviceinterface.IMyPaymentsServiceInterface

class MyPaymentsService {

    private val mNetworkService = MyPaymentsNetworkService()
    private lateinit var mServiceInterface: IMyPaymentsServiceInterface

    fun setServiceInterface(serviceInterface: IMyPaymentsServiceInterface) {
        mServiceInterface = serviceInterface
    }

    fun getTransactionsList(request: TransactionRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getTransactionsListServerCall(request, mServiceInterface)
        }
    }

    fun getMyPaymentPageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getMyPaymentPageInfoServerCall(mServiceInterface)
        }
    }

}