package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.StoreAddressRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IStoreAddressServiceInterface
import com.google.gson.Gson

class StoreAddressNetworkService {

    suspend fun updateStoreAddressServerCall(
        storeAddressRequest: StoreAddressRequest,
        serviceInterface: IStoreAddressServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateStoreAddress(storeAddressRequest)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onStoreAddressResponse(storeNameResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    throw Exception(response.message())
                }
            }
        } catch (e: Exception) {
            Log.e(StoreAddressNetworkService::class.java.simpleName, "updateStoreAddressServerCall: ", e)
            serviceInterface.onStoreAddressServerException(e)
        }
    }

    suspend fun getStoreLocationServerCall(serviceInterface: IStoreAddressServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getStoreLocation()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { commonApiResponse -> serviceInterface.onGetStoreLocationResponse(commonApiResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onGetStoreLocationResponse(errorResponse)
                    }
                }
            }
        } catch (e : Exception) {
            Log.e(StoreAddressNetworkService::class.java.simpleName, "getStoreLocationServerCall: ", e)
            serviceInterface.onStoreAddressServerException(e)
        }
    }

}