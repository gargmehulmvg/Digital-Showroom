package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.MoreControlsRequest
import com.digitaldukaan.models.request.StoreDeliveryStatusChangeRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IMoreControlsServiceInterface
import com.google.gson.Gson

class MoreControlNetworkService {

    suspend fun getMoreControlsPageInfoServerCall(
        serviceInterface: IMoreControlsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getStoreControlPageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onMoreControlsPageInfoResponse(storeNameResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(it.string(), CommonApiResponse::class.java)
                        serviceInterface.onMoreControlsPageInfoResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(MoreControlNetworkService::class.java.simpleName, "getMoreControlsPageInfoServerCall: ", e)
            serviceInterface.onMoreControlsServerException(e)
        }
    }

    suspend fun updateDeliveryInfoServerCall(
        request: MoreControlsRequest,
        serviceInterface: IMoreControlsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateDeliveryInfo(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onMoreControlsResponse(storeNameResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
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

    suspend fun changeStoreAndDeliveryStatusServerCall(
        request : StoreDeliveryStatusChangeRequest,
        serviceInterface: IMoreControlsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.changeStoreAndDeliveryStatus(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse ->
                        serviceInterface.onChangeStoreAndDeliveryStatusResponse(commonApiResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(it.string(), CommonApiResponse::class.java)
                        serviceInterface.onChangeStoreAndDeliveryStatusResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(MoreControlNetworkService::class.java.simpleName, "changeStoreAndDeliveryStatusServerCall: ", e)
            serviceInterface.onMoreControlsServerException(e)
        }
    }

}