package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.UpdateOrderRequest
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

    fun getDeliveryTime(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getDeliveryTimeServerCall(authToken, mServiceInterface)
        }
    }

    fun updateOrder(authToken: String, request: UpdateOrderRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateOrderServerCall(authToken, request, mServiceInterface)
        }
    }

}