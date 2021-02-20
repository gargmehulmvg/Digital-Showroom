package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.interfaces.IOnOTPFilledListener
import kotlinx.android.synthetic.main.otp_verification_fragment.*


class OtpVerificationFragment : BaseFragment(), IOnOTPFilledListener {

    private lateinit var timer: CountDownTimer

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.otp_verification_fragment, container, false)
        return mContentView
    }

    override fun onClick(view: View?) {
        if (view?.id == verifyTextView.id) {
            timer.cancel()
            launchFragment(OnBoardScreenDukaanNameFragment(), true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        otpEditText.setOtpFilledListener(this)
        startCountDownTimer()
    }

    private fun startCountDownTimer() {
        timer = object: CountDownTimer(Constants.RESEND_OTP_TIMER, Constants.TIMER_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    resendOtpTextView.text = """${(millisUntilFinished / 1000)} seconds"""
                }
            }

            override fun onFinish() {
                CoroutineScopeUtils().runTaskOnCoroutineMain { resendOtpTextView.text = getString(R.string.resend_otp) }
            }
        }
        timer.start()
    }

    override fun onStart() {
        super.onStart()
        otpEditText.clearOTP()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    override fun onOTPFilledListener(otpStr: String) {
        otpEditText.hideKeyboard()
        verifyTextView.isEnabled = true
        verifyTextView.callOnClick()
    }
}