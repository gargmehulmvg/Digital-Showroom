package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.appsflyer.AppsFlyerLib
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IOnOTPFilledListener
import com.digitaldukaan.models.dto.CleverTapProfile
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.LoginService
import com.digitaldukaan.services.OtpVerificationService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ILoginServiceInterface
import com.digitaldukaan.services.serviceinterface.IOtpVerificationServiceInterface
import com.digitaldukaan.smsapi.AppSignatureHelper
import com.digitaldukaan.smsapi.ISmsReceivedListener
import com.digitaldukaan.smsapi.MySMSBroadcastReceiver
import com.google.android.gms.auth.api.phone.SmsRetriever
import kotlinx.android.synthetic.main.otp_verification_fragment.*


class OtpVerificationFragment : BaseFragment(), IOnOTPFilledListener, IOtpVerificationServiceInterface, ISmsReceivedListener,
    ILoginServiceInterface {

    private lateinit var mCountDownTimer: CountDownTimer
    private lateinit var mOtpVerificationService: OtpVerificationService
    private var mEnteredOtpStr = ""
    private var mMobileNumberStr = ""
    private lateinit var mLoginService: LoginService
    private var mIsNewUser: Boolean = false
    private var mIsServerCallInitiated: Boolean = false
    private var mOtpStaticResponseData: VerifyOtpStaticResponseData? = null

    companion object {
        private const val TAG = "OtpVerificationFragment"
        fun newInstance(mobileNumber: String): OtpVerificationFragment {
            val fragment = OtpVerificationFragment()
            fragment.mMobileNumberStr = mobileNumber
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val client = SmsRetriever.getClient(mActivity)
        val task = client.startSmsRetriever()
        MySMSBroadcastReceiver.mSmsReceiverListener = this
        task.addOnSuccessListener {
            Log.d("OtpVerificationFragment", "onCreate: Auto read SMS retrieval task success")
        }
        task.addOnFailureListener {
            Log.d("OtpVerificationFragment", "onCreate: Auto read SMS retrieval task failed")
        }
        Log.d("OtpVerificationFragment", "App Signature is ${AppSignatureHelper(mActivity).appSignatures[0]}")
        mLoginService = LoginService()
        mLoginService.setLoginServiceInterface(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.otp_verification_fragment, container, false)
        mOtpVerificationService = OtpVerificationService()
        mOtpVerificationService.setOtpVerificationListener(this)
        mOtpStaticResponseData = mStaticData.mStaticData.mVerifyOtpStaticData
        return mContentView
    }

    private fun setupUIFromStaticResponse() {
        enterMobileNumberHeading?.text = mOtpStaticResponseData?.mHeadingText
        verifyTextView?.text = mOtpStaticResponseData?.mVerifyText
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            verifyTextView.id -> {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                Log.d(OtpVerificationFragment::class.simpleName, "onClick: clicked")
                mIsServerCallInitiated = true
                showProgressDialog(mActivity, mOtpStaticResponseData?.mVerifyingText)
                mOtpVerificationService.verifyOTP(mMobileNumberStr, mEnteredOtpStr.toInt())
            }
            resendOtpTextView.id -> {
                if (resendOtpTextView?.text == mOtpStaticResponseData?.mResendButtonText) {
                    startCountDownTimer()
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return
                    }
                    mLoginService.generateOTP(mMobileNumberStr)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUIFromStaticResponse()
        otpEditText?.setOtpFilledListener(this)
        startCountDownTimer()
    }

    private fun startCountDownTimer() {
        mCountDownTimer = object: CountDownTimer(Constants.RESEND_OTP_TIMER, Constants.TIMER_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    resendOtpText?.text = getString(R.string.otp_auto_resend_in)
                    resendOtpTextView?.text = "${(millisUntilFinished / 1000)} ${mOtpStaticResponseData?.mSecondText}"
                }
            }

            override fun onFinish() {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    resendOtpText?.text = ""
                    resendOtpTextView?.text = mOtpStaticResponseData?.mResendButtonText
                }
            }
        }
        mCountDownTimer.start()
    }

    override fun onStart() {
        super.onStart()
        otpEditText?.clearOTP()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDownTimer.cancel()
    }

    override fun onOTPFilledListener(otpStr: String) {
        mEnteredOtpStr = otpStr
        otpEditText.hideKeyboard()
        verifyTextView.isEnabled = true
        if (!mIsServerCallInitiated) verifyTextView.callOnClick()
    }

    override fun onOTPVerificationSuccessResponse(validateOtpResponse: ValidateOtpResponse) {
        mIsServerCallInitiated = false
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mCountDownTimer.cancel()
            if (!validateOtpResponse.mIsSuccessStatus) {
                stopProgress()
                otpEditText?.clearOTP()
                showShortSnackBar(validateOtpResponse.mMessage, true, R.drawable.ic_close_red)
            } else {
                mIsNewUser = validateOtpResponse.mIsNewUser
                stopProgress()
                showToast(validateOtpResponse.mMessage)
                saveUserDetailsInPref(validateOtpResponse)
                val cleverTapProfile = CleverTapProfile()
                cleverTapProfile.mShopName = validateOtpResponse.mStore?.storeInfo?.name
                var businessTypeStr = ""
                validateOtpResponse.mStore?.storeBusiness?.forEachIndexed { _, businessResponse -> businessTypeStr += "$businessResponse ," }
                cleverTapProfile.mShopCategory = businessTypeStr
                cleverTapProfile.mPhone = validateOtpResponse.mUserPhoneNumber
                cleverTapProfile.mIdentity = validateOtpResponse.mUserPhoneNumber
                cleverTapProfile.mLat = validateOtpResponse.mStore?.storeAddress?.latitude
                cleverTapProfile.mLong = validateOtpResponse.mStore?.storeAddress?.longitude
                cleverTapProfile.mAddress = validateOtpResponse.mStore?.storeAddress?.let {
                    "${it.address1}, ${it.googleAddress}, ${it.pinCode}"
                }
                AppsFlyerLib.getInstance().setCustomerUserId(validateOtpResponse.mUserPhoneNumber)
                AppEventsManager.pushCleverTapProfile(cleverTapProfile)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_OTP_VERIFIED,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                if (validateOtpResponse.mStore == null || mIsNewUser) launchFragment(OnBoardScreenDukaanNameFragment(), true) else launchFragment(HomeFragment(), true)
            }
        }
    }

    private fun saveUserDetailsInPref(validateOtpResponse: ValidateOtpResponse) {
        PrefsManager.storeStringDataInSharedPref(Constants.USER_AUTH_TOKEN, validateOtpResponse.mUserAuthToken)
        PrefsManager.storeStringDataInSharedPref(Constants.USER_MOBILE_NUMBER, validateOtpResponse.mUserPhoneNumber)
        PrefsManager.storeStringDataInSharedPref(Constants.USER_ID, validateOtpResponse.mUserId)
        validateOtpResponse.mStore?.run {
            PrefsManager.storeStringDataInSharedPref(Constants.STORE_ID, storeId.toString())
            Log.d("STORE_OBJECT_TEST", "$TAG saveUserDetailsInPref: STORE_ID :: ${PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)}")
            PrefsManager.storeStringDataInSharedPref(Constants.STORE_NAME, storeInfo.name)
            Log.d("STORE_OBJECT_TEST", "$TAG saveUserDetailsInPref: STORE_NAME :: ${PrefsManager.getStringDataFromSharedPref(Constants.STORE_NAME)}")
        }
    }

    override fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse) {
        mIsServerCallInitiated = false
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            otpEditText?.clearOTP()
            showShortSnackBar(validateOtpErrorResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onOTPVerificationDataException(e: Exception) {
        mIsServerCallInitiated = false
        exceptionHandlingForAPIResponse(e)
    }

    override fun onNewSmsReceived(sms: String?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            otpEditText?.setText(sms)
        }
    }

    override fun onGenerateOTPResponse(generateOtpResponse: GenerateOtpResponse) {
        stopProgress()
    }

    override fun onValidateUserResponse(validateUserResponse: CommonApiResponse) {
        Log.d(TAG, "onValidateUserResponse: do nothing")
    }

    override fun onGenerateOTPException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }
}