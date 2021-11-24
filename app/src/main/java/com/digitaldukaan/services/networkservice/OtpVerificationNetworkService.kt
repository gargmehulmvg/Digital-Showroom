package com.digitaldukaan.services.networkservice

import android.util.Log
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.models.request.ValidateOtpRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface
import com.digitaldukaan.services.serviceinterface.IOtpVerificationServiceInterface
import com.google.gson.Gson

class OtpVerificationNetworkService {

    suspend fun verifyOTPServerCall(
        mobileNumber: String,
        otpStr: Int,
        otpVerificationServiceInterface: IOtpVerificationServiceInterface
    ) {
        try {
            val response = RetrofitApi().getServerCallObject()?.validateOTP(ValidateOtpRequest(otpStr, StaticInstances.sCleverTapId, StaticInstances.sFireBaseMessagingToken, mobileNumber))
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { validateOtpSuccessResponse -> otpVerificationServiceInterface.onOTPVerificationSuccessResponse(validateOtpSuccessResponse) }
                } else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    if (Constants.ERROR_CODE_FORCE_UPDATE == it.code()) throw DeprecateAppVersionException()
                    val validateOtpError = it.errorBody()
                    validateOtpError?.let {
                        val validateOtpErrorResponse = Gson().fromJson(
                            validateOtpError.string(),
                            ValidateOtpErrorResponse::class.java
                        )
                        otpVerificationServiceInterface.onOTPVerificationErrorResponse(validateOtpErrorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(SplashNetworkService::class.java.simpleName, "verifyOTPServerCall: ", e)
            otpVerificationServiceInterface.onOTPVerificationDataException(e)
        }
    }

    suspend fun checkStaffInviteServerCall(otpVerificationServiceInterface: IOtpVerificationServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.checkStaffInvite()
            response?.let {
                if (it.isSuccessful) it.body()?.let { commonApiResponse -> otpVerificationServiceInterface.checkStaffInviteResponse(commonApiResponse) }
                else {
                    if (Constants.ERROR_CODE_UN_AUTHORIZED_ACCESS == it.code() || Constants.ERROR_CODE_FORBIDDEN_ACCESS == it.code()) throw UnAuthorizedAccessException(Constants.ERROR_MESSAGE_UN_AUTHORIZED_ACCESS)
                    val responseBody = it.errorBody()
                    responseBody?.let {
                        val errorResponse = Gson().fromJson(responseBody.string(), CommonApiResponse::class.java)
                        otpVerificationServiceInterface.checkStaffInviteResponse(errorResponse)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(OrderNetworkService::class.java.simpleName, "checkStaffInviteServerCall: ", e)
            otpVerificationServiceInterface.onOTPVerificationDataException(e)
        }
    }

    suspend fun getOtpModesListServerCall(otpVerificationServiceInterface: IOtpVerificationServiceInterface) {
        try {
            val response = RetrofitApi().getServerCallObject()?.getOtpModesList()
            response?.let {
                if (it.isSuccessful) {
                    it.body()?.let { getOtpModesList ->
                        otpVerificationServiceInterface.onGetOtpModesListResponse(getOtpModesList)
                    }
                } else otpVerificationServiceInterface.onGetOtpModesListDataException(Exception(response.message()))
            }
        } catch (e: Exception) {
            Log.e(SplashNetworkService::class.java.simpleName, "getHelpScreensServerCall :: ", e)
            otpVerificationServiceInterface.onGetOtpModesListDataException(e)
        }
    }

}