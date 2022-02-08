package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.*
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface
import com.google.gson.Gson

class OrderNetworkService {

    suspend fun getOrdersServerCall(
        request: OrdersRequest,
        serviceInterface: IHomeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getOrdersList(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse ->
                        if (request.orderMode == Constants.MODE_PENDING) serviceInterface.onPendingOrdersResponse(commonApiResponse)
                        else serviceInterface.onCompletedOrdersResponse(commonApiResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val validateOtpError = it.errorBody()
                    validateOtpError?.let {
                        val validateOtpErrorResponse = Gson().fromJson(
                            validateOtpError.string(),
                            ValidateOtpErrorResponse::class.java
                        )
                        serviceInterface.onOTPVerificationErrorResponse(
                            validateOtpErrorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "getOrdersServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getAnalyticsDataServerCall(serviceInterface: IHomeServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getAnalyticsData()
            response?.let {
                if (it.isSuccessful) it.body()?.let { validateUserResponse -> serviceInterface.onAnalyticsDataResponse(validateUserResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val validateOtpError = it.errorBody()
                    validateOtpError?.let {
                        val errorResponse = Gson().fromJson(validateOtpError.string(), CommonApiResponse::class.java)
                        serviceInterface.onAnalyticsDataResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "getAnalyticsDataServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getOrderPageInfoServerCall(serviceInterface: IHomeServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getOrderPageInfo()
            response?.let {
                if (it.isSuccessful) it.body()?.let { validateUserResponse -> serviceInterface.onOrderPageInfoResponse(validateUserResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val validateOtpError = it.errorBody()
                    validateOtpError?.let {
                        val errorResponse = Gson().fromJson(validateOtpError.string(), CommonApiResponse::class.java)
                        serviceInterface.onOrderPageInfoResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "getOrderPageInfoServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getSearchOrdersServerCall(
        request: SearchOrdersRequest,
        serviceInterface: IHomeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getSearchOrdersList(request)
            response?.let {
                if (it.isSuccessful) it.body()?.let { validateUserResponse -> serviceInterface.onSearchOrdersResponse(validateUserResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val validateOtpError = it.errorBody()
                    validateOtpError?.let {
                        val errorResponse = Gson().fromJson(validateOtpError.string(), CommonApiResponse::class.java)
                        serviceInterface.onSearchOrdersResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "getSearchOrdersServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun updateOrderStatusServerCall(
        statusRequest: UpdateOrderStatusRequest,
        serviceInterface: IHomeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateOrderStatus(statusRequest)
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.onOrdersUpdatedStatusResponse(commonApiResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onOrdersUpdatedStatusResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "updateOrderStatusServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun completeOrderServerCall(
        request: CompleteOrderRequest,
        serviceInterface: IHomeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.completeOrder(request)
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.onCompleteOrderStatusResponse(commonApiResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onCompleteOrderStatusResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "completeOrderServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getCustomDomainBottomSheetDataServerCall(serviceInterface: IHomeServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getCustomDomainBottomSheetData()
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.onCustomDomainBottomSheetDataResponse(commonApiResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onCustomDomainBottomSheetDataResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "getCustomDomainBottomSheetDataServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getLandingPageCardsServerCall(serviceInterface: IHomeServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getLandingPageCards()
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.onLandingPageCardsResponse(commonApiResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onLandingPageCardsResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "getLandingPageCardsServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getDomainSuggestionListServerCall(
        count: Int,
        serviceInterface: IHomeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getDomainSuggestionList(count)
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.onDomainSuggestionListResponse(commonApiResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onDomainSuggestionListResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "getLandingPageCardsServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun checkStaffInviteServerCall(serviceInterface: IHomeServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.checkStaffInvite()
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.checkStaffInviteResponse(commonApiResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.checkStaffInviteResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "checkStaffInviteServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getCartsByFiltersServerCall(
        serviceInterface: IHomeServiceInterface,
        request: LeadsListRequest
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getCartsByFilters(request)
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.onGetCartsByFiltersResponse(commonApiResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onGetCartsByFiltersResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "getCartsByFiltersServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }

    suspend fun getCartFilterOptionsServerCall(serviceInterface: IHomeServiceInterface, request: LeadsFilterOptionsRequest) {
        try {
//            val response = RetrofitApi().getServerCallObject()?.getCartFilterOptions(request)
            val response = RetrofitApi().getServerCallObject()?.getCartFilterOptions()
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.getCartFilterOptionsResponse(commonApiResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.getCartFilterOptionsResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "getCartFilterOptionsServerCall: ", e)
            serviceInterface.onHomePageException(e)
        }
    }
}