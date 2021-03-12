package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.OrdersRequest
import com.digitaldukaan.services.networkservice.HomeNetworkService
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface

class HomeFragmentService {

    private lateinit var mServiceInterface: IHomeServiceInterface

    private val mNetworkService = HomeNetworkService()

    fun setHomeFragmentServiceListener(listener: IHomeServiceInterface) {
        mServiceInterface = listener
    }

    fun verifyUserAuthentication(authToken: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.authenticateUserServerCall(authToken, mServiceInterface)
        }
    }

    fun getOrders(authToken: String, request: OrdersRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getOrdersServerCall(authToken , request, mServiceInterface)
        }
    }

    fun getCompletedOrders(storeId: String, page: Int) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getCompletedOrdersServerCall(storeId , page, mServiceInterface)
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