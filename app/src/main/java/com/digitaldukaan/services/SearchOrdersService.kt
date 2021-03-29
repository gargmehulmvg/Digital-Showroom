package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.SearchOrdersRequest
import com.digitaldukaan.models.request.UpdateOrderStatusRequest
import com.digitaldukaan.services.networkservice.SearchOrderNetworkService
import com.digitaldukaan.services.serviceinterface.ISearchOrderServiceInterface

class SearchOrdersService {

    private lateinit var mServiceInterface: ISearchOrderServiceInterface

    private val mNetworkService = SearchOrderNetworkService()

    fun setHomeFragmentServiceListener(listener: ISearchOrderServiceInterface) {
        mServiceInterface = listener
    }

    fun getSearchOrders(authToken: String, request: SearchOrdersRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getSearchOrdersServerCall(authToken , request, mServiceInterface)
        }
    }

    fun updateOrderStatus(authToken: String, statusRequest: UpdateOrderStatusRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.updateOrderStatusServerCall(authToken , statusRequest, mServiceInterface)
        }
    }

}