package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.UpdatePaymentMethodRequest
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
                    it.body()?.let { storeNameResponse -> serviceInterface.onSetOrderTypeResponse(storeNameResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(it.string(), CommonApiResponse::class.java)
                        serviceInterface.onSetOrderTypeResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(SetOrderTypeNetworkService::class.java.simpleName, "getOrderTypePageInfoServerCall: ", e)
            serviceInterface.onSetOrderTypeException(e)
        }
    }

    suspend fun updatePaymentMethodServerCall(
        request: UpdatePaymentMethodRequest,
        serviceInterface: ISetOrderTypeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updatePaymentMethod(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse -> serviceInterface.onUpdatePaymentMethodResponse(storeNameResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(it.string(), CommonApiResponse::class.java)
                        serviceInterface.onUpdatePaymentMethodResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(SetOrderTypeNetworkService::class.java.simpleName, "updatePaymentMethodServerCall: ", e)
            serviceInterface.onSetOrderTypeException(e)
        }
    }

}