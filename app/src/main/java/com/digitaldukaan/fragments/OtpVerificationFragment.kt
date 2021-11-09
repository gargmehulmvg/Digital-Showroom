package com.digitaldukaan.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
    private var mTimerCompleted = false

    companion object {
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
            task?.addOnSuccessListener { Log.d(TAG, "onCreate: Auto read SMS retrieval task success") }
            task?.addOnFailureListener { Log.d(TAG, "onCreate: Auto read SMS retrieval task failed") }
        }
        Log.d(TAG, "App Signature is ${AppSignatureHelper(mActivity).appSignatures[0]}")
        mLoginService = LoginService()
        mLoginService?.setLoginServiceInterface(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "OtpVerificationFragment"
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
        mOtpStaticResponseData?.let { data ->
            val headingStr = "${data.heading_otp_sent_to}\n$mMobileNumberStr"
            enterMobileNumberHeading?.text = headingStr
            otpEditText?.hint = data.hint_enter_4_digit_otp
            changeTextView?.text = data.text_change_caps
            verifyTextView?.text = data.mVerifyText
            readMoreTextView?.setHtmlData(data.mReadMore)
            consentCheckBox?.text = data.mHeadingMessage
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            verifyTextViewContainer?.id -> {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                Log.d(OtpVerificationFragment::class.simpleName, "onClick: clicked")
                mIsServerCallInitiated = true
                try {
                    if (isEmpty(mEnteredOtpStr)) {
                        otpEditText?.apply {
                            text = null
                            requestFocus()
                            showKeyboard()
                            error = mOtpStaticResponseData?.error_mandatory_field
                        }
                        return
                    }
                    if (mActivity?.resources?.getInteger(R.integer.otp_length) != mEnteredOtpStr.length) {
                        otpEditText?.apply {
                            text = null
                            requestFocus()
                            showKeyboard()
                            error = mOtpStaticResponseData?.error_otp_not_valid
                        }
                        return
                    }
                    val otpInt: Int = if (isEmpty(mEnteredOtpStr)) 0 else mEnteredOtpStr.toInt()
                    showProgressDialog(mActivity, mOtpStaticResponseData?.mVerifyingText)
                    mOtpVerificationService?.verifyOTP(mMobileNumberStr, otpInt)
                    verifyTextView?.text = mOtpStaticResponseData?.mVerifyingText
                    verifyProgressBar?.apply {
                        visibility = View.VISIBLE
                        indeterminateDrawable?.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "onClick: ${e.message}", e)
                }
            }
            counterTextView?.id -> if (mTimerCompleted) {
                resendOtpText?.text = null
                startCountDownTimer()
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                mLoginService?.generateOTP(mMobileNumberStr)
            }
            changeTextView?.id -> mActivity?.onBackPressed()
            resendOtpText?.id -> if (mTimerCompleted) {
                resendOtpText?.text = null
                startCountDownTimer()
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                mLoginService?.generateOTP(mMobileNumberStr)
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
        startCountDownTimer()
        verifyProgressBar?.visibility = View.GONE
        otpEditText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: do nothing")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: do nothing")
            }

            override fun afterTextChanged(editable: Editable?) {
                mEnteredOtpStr = editable?.toString() ?: ""
                if (isNotEmpty(mEnteredOtpStr) && mActivity?.resources?.getInteger(R.integer.otp_length) == mEnteredOtpStr.length) {
                    onOTPFilledListener(mEnteredOtpStr)
                }
            }

        })
        if (isEmpty(mMobileNumberStr)) mActivity?.onBackPressed()
    }

    private fun startCountDownTimer() {
        mCountDownTimer = object: CountDownTimer(Constants.RESEND_OTP_TIMER, Constants.TIMER_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                mTimerCompleted = false
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    val seconds = (millisUntilFinished / 1000)
                    val secRemaining = "00:${if (isSingleDigitNumber(seconds)) "0$seconds" else "$seconds"}"
                    counterTextView?.text = secRemaining
                }
            }

            override fun onFinish() {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    mTimerCompleted = true
                    counterTextView?.text = mOtpStaticResponseData?.text_did_not_receive_otp
                    resendOtpText?.text = mOtpStaticResponseData?.text_send_again
                }
            }
        }
        mCountDownTimer?.start()
    }

    override fun onStart() {
        super.onStart()
        otpEditText?.text = null
    }

    override fun onDestroy() {
        super.onDestroy()
        mCountDownTimer?.cancel()
    }

    override fun onOTPFilledListener(otpStr: String) {
        mEnteredOtpStr = otpStr
        otpEditText?.hideKeyboard()
        verifyTextViewContainer?.callOnClick()
    }

    override fun onOTPVerificationSuccessResponse(validateOtpResponse: ValidateOtpResponse) {
        mIsServerCallInitiated = false
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mCountDownTimer?.cancel()
            if (!validateOtpResponse.mIsSuccessStatus) {
                stopProgress()
                otpEditText?.text = null
                verifyProgressBar?.visibility = View.GONE
                verifyTextView?.text = mOtpStaticResponseData?.mVerifyText
                showShortSnackBar(validateOtpResponse.mMessage, true, R.drawable.ic_close_red)
            } else {
                mIsNewUser = validateOtpResponse.mIsNewUser
                stopProgress()
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
                cleverTapProfile.mAddress = validateOtpResponse.mStore?.storeAddress?.let { address ->
                    "${address.address1}, ${address.googleAddress}, ${address.pinCode}"
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
                Handler(Looper.getMainLooper()).postDelayed({
                    if (null == validateOtpResponse.mStore && mIsNewUser) launchFragment(DukaanNameFragment.newInstance(), true) else launchFragment(OrderFragment.newInstance(), true)
                }, Constants.OTP_SUCCESS_TIMER)
                verifiedOtpGroup?.visibility = View.GONE
                verifiedTextViewContainer?.visibility = View.VISIBLE
                verifiedTextView?.text = mOtpStaticResponseData?.text_verified
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
            otpEditText?.text = null
            verifyTextView?.text = mOtpStaticResponseData?.mVerifyText
            verifyProgressBar?.visibility = View.GONE
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

    override fun onGenerateOTPResponse(generateOtpResponse: GenerateOtpResponse) = stopProgress()

    override fun onValidateUserResponse(validateUserResponse: CommonApiResponse) {
        Log.d(TAG, "onValidateUserResponse: do nothing")
    }

    override fun onGenerateOTPException(e: Exception) = exceptionHandlingForAPIResponse(e)
}