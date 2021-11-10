package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.PaymentModeRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IPaymentModesServiceInterface
import com.google.gson.Gson

class PaymentModesNetworkService {

    suspend fun getPaymentModesPageInfoServerCall(serviceInterface: IPaymentModesServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getPaymentModesPageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onPaymentModesResponse(storeNameResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(it.string(), CommonApiResponse::class.java)
                        serviceInterface.onPaymentModesResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(PaymentModesNetworkService::class.java.simpleName, "getPaymentModesPageInfoServerCall: ", e)
            serviceInterface.onPaymentModesServerException(e)
        }
    }

    suspend fun setPaymentOptionsServerCall(request: PaymentModeRequest, serviceInterface: IPaymentModesServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setPaymentOptions(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onSetPaymentModesResponse(storeNameResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(it.string(), CommonApiResponse::class.java)
                        serviceInterface.onSetPaymentModesResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(PaymentModesNetworkService::class.java.simpleName, "setPaymentOptions: ", e)
            serviceInterface.onPaymentModesServerException(e)
        }
    }

}