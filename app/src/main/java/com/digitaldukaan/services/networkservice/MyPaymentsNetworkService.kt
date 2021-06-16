package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IMyPaymentsServiceInterface
import com.google.gson.Gson

class MyPaymentsNetworkService {

    suspend fun getMyPaymentsListServerCall(
        pageNo: Int, startDate: String?, endDate: String?,
        serviceInterface: IMyPaymentsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getMyPaymentsList(
                pageNo = pageNo,
                startDate = startDate,
                endDate = endDate,
                listType = "all"
            )
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onMyPaymentsListResponse(storeNameResponse)
                    }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(it.string(), CommonApiResponse::class.java)
                        serviceInterface.onMyPaymentsListResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(MyPaymentsNetworkService::class.java.simpleName, "getMyPaymentsListServerCall: ", e)
            serviceInterface.onMyPaymentsServerException(e)
        }
    }

}