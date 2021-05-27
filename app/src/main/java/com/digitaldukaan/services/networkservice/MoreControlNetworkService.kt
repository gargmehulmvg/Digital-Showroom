package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.MoreControlsRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IMoreControlsServiceInterface
import com.google.gson.Gson

class MoreControlNetworkService {

    suspend fun updateDeliveryInfoServerCall(
        authToken:String,
        request : MoreControlsRequest,
        serviceInterface: IMoreControlsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateDeliveryInfo(authToken, request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onMoreControlsResponse(storeNameResponse)
                    }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) {
                        throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    }
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(it.string(), CommonApiResponse::class.java)
                        serviceInterface.onMoreControlsResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(MoreControlNetworkService::class.java.simpleName, "updateDeliveryInfoServerCall: ", e)
            serviceInterface.onMoreControlsServerException(e)
        }
    }

}