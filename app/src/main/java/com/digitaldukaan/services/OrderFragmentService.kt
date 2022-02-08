package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.*
import com.digitaldukaan.services.networkservice.OrderNetworkService
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface

class OrderFragmentService {

    private lateinit var mServiceInterface: IHomeServiceInterface

    private val mNetworkService = OrderNetworkService()

    fun setServiceListener(listener: IHomeServiceInterface) {
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

    fun getCustomDomainBottomSheetData() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getCustomDomainBottomSheetDataServerCall(mServiceInterface)
        }
    }

    fun getLandingPageCards() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getLandingPageCardsServerCall(mServiceInterface)
        }
    }

    fun getDomainSuggestionList(count: Int) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getDomainSuggestionListServerCall(count, mServiceInterface)
        }
    }

    fun checkStaffInvite() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.checkStaffInviteServerCall(mServiceInterface)
        }
    }

    fun getCartsByFilters(request: LeadsListRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getCartsByFiltersServerCall(mServiceInterface, request)
        }
    }

    fun getCartFilterOptions(request: LeadsFilterOptionsRequest) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mNetworkService.getCartFilterOptionsServerCall(mServiceInterface, request)
        }
    }



}