package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.interfaces.IOnOTPFilledListener
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.models.response.ValidateOtpResponse
import com.digitaldukaan.services.OtpVerificationService
import com.digitaldukaan.services.serviceinterface.IOtpVerificationServiceInterface
import com.digitaldukaan.smsapi.ISmsReceivedListener
import com.google.android.gms.auth.api.phone.SmsRetriever
import kotlinx.android.synthetic.main.otp_verification_fragment.*


class OtpVerificationFragment : BaseFragment(), IOnOTPFilledListener, IOtpVerificationServiceInterface, ISmsReceivedListener {

    private lateinit var mCountDownTimer: CountDownTimer
    private lateinit var mOtpVerificationService: OtpVerificationService
    private var mEnteredOtpStr = ""
    private var mMobileNumberStr = ""

    fun newInstance(mobileNumber: String): OtpVerificationFragment {
        val fragment = OtpVerificationFragment()
        fragment.mMobileNumberStr = mobileNumber
        return fragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val client = SmsRetriever.getClient(mActivity)
        val task = client.startSmsRetriever()
        task.addOnSuccessListener {
            Log.d("OtpVerificationFragment", "onCreate: Auto read SMS retrieval task success")
        }
        task.addOnFailureListener {
            Log.d("OtpVerificationFragment", "onCreate: Auto read SMS retrieval task failed")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.otp_verification_fragment, container, false)
        mOtpVerificationService = OtpVerificationService()
        mOtpVerificationService.setOtpVerificationListener(this)
        return mContentView
    }

    override fun onClick(view: View?) {
        if (view?.id == verifyTextView.id) {
            mCountDownTimer.cancel()
            showCancellableProgressDialog(mActivity)
            mOtpVerificationService.verifyOTP(mMobileNumberStr, mEnteredOtpStr.toInt())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        otpEditText.setOtpFilledListener(this)
        startCountDownTimer()
    }

    private fun startCountDownTimer() {
        mCountDownTimer = object: CountDownTimer(Constants.RESEND_OTP_TIMER, Constants.TIMER_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    resendOtpTextView.text = """${(millisUntilFinished / 1000)} seconds"""
                }
            }

            override fun onFinish() {
                CoroutineScopeUtils().runTaskOnCoroutineMain { resendOtpTextView.text = getString(R.string.resend_otp) }
            }
        }
        mCountDownTimer.start()
    }

    override fun onStart() {
        super.onStart()
        otpEditText.clearOTP()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDownTimer.cancel()
    }

    override fun onOTPFilledListener(otpStr: String) {
        mEnteredOtpStr = otpStr
        otpEditText.hideKeyboard()
        verifyTextView.isEnabled = true
        verifyTextView.callOnClick()
    }

    override fun onOTPVerificationSuccessResponse(validateOtpResponse: ValidateOtpResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showToast(validateOtpResponse.mMessage)
            if (validateOtpResponse.mIsSuccessStatus) {
                launchFragment(OnBoardScreenDukaanNameFragment(), true)
            }
        }

    }

    override fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showToast(validateOtpErrorResponse.mMessage)
        }
    }

    override fun onNewSmsReceived(sms: String?) {
        showToast(sms)
    }
}