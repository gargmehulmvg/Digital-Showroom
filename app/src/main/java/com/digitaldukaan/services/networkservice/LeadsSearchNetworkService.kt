package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.LeadsListRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ILeadsSearchServiceInterface
import com.google.gson.Gson

class LeadsSearchNetworkService {

    suspend fun getCartsByFiltersServerCall(
        serviceInterface: ILeadsSearchServiceInterface,
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
            Log.e(LeadsSearchNetworkService::class.java.simpleName, "getCartsByFiltersServerCall: ", e)
            serviceInterface.onLeadsSearchException(e)
        }
    }
}