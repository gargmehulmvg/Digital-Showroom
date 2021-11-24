package com.digitaldukaan.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.appsflyer.AppsFlyerLib
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ResendOtpAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IAdapterItemClickListener
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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.otp_verification_fragment.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class OtpVerificationFragment : BaseFragment(), IOnOTPFilledListener, IOtpVerificationServiceInterface, ISmsReceivedListener,
    ILoginServiceInterface, IAdapterItemClickListener {

    private var mCountDownTimer: CountDownTimer? = null
    private var mOtpVerificationService: OtpVerificationService? = null
    private var mEnteredOtpStr = ""
    private var mMobileNumberStr = ""
    private var mLoginService: LoginService? = null
    private var mIsNewUser = false
    private var mIsConsentTakenFromUser = true
    private var mIsServerCallInitiated = false
    private var mOtpStaticResponseData: VerifyOtpStaticResponseData? = null
    private var mTimerCompleted = false
    private var mCheckStaffInviteResponse: StaffMemberDetailsResponse? = null
    private var mValidateOtpResponse: ValidateOtpResponse? = null
    private var mOtpModesList: ArrayList<CommonCtaResponse>? = null

    companion object {
        fun newInstance(mobileNumber: String): OtpVerificationFragment {
            val fragment = OtpVerificationFragment()
            fragment.mMobileNumberStr = mobileNumber
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mActivity?.let { context ->
            val client = SmsRetriever.getClient(context)
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
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
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
        mOtpVerificationService?.getOtpModesList()
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
            changeTextView?.id -> mActivity?.onBackPressed()
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
                    view?.let { v ->
                        val updateTextView: TextView = v.findViewById(R.id.updateTextView)
                        val messageTextView: TextView = v.findViewById(R.id.messageTextView)
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
        counterTextView.visibility = View.VISIBLE
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
                    resendOtpText?.text = mOtpStaticResponseData?.text_send_again
                    counterTextView?.visibility = View.INVISIBLE
                    if (View.GONE == resendOtpContainer?.visibility) {
                        resendTextView?.text = mOtpStaticResponseData?.text_resend_otp
                        resendOtpContainer?.visibility = View.VISIBLE
                        recyclerView?.apply {
                            layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                            val modeTempList: ArrayList<CommonCtaResponse> = ArrayList()
                            mOtpModesList?.forEachIndexed { _, ctaResponse ->
                                if (ctaResponse.isEnabled) modeTempList.add(ctaResponse)
                            }
                            mOtpModesList = ArrayList()
                            mOtpModesList?.addAll(modeTempList)
                            adapter = ResendOtpAdapter(mActivity, mOtpModesList, this@OtpVerificationFragment)
                        }
                    } else if (View.VISIBLE == resendOtpContainer?.visibility) {
                        resendOtpContainer?.alpha = 1f
                    }
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

    override fun onOTPVerificationSuccessResponse(commonApiResponse: CommonApiResponse) {
        mIsServerCallInitiated = false
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mCountDownTimer?.cancel()
            mValidateOtpResponse = Gson().fromJson<ValidateOtpResponse>(commonApiResponse.mCommonDataStr, ValidateOtpResponse::class.java)
            if (!commonApiResponse.mIsSuccessStatus) {
                stopProgress()
                otpEditText?.text = null
                verifyProgressBar?.visibility = View.GONE
                verifyTextView?.text = mOtpStaticResponseData?.mVerifyText
                showShortSnackBar(mValidateOtpResponse?.mMessage, true, R.drawable.ic_close_red)
            } else {
                saveUserDetailsInPref(mValidateOtpResponse)
                StaticInstances.sPermissionHashMap = null
                StaticInstances.sPermissionHashMap = mValidateOtpResponse?.mPermissionMap
                mOtpVerificationService?.checkStaffInvite()
                mIsNewUser = mValidateOtpResponse?.mIsNewUser ?: false
                stopProgress()
                val cleverTapProfile = CleverTapProfile()
                cleverTapProfile.mShopName = mValidateOtpResponse?.mStore?.storeInfo?.name
                var businessTypeStr = ""
                mValidateOtpResponse?.mStore?.storeBusiness?.forEachIndexed { _, businessResponse -> businessTypeStr += "$businessResponse ," }
                cleverTapProfile.mShopCategory = businessTypeStr
                cleverTapProfile.mPhone = mValidateOtpResponse?.mUserPhoneNumber
                cleverTapProfile.mIdentity = mValidateOtpResponse?.mUserPhoneNumber
                cleverTapProfile.mLat = mValidateOtpResponse?.mStore?.storeAddress?.latitude
                cleverTapProfile.mLong = mValidateOtpResponse?.mStore?.storeAddress?.longitude
                cleverTapProfile.mAddress = mValidateOtpResponse?.mStore?.storeAddress?.let { address ->
                    "${address.address1}, ${address.googleAddress}, ${address.pinCode}"
                }
                AppsFlyerLib.getInstance()?.setCustomerUserId(mValidateOtpResponse?.mUserPhoneNumber)
                AppEventsManager.pushCleverTapProfile(cleverTapProfile)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_OTP_VERIFIED,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PHONE to mMobileNumberStr,
                        AFInAppEventParameterName.IS_CONSENT to if (mIsConsentTakenFromUser) "1" else "0")
                )
                verifiedOtpGroup?.visibility = View.GONE
                verifiedTextViewContainer?.visibility = View.VISIBLE
                verifiedTextView?.text = mOtpStaticResponseData?.text_verified
            }
        }
    }

    override fun checkStaffInviteResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                mCheckStaffInviteResponse = Gson().fromJson<StaffMemberDetailsResponse>(commonResponse.mCommonDataStr, StaffMemberDetailsResponse::class.java)
                if (null == mValidateOtpResponse?.mStore && mIsNewUser) {
                    launchFragment(DukaanNameFragment.newInstance(mCheckStaffInviteResponse?.mIsInvitationAvailable ?: false, mCheckStaffInviteResponse?.mStaffInvitation, mValidateOtpResponse?.mUserId ?: ""), true)
                } else StaticInstances.sPermissionHashMap?.let { it1 -> launchScreenFromPermissionMap(it1) }
            }
        }
    }

    private fun saveUserDetailsInPref(validateOtpResponse: ValidateOtpResponse?) {
        PrefsManager.storeStringDataInSharedPref(Constants.USER_AUTH_TOKEN, validateOtpResponse?.mUserAuthToken)
        PrefsManager.storeStringDataInSharedPref(Constants.USER_MOBILE_NUMBER, validateOtpResponse?.mUserPhoneNumber)
        PrefsManager.storeStringDataInSharedPref(Constants.USER_ID, validateOtpResponse?.mUserId)
        validateOtpResponse?.mStore?.let { store ->
            PrefsManager.storeStringDataInSharedPref(Constants.STORE_ID, store.storeId.toString())
            PrefsManager.storeStringDataInSharedPref(Constants.STORE_NAME, store.storeInfo.name)
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

    override fun onGetOtpModesListResponse(commonApiResponse: CommonApiResponse) {
        if (commonApiResponse.mIsSuccessStatus) {
            val listType = object : TypeToken<List<CommonCtaResponse>>() {}.type
            mOtpModesList = Gson().fromJson<ArrayList<CommonCtaResponse>>(commonApiResponse.mCommonDataStr, listType)
        }
    }

    override fun onOTPVerificationDataException(e: Exception) {
        mIsServerCallInitiated = false
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            otpEditText?.text = null
            verifyTextView?.text = mOtpStaticResponseData?.mVerifyText
            verifyProgressBar?.visibility = View.GONE
            showShortSnackBar(mActivity?.getString(R.string.something_went_wrong), true, R.drawable.ic_close_red)
        }
        exceptionHandlingForAPIResponse(e)
    }

    override fun onGetOtpModesListDataException(e: Exception) {
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

    override fun onAdapterItemClickListener(position: Int) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (1f != resendOtpContainer?.alpha && !mTimerCompleted) return@runTaskOnCoroutineMain
            otpSentOnContainer?.apply {
                visibility = View.VISIBLE
                alpha = 1f
            }
            val modeListItem = mOtpModesList?.get(position)
            val otpSentOnStr = "${mOtpStaticResponseData?.heading_otp_sent_to} ${modeListItem?.text}"
            otpSentOnTextView?.text = otpSentOnStr
            resendOtpContainer?.alpha = 0.3f
            Handler(Looper.getMainLooper()).postDelayed({
                otpSentOnContainer.visibility = View.GONE
            }, Constants.STORE_CREATION_PROGRESS_ANIMATION_INTERVAL)
            startCountDownTimer()
            mLoginService?.generateOTP(mMobileNumberStr, modeListItem?.id ?: 0)
        }
    }
}