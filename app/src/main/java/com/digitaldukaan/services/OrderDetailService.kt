package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.CompleteOrderRequest
import com.digitaldukaan.models.request.UpdateOrderRequest
import com.digitaldukaan.models.request.UpdateOrderStatusRequest
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

    fun getDeliveryTime() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getDeliveryTimeServerCall(mServiceInterface)
        }
    }

    fun updateOrder(authToken: String, request: UpdateOrderRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateOrderServerCall(authToken, request, mServiceInterface)
        }
    }

    fun updateOrderStatus(request: UpdateOrderStatusRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateOrderStatusServerCall(request, mServiceInterface)
        }
    }

    fun shareBillResponse(orderId: Int?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            orderId?.let { mNetworkService.shareBillResponseServerCall("$it", mServiceInterface) }
        }
    }

    fun getOrderDetailStatus(orderId: Int?) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            orderId?.let { mNetworkService.onOrderDetailStatusResponse("$it", mServiceInterface) }
        }
    }

    fun completeOrder(request: CompleteOrderRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.completeOrderServerCall(request, mServiceInterface)
        }
    }

}