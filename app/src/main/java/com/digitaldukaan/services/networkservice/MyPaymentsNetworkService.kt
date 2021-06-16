package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IMyPaymentsServiceInterface
import com.google.gson.Gson

class MyPaymentsNetworkService {

    suspend fun getTransactionsListServerCall(
        pageNo: Int, startDate: String?, endDate: String?,
        serviceInterface: IMyPaymentsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getTransactionsList(
                pageNo = pageNo,
                startDate = startDate,
                endDate = endDate,
                listType = "all"
            )
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onGetTransactionsListResponse(storeNameResponse)
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

    suspend fun getSettlementListServerCall(
        pageNo: Int, startDate: String?, endDate: String?,
        serviceInterface: IMyPaymentsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getSettlementList(
                pageNo = pageNo,
                startDate = startDate,
                endDate = endDate,
                listType = "all"
            )
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onGetTransactionsListResponse(storeNameResponse)
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
            Log.e(MyPaymentsNetworkService::class.java.simpleName, "getSettlementListServerCall: ", e)
            serviceInterface.onMyPaymentsServerException(e)
        }
    }

}