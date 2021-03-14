package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.OrdersRequest
import com.digitaldukaan.models.request.SearchOrdersRequest
import com.digitaldukaan.services.networkservice.HomeNetworkService
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface

class HomeFragmentService {

    private lateinit var mServiceInterface: IHomeServiceInterface

    private val mNetworkService = HomeNetworkService()

    fun setHomeFragmentServiceListener(listener: IHomeServiceInterface) {
        mServiceInterface = listener
    }

    fun getOrders(authToken: String, request: OrdersRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getOrdersServerCall(authToken , request, mServiceInterface)
        }
    }

    fun getSearchOrders(authToken: String, request: SearchOrdersRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getSearchOrdersServerCall(authToken , request, mServiceInterface)
        }
    }

    fun getAnalyticsData(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getAnalyticsDataServerCall(authToken, mServiceInterface)
        }
    }

    fun getOrderPageInfo(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getOrderPageInfoServerCall(authToken, mServiceInterface)
        }
    }

}