package com.digitaldukaan.services

import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.services.networkservice.OtpVerificationNetworkService
import com.digitaldukaan.services.serviceinterface.IOtpVerificationServiceInterface

class OtpVerificationService {

    private lateinit var mOtpVerificationInterface : IOtpVerificationServiceInterface

    fun setOtpVerificationListener(listener: IOtpVerificationServiceInterface) {
        mOtpVerificationInterface = listener
    }

    private val mOtpVerificationNetwork = OtpVerificationNetworkService()

    fun verifyOTP(mobileNumber: String, otp:Int) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mOtpVerificationNetwork.verifyOTPServerCall(mobileNumber, otp, mOtpVerificationInterface)
        }
    }

    fun getOtpModesList() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mOtpVerificationNetwork.getOtpModesListServerCall(mOtpVerificationInterface)
        }
    }

    fun checkStaffInvite() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            mOtpVerificationNetwork.checkStaffInviteServerCall(mOtpVerificationInterface)
        }
    }

}