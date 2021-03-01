package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.models.request.BusinessTypeRequest
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IBusinessTypeServiceInterface

class BusinessTypeNetworkService {

    suspend fun getBusinessListServerCall(
        serviceInterface: IBusinessTypeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getBusinessList()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { profilePreviewResponse -> serviceInterface.onBusinessTypeResponse(profilePreviewResponse) }
                } else serviceInterface.onBusinessTypeServerException(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(BusinessTypeNetworkService::class.java.simpleName, "saveStoreDescriptionServerCall: ", e)
            serviceInterface.onBusinessTypeServerException(e)
        }
    }

    suspend fun setStoreBusinessesServerCall(
        authToken:String,
        businessTypeRequest : BusinessTypeRequest,
        serviceInterface: IBusinessTypeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setStoreBusinesses(authToken, businessTypeRequest)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { profilePreviewResponse -> serviceInterface.onSavingBusinessTypeResponse(profilePreviewResponse) }
                } else serviceInterface.onBusinessTypeServerException(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(BusinessTypeNetworkService::class.java.simpleName, "saveStoreDescriptionServerCall: ", e)
            serviceInterface.onBusinessTypeServerException(e)
        }
    }

}