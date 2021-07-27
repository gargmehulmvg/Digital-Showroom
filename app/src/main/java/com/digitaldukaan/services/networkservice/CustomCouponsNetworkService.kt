package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.CreateCouponsRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.ICustomCouponsServiceInterface
import com.digitaldukaan.services.serviceinterface.IPromoCodePageInfoServiceInterface
import com.google.gson.Gson

class CustomCouponsNetworkService {

    suspend fun getCreatePromoCodeServerCall(
        serviceInterface: ICustomCouponsServiceInterface,
        request: CreateCouponsRequest
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.createPromoCode(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { generateOtpResponse -> serviceInterface.onCustomCouponsResponse(generateOtpResponse) }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    serviceInterface.onCustomCouponsErrorResponse(Exception(response.message()))
                }
            }
        } catch (e : Exception) {
            Log.e(CustomCouponsNetworkService::class.java.simpleName, "getMarketingCardsDataServerCall: ", e)
            serviceInterface.onCustomCouponsErrorResponse(e)
        }
    }

    suspend fun getPromoCodePageInfoServerCall(
        serviceInterface: IPromoCodePageInfoServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getPromoCodePageInfo()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { generateOtpResponse -> serviceInterface.onPromoCodePageInfoResponse(generateOtpResponse) }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onPromoCodePageInfoResponse(errorResponse)
                    }
                }
            }
        } catch (e : Exception) {
            Log.e(CustomCouponsNetworkService::class.java.simpleName, "getPromoCodePageInfoServerCall: ", e)
            serviceInterface.onPromoCodePageInfoException(e)
        }
    }

}