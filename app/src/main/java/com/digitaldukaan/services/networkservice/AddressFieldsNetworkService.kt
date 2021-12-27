package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IAddressFieldsServiceInterface
import com.google.gson.Gson

class AddressFieldsNetworkService {

    suspend fun getAddressFieldsPageInfoServerCall(
        serviceInterface: IAddressFieldsServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getAddressFieldsPageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse ->
                        serviceInterface.onAddressFieldsPageInfoResponse(storeNameResponse)
                    }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let { body ->
                        val errorResponse = Gson().fromJson(body.string(), CommonApiResponse::class.java)
                        serviceInterface.onAddressFieldsPageInfoResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(AddressFieldsNetworkService::class.java.simpleName, "getAddressFieldsPageInfoServerCall: ", e)
            serviceInterface.onAddressFieldsServerException(e)
        }
    }

}