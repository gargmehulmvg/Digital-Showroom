package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
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
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    serviceInterface.onBusinessTypeServerException(Exception(response.message()))
                }
            }
        } catch (e: Exception) {
            Log.e(BusinessTypeNetworkService::class.java.simpleName, "saveStoreDescriptionServerCall: ", e)
            serviceInterface.onBusinessTypeServerException(e)
        }
    }

    suspend fun setStoreBusinessesServerCall(
        businessTypeRequest: BusinessTypeRequest,
        serviceInterface: IBusinessTypeServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.setStoreBusinesses(businessTypeRequest)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { profilePreviewResponse -> serviceInterface.onSavingBusinessTypeResponse(profilePreviewResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    serviceInterface.onBusinessTypeServerException(Exception(response.message()))
                }
            }
        } catch (e: Exception) {
            Log.e(BusinessTypeNetworkService::class.java.simpleName, "saveStoreDescriptionServerCall: ", e)
            serviceInterface.onBusinessTypeServerException(e)
        }
    }

}