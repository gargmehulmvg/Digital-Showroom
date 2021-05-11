package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.CompleteOrderRequest
import com.digitaldukaan.models.request.OrdersRequest
import com.digitaldukaan.models.request.SearchOrdersRequest
import com.digitaldukaan.models.request.UpdateOrderStatusRequest
import com.digitaldukaan.services.networkservice.HomeNetworkService
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface

class HomeFragmentService {

    private lateinit var mServiceInterface: IHomeServiceInterface

    private val mNetworkService = HomeNetworkService()

    fun setHomeFragmentServiceListener(listener: IHomeServiceInterface) {
        mServiceInterface = listener
    }

    fun getOrders(request: OrdersRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getOrdersServerCall(request, mServiceInterface)
        }
    }

    fun getSearchOrders(request: SearchOrdersRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getSearchOrdersServerCall(request, mServiceInterface)
        }
    }

    fun updateOrderStatus(statusRequest: UpdateOrderStatusRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateOrderStatusServerCall(statusRequest, mServiceInterface)
        }
    }

    fun completeOrder(request: CompleteOrderRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.completeOrderServerCall(request, mServiceInterface)
        }
    }

    fun getAnalyticsData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getAnalyticsDataServerCall(mServiceInterface)
        }
    }

    fun getOrderPageInfo() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getOrderPageInfoServerCall(mServiceInterface)
        }
    }

}