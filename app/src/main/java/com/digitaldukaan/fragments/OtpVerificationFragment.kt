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
import com.digitaldukaan.models.response.GenerateOtpResponse
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.models.response.ValidateOtpResponse
import com.digitaldukaan.services.LoginService
import com.digitaldukaan.services.OtpVerificationService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ILoginServiceInterface
import com.digitaldukaan.services.serviceinterface.IOtpVerificationServiceInterface
import com.digitaldukaan.smsapi.AppSignatureHelper
import com.digitaldukaan.smsapi.ISmsReceivedListener
import com.google.android.gms.auth.api.phone.SmsRetriever
import kotlinx.android.synthetic.main.otp_verification_fragment.*


class OtpVerificationFragment : BaseFragment(), IOnOTPFilledListener, IOtpVerificationServiceInterface, ISmsReceivedListener,
    ILoginServiceInterface {

    private lateinit var mCountDownTimer: CountDownTimer
    private lateinit var mOtpVerificationService: OtpVerificationService
    private var mEnteredOtpStr = ""
    private var mMobileNumberStr = ""
    private lateinit var mLoginService: LoginService
    private val mOtpStaticResponseData = mStaticData.mStaticData.mVerifyOtpStaticData

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
        showToast("App Signature is ${AppSignatureHelper(mActivity).appSignatures[0]}")
        Log.d("OtpVerificationFragment", "App Signature is ${AppSignatureHelper(mActivity).appSignatures[0]}")
        mLoginService = LoginService()
        mLoginService.setLoginServiceInterface(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.otp_verification_fragment, container, false)
        mOtpVerificationService = OtpVerificationService()
        mOtpVerificationService.setOtpVerificationListener(this)
        return mContentView
    }

    private fun setupUIFromStaticResponse() {
        enterMobileNumberHeading.text = mOtpStaticResponseData.mHeadingText
        verifyTextView.text = mOtpStaticResponseData.mVerifyText
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            verifyTextView.id -> {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                mCountDownTimer.cancel()
                showProgressDialog(mActivity, mOtpStaticResponseData.mVerifyingText)
                mOtpVerificationService.verifyOTP(mMobileNumberStr, mEnteredOtpStr.toInt())
            }
            resendOtpTextView.id -> {
                if (mOtpStaticResponseData.mResendButtonText == resendOtpTextView.text) {
                    startCountDownTimer()
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return
                    }
                    showProgressDialog(mActivity)
                    mLoginService.generateOTP(mMobileNumberStr)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUIFromStaticResponse()
        otpEditText.setOtpFilledListener(this)
        startCountDownTimer()
    }

    private fun startCountDownTimer() {
        mCountDownTimer = object: CountDownTimer(Constants.RESEND_OTP_TIMER, Constants.TIMER_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    resendOtpTextView.text = "${(millisUntilFinished / 1000)} ${mOtpStaticResponseData.mSecondText}"
                }
            }

            override fun onFinish() {
                CoroutineScopeUtils().runTaskOnCoroutineMain { resendOtpTextView.text = mOtpStaticResponseData.mResendButtonText }
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
            saveUserDetailsInPref(validateOtpResponse)
            if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
            } else {
                showProgressDialog(mActivity, getString(R.string.authenticating_user))
                CoroutineScopeUtils().runTaskOnCoroutineBackground {
                    mOtpVerificationService.verifyUserAuthentication(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
                }
            }
        }

    }

    private fun saveUserDetailsInPref(validateOtpResponse: ValidateOtpResponse) {
        storeStringDataInSharedPref(Constants.USER_AUTH_TOKEN, validateOtpResponse.mUserAuthToken)
        storeStringDataInSharedPref(Constants.USER_ID, validateOtpResponse.mUserId)
        storeStringDataInSharedPref(Constants.USER_MOBILE_NUMBER, validateOtpResponse.mUserPhoneNumber)
        storeStringDataInSharedPref(Constants.STORE_NAME, validateOtpResponse.mStore)
    }

    override fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            otpEditText.clearOTP()
            showToast(validateOtpErrorResponse.mMessage)
        }
    }

    override fun onUserAuthenticationResponse(authenticationUserResponse: ValidateOtpResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            saveUserDetailsInPref(authenticationUserResponse)
            if (authenticationUserResponse.mIsSuccessStatus) launchFragment(OnBoardScreenDukaanNameFragment(), true)
        }
    }

    override fun onNewSmsReceived(sms: String?) = showToast(sms)

    override fun onGenerateOTPResponse(generateOtpResponse: GenerateOtpResponse) {
        stopProgress()
    }

    override fun onGenerateOTPException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }
}