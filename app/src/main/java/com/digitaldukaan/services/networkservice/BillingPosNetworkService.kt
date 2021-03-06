package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.RequestToCallbackRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IBillingPosServiceInterface
import com.google.gson.Gson

class BillingPosNetworkService {

    companion object {
        private const val BILLING_POS_YES   = "billing_pos_yes"
        private const val BILLING_POS_NO    = "billing_pos_no"
    }

    suspend fun getPosBillingPageInfoServerCall(serviceInterface: IBillingPosServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getPosBillingPageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse -> serviceInterface.onBillingPosPageInfoResponse(storeNameResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let { body ->
                        val errorResponse = Gson().fromJson(body.string(), CommonApiResponse::class.java)
                        serviceInterface.onBillingPosPageInfoResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(BillingPosNetworkService::class.java.simpleName, "getPosBillingPageInfoServerCall: ", e)
            serviceInterface.onBillingPosServerException(e)
        }
    }

    suspend fun requestACallBackServerCall(
        serviceInterface: IBillingPosServiceInterface,
        isYesButtonClicked: Boolean
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.updateCallbackFlag(RequestToCallbackRequest(text = if (isYesButtonClicked) BILLING_POS_YES else BILLING_POS_NO))
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { storeNameResponse -> serviceInterface.onRequestCallBackResponse(storeNameResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val responseBody = it.errorBody()
                    responseBody?.let { body ->
                        val errorResponse = Gson().fromJson(body.string(), CommonApiResponse::class.java)
                        serviceInterface.onRequestCallBackResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(BillingPosNetworkService::class.java.simpleName, "requestACallBackServerCall: ", e)
            serviceInterface.onBillingPosServerException(e)
        }
    }

}