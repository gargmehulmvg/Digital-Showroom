package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.CreateCouponsRequest
import com.digitaldukaan.models.request.GetPromoCodeRequest
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
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onCustomCouponsResponse(errorResponse)
                    }
                }
            }
        } catch (e : Exception) {
            Log.e(CustomCouponsNetworkService::class.java.simpleName, "getMarketingCardsDataServerCall: ", e)
            serviceInterface.onCustomCouponsErrorResponse(e)
        }
    }

    suspend fun getAllMerchantPromoCodesServerCall(
        serviceInterface: IPromoCodePageInfoServiceInterface,
        request: GetPromoCodeRequest
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getAllMerchantPromoCodes(request)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { generateOtpResponse -> serviceInterface.onGetPromoCodeListResponse(generateOtpResponse) }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onGetPromoCodeListResponse(errorResponse)
                    }
                }
            }
        } catch (e : Exception) {
            Log.e(CustomCouponsNetworkService::class.java.simpleName, "getAllMerchantPromoCodesServerCall: ", e)
            serviceInterface.onPromoCodePageInfoException(e)
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

    suspend fun getPromoCodePageInfoServerCallV2(
        serviceInterface: ICustomCouponsServiceInterface
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
            serviceInterface.onCustomCouponsErrorResponse(e)
        }
    }

    suspend fun getCouponDetailsServerCall(
        promoCode: String,
        serviceInterface: IPromoCodePageInfoServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getCouponDetails(promoCode)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { generateOtpResponse -> serviceInterface.onPromoCodeDetailResponse(generateOtpResponse) }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onPromoCodeDetailResponse(errorResponse)
                    }
                }
            }
        } catch (e : Exception) {
            Log.e(CustomCouponsNetworkService::class.java.simpleName, "getCouponDetailsServerCall: ", e)
            serviceInterface.onPromoCodePageInfoException(e)
        }
    }

    suspend fun shareCouponServerCall(
        promoCode: String,
        serviceInterface: IPromoCodePageInfoServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.shareCoupon(promoCode)
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { generateOtpResponse -> serviceInterface.onPromoCodeShareResponse(generateOtpResponse) }
                } else {
                    if (it.code() == Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val errorResponseBody = it.errorBody()
                    errorResponseBody?.let {
                        val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                        serviceInterface.onPromoCodeShareResponse(errorResponse)
                    }
                }
            }
        } catch (e : Exception) {
            Log.e(CustomCouponsNetworkService::class.java.simpleName, "shareCouponServerCall: ", e)
            serviceInterface.onPromoCodePageInfoException(e)
        }
    }

}