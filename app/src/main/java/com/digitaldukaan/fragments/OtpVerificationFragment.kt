package com.digitaldukaan.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener


class OtpVerificationFragment : BaseFragment(), IOnOTPFilledListener, IOtpVerificationServiceInterface, ISmsReceivedListener,
    ILoginServiceInterface {

    private var mCountDownTimer: CountDownTimer? = null
    private var mOtpVerificationService: OtpVerificationService? = null
    private var mEnteredOtpStr = ""
    private var mMobileNumberStr = ""
    private var mLoginService: LoginService? = null
    private var mIsNewUser: Boolean = false
    private var mIsConsentTakenFromUser = true
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
        mActivity?.run {
            val client = SmsRetriever.getClient(this)
            val task = client.startSmsRetriever()
            MySMSBroadcastReceiver.mSmsReceiverListener = this@OtpVerificationFragment
            task?.addOnSuccessListener {
                Log.d("OtpVerificationFragment", "onCreate: Auto read SMS retrieval task success")
            }
            task?.addOnFailureListener {
                Log.d("OtpVerificationFragment", "onCreate: Auto read SMS retrieval task failed")
            }
        }
        Log.d("OtpVerificationFragment", "App Signature is ${AppSignatureHelper(mActivity).appSignatures[0]}")
        mLoginService = LoginService()
        mLoginService?.setLoginServiceInterface(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.otp_verification_fragment, container, false)
        mOtpVerificationService = OtpVerificationService()
        mOtpVerificationService?.setOtpVerificationListener(this)
        mOtpStaticResponseData = StaticInstances.sStaticData?.mVerifyOtpStaticData
        mActivity?.let {
            KeyboardVisibilityEvent.setEventListener(it, object : KeyboardVisibilityEventListener {
                    override fun onVisibilityChanged(isOpen: Boolean) {
                        if (isOpen) {
                            consentCheckBox?.visibility = View.GONE
                            readMoreTextView?.visibility = View.GONE
                        } else {
                            consentCheckBox?.visibility = View.VISIBLE
                            readMoreTextView?.visibility = View.VISIBLE
                        }
                    }
                })
        }
        consentCheckBox?.setOnCheckedChangeListener { _, isChecked -> mIsConsentTakenFromUser = isChecked }
        return mContentView
    }

    private fun setupUIFromStaticResponse() {
        enterMobileNumberHeading?.text = mOtpStaticResponseData?.mHeadingText
        verifyTextView?.text = mOtpStaticResponseData?.mVerifyText
        readMoreTextView?.setHtmlData(mOtpStaticResponseData?.mReadMore)
        consentCheckBox?.text = mOtpStaticResponseData?.mHeadingMessage
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            verifyTextView?.id -> {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                Log.d(OtpVerificationFragment::class.simpleName, "onClick: clicked")
                mIsServerCallInitiated = true
                showProgressDialog(mActivity, mOtpStaticResponseData?.mVerifyingText)
                val otpInt: Int
                try {
                    otpInt = mEnteredOtpStr.toInt()
                    mOtpVerificationService?.verifyOTP(mMobileNumberStr, otpInt)
                } catch (e: Exception) {
                    Log.e(TAG, "onClick: ${e.message}", e)
                }
            }
            resendOtpTextView.id -> {
                if (resendOtpTextView?.text == mOtpStaticResponseData?.mResendButtonText) {
                    startCountDownTimer()
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return
                    }
                    mLoginService?.generateOTP(mMobileNumberStr)
                }
            }
            readMoreTextView?.id -> showConsentDialog()
        }
    }

    private fun showConsentDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.let {
                val dialog = Dialog(it)
                val view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_user_consent, null)
                dialog.apply {
                    setContentView(view)
                    setCancelable(true)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    view?.run {
                        val updateTextView: TextView = findViewById(R.id.updateTextView)
                        val messageTextView: TextView = findViewById(R.id.messageTextView)
                        messageTextView.text = mOtpStaticResponseData?.mConsentMessage
                        updateTextView.text = mOtpStaticResponseData?.mTextOk
                        updateTextView.setOnClickListener { (this@apply).dismiss() }
                    }
                }.show()
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
                    val secRemaining = "${(millisUntilFinished / 1000)} ${mOtpStaticResponseData?.mSecondText}"
                    resendOtpTextView?.text = secRemaining
                }
            }

            override fun onFinish() {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    resendOtpText?.text = ""
                    resendOtpTextView?.text = mOtpStaticResponseData?.mResendButtonText
                }
            }
        }
        mCountDownTimer?.start()
    }

    override fun onStart() {
        super.onStart()
        otpEditText?.clearOTP()
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDownTimer?.cancel()
    }

    override fun onOTPFilledListener(otpStr: String) {
        mEnteredOtpStr = otpStr
        otpEditText?.hideKeyboard()
        verifyTextView?.isEnabled = true
        if (!mIsServerCallInitiated) verifyTextView?.callOnClick()
    }

    override fun onOTPVerificationSuccessResponse(validateOtpResponse: ValidateOtpResponse) {
        mIsServerCallInitiated = false
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mCountDownTimer?.cancel()
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
                AppsFlyerLib.getInstance()?.setCustomerUserId(validateOtpResponse.mUserPhoneNumber)
                AppEventsManager.pushCleverTapProfile(cleverTapProfile)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_OTP_VERIFIED,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PHONE to mMobileNumberStr,
                        AFInAppEventParameterName.IS_CONSENT to if (mIsConsentTakenFromUser) "1" else "0")
                )
                if (null == validateOtpResponse.mStore && mIsNewUser) launchFragment(OnBoardScreenDukaanNameFragment(), true) else launchFragment(HomeFragment(), true)
            }
        }
    }

    private fun saveUserDetailsInPref(validateOtpResponse: ValidateOtpResponse) {
        PrefsManager.storeStringDataInSharedPref(Constants.USER_AUTH_TOKEN, validateOtpResponse.mUserAuthToken)
        PrefsManager.storeStringDataInSharedPref(Constants.USER_MOBILE_NUMBER, validateOtpResponse.mUserPhoneNumber)
        PrefsManager.storeStringDataInSharedPref(Constants.USER_ID, validateOtpResponse.mUserId)
        validateOtpResponse.mStore?.run {
            PrefsManager.storeStringDataInSharedPref(Constants.STORE_ID, storeId.toString())
            PrefsManager.storeStringDataInSharedPref(Constants.STORE_NAME, storeInfo.name)
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