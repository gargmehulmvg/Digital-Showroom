package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.CompleteOrderRequest
import com.digitaldukaan.models.request.UpdateOrderRequest
import com.digitaldukaan.models.request.UpdateOrderStatusRequest
import com.digitaldukaan.models.request.UpdatePrepaidOrderRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IOrderDetailServiceInterface
import com.google.gson.Gson

class OrderDetailNetworkService {

    suspend fun getOrderDetailServerCall(
        orderId: String,
        serviceInterface: IOrderDetailServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getOrderDetails(orderId)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onOrderDetailResponse(commonApiResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onOrderDetailResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderDetailNetworkService::class.java.simpleName, "getOrderDetailServerCall: ", e)
            serviceInterface.onOrderDetailException(e)
        }
    }

    suspend fun getDeliveryTimeServerCall(
        serviceInterface: IOrderDetailServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getDeliveryTime()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onDeliveryTimeResponse(commonApiResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onDeliveryTimeResponse(
                            errorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderDetailNetworkService::class.java.simpleName, "getDeliveryTimeServerCall: ", e)
            serviceInterface.onOrderDetailException(e)
        }
    }

    suspend fun updateOrderServerCall(
        request: UpdateOrderRequest,
        serviceInterface: IOrderDetailServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateOrder(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onUpdateOrderResponse(commonApiResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onUpdateOrderResponse(
                            errorResponse
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(SendBillPhotoNetworkService::class.java.simpleName, "updateOrderServerCall: ", e)
            serviceInterface.onOrderDetailException(e)
        }
    }

    suspend fun updatePrepaidOrderServerCall(
        orderId: String?,
        request: UpdatePrepaidOrderRequest?,
        serviceInterface: IOrderDetailServiceInterface
    ) {
        try {
            val newRequest: UpdatePrepaidOrderRequest = request ?: UpdatePrepaidOrderRequest(null, null, null)
            val response = RetrofitApi().getServerCallObject()?.updatePrepaidOrder(orderId, newRequest)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onPrepaidOrderUpdateStatusResponse(commonApiResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onPrepaidOrderUpdateStatusResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(SendBillPhotoNetworkService::class.java.simpleName, "updatePrepaidOrderServerCall: ", e)
            serviceInterface.onOrderDetailException(e)
        }
    }

    suspend fun updateOrderStatusServerCall(
        request: UpdateOrderStatusRequest,
        serviceInterface: IOrderDetailServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateOrderStatus(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onUpdateStatusResponse(commonApiResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onUpdateStatusResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(SendBillPhotoNetworkService::class.java.simpleName, "updateOrderStatusServerCall: ", e)
            serviceInterface.onOrderDetailException(e)
        }
    }

    suspend fun shareBillResponseServerCall(
        orderId: String,
        serviceInterface: IOrderDetailServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.shareBill(orderId)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onShareBillResponse(commonApiResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onShareBillResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(SendBillPhotoNetworkService::class.java.simpleName, "shareBillResponseServerCall: ", e)
            serviceInterface.onOrderDetailException(e)
        }
    }

    suspend fun completeOrderServerCall(
        request: CompleteOrderRequest,
        serviceInterface: IOrderDetailServiceInterface
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
            serviceInterface.onOrderDetailException(e)
        }
    }

    suspend fun sharePaymentLink(
        orderId: String?,
        serviceInterface: IOrderDetailServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.sharePaymentLink(orderId)
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.onSharePaymentLinkResponse(commonApiResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onSharePaymentLinkResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "sharePaymentLink: ", e)
            serviceInterface.onOrderDetailException(e)
        }
    }

}