package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ISetOrderTypeServiceInterface
import com.google.gson.Gson

class SetOrderTypeNetworkService {

    suspend fun getOrderTypePageInfoServerCall(
        serviceInterface: ISetOrderTypeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getOrderTypePageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onSetOrderTypeResponse(storeNameResponse)
                    }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) {
                        throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    }
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onSetOrderTypeResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(SetOrderTypeNetworkService::class.java.simpleName, "getOrderTypePageInfoServerCall: ", e)
            serviceInterface.onSetOrderTypeException(e)
        }
    }

}