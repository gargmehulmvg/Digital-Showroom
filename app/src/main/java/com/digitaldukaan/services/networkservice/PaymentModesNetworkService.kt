package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IPaymentModesServiceInterface
import com.google.gson.Gson

class PaymentModesNetworkService {

    suspend fun getPaymentModesPageInfo(
        serviceInterface: IPaymentModesServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getPaymentModesPageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onPaymentModesResponse(storeNameResponse)
                    }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(it.string(), CommonApiResponse::class.java)
                        serviceInterface.onPaymentModesResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(PaymentModesNetworkService::class.java.simpleName, "updateDeliveryInfoServerCall: ", e)
            serviceInterface.onPaymentModesServerException(e)
        }
    }

}