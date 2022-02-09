package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ILeadsDetailServiceInterface
import com.google.gson.Gson

class LeadsDetailNetworkService {

    suspend fun getOrderCartByIdServerCall(
        serviceInterface: ILeadsDetailServiceInterface,
        id: String
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getOrderCartById(id)
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> serviceInterface.onGetOrderCartByIdResponse(commonApiResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onGetOrderCartByIdResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(LeadsDetailNetworkService::class.java.simpleName, "getOrderCartByIdServerCall: ", e)
            serviceInterface.onLeadsDetailException(e)
        }
    }
}