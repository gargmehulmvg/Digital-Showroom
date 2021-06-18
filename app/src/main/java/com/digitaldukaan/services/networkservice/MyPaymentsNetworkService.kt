package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.TransactionRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IMyPaymentsServiceInterface
import com.google.gson.Gson

class MyPaymentsNetworkService {

    suspend fun getTransactionsListServerCall(
        request: TransactionRequest,
        serviceInterface: IMyPaymentsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getTransactionsList(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse ->
                        serviceInterface.onGetTransactionsListResponse(commonApiResponse)
                    }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(it.string(), CommonApiResponse::class.java)
                        serviceInterface.onGetTransactionsListResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(MyPaymentsNetworkService::class.java.simpleName, "getTransactionsListServerCall: ", e)
            serviceInterface.onMyPaymentsServerException(e)
        }
    }

    suspend fun getMyPaymentPageInfoServerCall(
        serviceInterface: IMyPaymentsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getMyPaymentsPageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onGetMyPaymentPageInfoResponse(commonApiResponse) }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(it.string(), CommonApiResponse::class.java)
                        serviceInterface.onGetMyPaymentPageInfoResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(MyPaymentsNetworkService::class.java.simpleName, "getMyPaymentPageInfoServerCall: ", e)
            serviceInterface.onMyPaymentsServerException(e)
        }
    }

}