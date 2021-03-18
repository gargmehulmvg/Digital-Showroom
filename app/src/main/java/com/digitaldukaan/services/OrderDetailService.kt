package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.OrderDetailNetworkService
import com.digitaldukaan.services.serviceinterface.IOrderDetailServiceInterface

class OrderDetailService {

    private lateinit var mServiceInterface: IOrderDetailServiceInterface

    private val mNetworkService = OrderDetailNetworkService()

    fun setOrderDetailServiceListener(listener: IOrderDetailServiceInterface) {
        mServiceInterface = listener
    }

    fun getOrderDetail(authToken: String, orderId: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getOrderDetailServerCall(authToken , orderId, mServiceInterface)
        }
    }

}