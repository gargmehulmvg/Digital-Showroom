package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.CreateStoreRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ICreateStoreServiceInterface
import com.google.gson.Gson

class CreateStoreNetworkService {

    suspend fun createStoreServerCall(
        request: CreateStoreRequest?,
        serviceInterface: ICreateStoreServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.createStore(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { profilePreviewResponse ->
                        serviceInterface.onCreateStoreResponse(profilePreviewResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(
                        Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(
                            responseBody.string(),
                            CommonApiResponse::class.java
                        )
                        serviceInterface.onCreateStoreResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(CreateStoreNetworkService::class.java.simpleName, "createStoreServerCall: ", e)
            serviceInterface.onCreateStoreServerException(e)
        }
    }

}