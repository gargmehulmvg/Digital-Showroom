package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.StoreDescriptionRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IStoreDescriptionServiceInterface
import com.google.gson.Gson

class StoreDescriptionNetworkService {

    suspend fun saveStoreDescriptionServerCall(
        request: StoreDescriptionRequest,
        serviceInterface: IStoreDescriptionServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setStoreDescription(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { profilePreviewResponse -> serviceInterface.onStoreDescriptionResponse(profilePreviewResponse) }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(
                        Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onStoreDescriptionResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(StoreDescriptionNetworkService::class.java.simpleName, "saveStoreDescriptionServerCall: ", e)
            serviceInterface.onStoreDescriptionServerException(e)
        }
    }

}