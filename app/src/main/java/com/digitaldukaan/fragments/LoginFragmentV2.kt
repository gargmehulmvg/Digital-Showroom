package com.digitaldukaan.fragments

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.LoginHelpPageAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.dto.CleverTapProfile
import com.digitaldukaan.models.request.ValidateUserRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.GenerateOtpResponse
import com.digitaldukaan.models.response.ValidateUserResponse
import com.digitaldukaan.services.LoginService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ILoginServiceInterface
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsApi
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.truecaller.android.sdk.*
import kotlinx.android.synthetic.main.layout_login_fragment_v2.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import java.util.*

class LoginFragmentV2 : BaseFragment(), ILoginServiceInterface {

    private var mIsDoublePressToExit = false
    private var mIsMobileNumberSearchingDone = false
    private var mViewPagerTimer: Timer? = null
    private var mLoginService: LoginService? = null
    private var mTrueCallerInstance: TruecallerSDK? = null

    companion object {
        private const val TRUE_CALLER_ERROR_USE_ANOTHER_NUMBER = 14
        private var mMobileNumber = ""

        fun newInstance(isLogoutDone: Boolean = false): LoginFragmentV2 {
            if (isLogoutDone) mMobileNumber = ""
            return LoginFragmentV2()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "LoginFragmentV2"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_login_fragment_v2, container, false)
        mLoginService = LoginService()
        mLoginService?.setLoginServiceInterface(this)
        hideBottomNavigationView(true)
        initializeTrueCaller()
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        mActivity?.let { context ->
            KeyboardVisibilityEvent.setEventListener(context, object : KeyboardVisibilityEventListener {

                override fun onVisibilityChanged(isOpen: Boolean) {
                    trueCallerContainer?.visibility = if (isOpen) View.GONE else View.VISIBLE
                    if (!isOpen) checkTrueCallerInstalledOnDevice()
                    nestedScrollView?.scrollTo(0, nestedScrollView?.bottom ?: 0)
                }

            })
        }
        setupViewPager()
        mobileNumberEditText?.apply {
            addTextChangedListener(object : TextWatcher {

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                override fun afterTextChanged(editable: Editable?) {
                    mMobileNumber = editable?.toString() ?: ""
                    otpTextView?.isEnabled = (mActivity?.resources?.getInteger(R.integer.mobile_number_length) == mMobileNumber.length)
                }

            })
        }
        Log.d(TAG, "onViewCreated: mMobileNumber :: $mMobileNumber")
        if (isNotEmpty(mMobileNumber)) {
            mobileNumberTextView?.visibility = View.GONE
            mobileNumberEditText?.apply {
                visibility = View.VISIBLE
                setText(mMobileNumber)
                setOnEditorActionListener { _, actionId, _ ->
                    if (EditorInfo.IME_ACTION_DONE == actionId) otpTextView?.callOnClick()
                    true
                }
            }
        }
        setStaticText()
        checkTrueCallerInstalledOnDevice()
    }

    private fun checkTrueCallerInstalledOnDevice() {
        trueCallerContainer?.visibility = if (true == mTrueCallerInstance?.isUsable) View.VISIBLE else View.GONE
    }

    private fun setStaticText() {
        StaticInstances.sStaticData?.mLoginStaticText?.let { data ->
            mobileNumberTextView?.text = data.hint_enter_whatsapp_number
            otpTextView?.text = data.text_get_otp
            orTextView?.text = data.text_or
            trueCallerTextView?.text = data.text_login_with_truecaller
            mobileNumberEditText?.setHint(data.hint_enter_whatsapp_number)
        }
    }

    private fun setupViewPager() {
        val pagerAdapter = LoginHelpPageAdapter(mActivity)
        val viewPager: ViewPager? = mContentView?.findViewById(R.id.viewpager)
        val indicator: WormDotsIndicator? = mContentView?.findViewById(R.id.indicator)
        viewPager?.adapter = pagerAdapter
        viewPager?.let { indicator?.setViewPager(it) }
        val timerTask = object : TimerTask() {
            override fun run() {
                viewPager?.post {
                    viewPager.currentItem = ((viewPager.currentItem + 1) % (if (isEmpty(StaticInstances.sHelpScreenList)) 1 else StaticInstances.sHelpScreenList.size))
                }
            }
        }
        mViewPagerTimer = Timer()
        mViewPagerTimer?.schedule(timerTask, 3000,3000)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            trueCallerTextView?.id -> if (true == mTrueCallerInstance?.isUsable) mTrueCallerInstance?.getUserProfile(this)
            otpTextView?.id -> {
                mobileNumberEditText?.apply {
                    hideSoftKeyboard()
                    clearFocus()
                }
                val mobileNumber = mobileNumberEditText?.text?.trim().toString()
                val validationFailed = isMobileNumberValidationNotCorrect(mobileNumber)
                performOTPServerCall(validationFailed, mobileNumber)
            }
            mobileNumberTextView?.id -> {
                mobileNumberTextView?.visibility = View.GONE
                mobileNumberEditText?.apply {
                    text = null
                    visibility = View.VISIBLE
                    setOnEditorActionListener { _, actionId, _ ->
                        if (EditorInfo.IME_ACTION_DONE == actionId) otpTextView?.callOnClick()
                        true
                    }
                }
                initiateAutoDetectMobileNumber()
            }
        }
    }

    private fun isMobileNumberValidationNotCorrect(mobileNumber: String): Boolean = when {
        isEmpty(mobileNumber) -> {
            mobileNumberEditText?.error = StaticInstances.sStaticData?.mLoginStaticText?.error_mandatory_field
            true
        }
        resources.getInteger(R.integer.mobile_number_length) != mobileNumber.length -> {
            mobileNumberEditText?.error = StaticInstances.sStaticData?.mLoginStaticText?.error_mobile_number_not_valid
            true
        }
        else -> false
    }

    override fun onBackPressed(): Boolean {
        if (mIsDoublePressToExit) mActivity?.finish()
        showShortSnackBar(getString(R.string.msg_back_press))
        mIsDoublePressToExit = true
        Handler(Looper.getMainLooper()).postDelayed(
            { mIsDoublePressToExit = false },
            Constants.BACK_PRESS_INTERVAL
        )
        return true
    }

    private fun initializeTrueCaller() {
        try {
            mActivity?.let { context ->
                val trueScope = TruecallerSdkScope.Builder(context, sdkCallback)
                    .consentMode(TruecallerSdkScope.CONSENT_MODE_BOTTOMSHEET)
                    .loginTextPrefix(TruecallerSdkScope.LOGIN_TEXT_PREFIX_TO_GET_STARTED)
                    .loginTextSuffix(TruecallerSdkScope.LOGIN_TEXT_SUFFIX_PLEASE_VERIFY_MOBILE_NO)
                    .buttonColor(ContextCompat.getColor(context, R.color.black))
                    .buttonTextColor(ContextCompat.getColor(context, R.color.white))
                    .consentTitleOption(TruecallerSdkScope.SDK_CONSENT_TITLE_VERIFY)
                    .footerType(TruecallerSdkScope.FOOTER_TYPE_CONTINUE)
                    .sdkOptions(TruecallerSdkScope.SDK_OPTION_WITHOUT_OTP)
                    .buttonShapeOptions(TruecallerSdkScope.BUTTON_SHAPE_ROUNDED)
                    .build()
                TruecallerSDK.init(trueScope)
            }
        } catch (e: Exception) {
            Log.e(TAG, "initializeTrueCaller: ${e.message}", e)
        }
        mTrueCallerInstance = TruecallerSDK.getInstance()
    }

    private val sdkCallback = object : ITrueCallback {

        override fun onFailureProfileShared(trueError: TrueError) {
            Log.d(TAG, "onFailureProfileShared: $trueError")
            if (TRUE_CALLER_ERROR_USE_ANOTHER_NUMBER == trueError.errorType) mobileNumberTextView?.callOnClick()
        }

        override fun onSuccessProfileShared(response: TrueProfile) {
            Log.d(TAG, "onSuccessProfileShared: $response")
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_TRUE_CALLER_VERIFIED,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
            )
            val request = ValidateUserRequest(
                response.payload,
                response.signature,
                StaticInstances.sCleverTapId,
                StaticInstances.sFireBaseMessagingToken,
                StaticInstances.sFireBaseAppInstanceId,
                if (response.phoneNumber.length >= (mActivity?.resources?.getInteger(R.integer.mobile_number_length) ?: 10)) response.phoneNumber.substring(response.phoneNumber.length - (mActivity?.resources?.getInteger(R.integer.mobile_number_length) ?: 10)) else ""
            )
            mLoginService?.validateUser(request)
        }

        override fun onVerificationRequired(error: TrueError?) {
            Log.d(TAG, "onVerificationRequired: $error")
        }
    }

    private fun initiateAutoDetectMobileNumber() {
        mActivity?.let {
            try {
                mIsMobileNumberSearchingDone = true
                val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()
                val intent: PendingIntent = Credentials.getClient(it).getHintPickerIntent(hintRequest)
                val intentSenderRequest = IntentSenderRequest.Builder(intent.intentSender).build()
                phonePickIntentResultLauncher.launch(intentSenderRequest)
            } catch (e: Exception) {
                Log.e(TAG, "initiateAutoDetectMobileNumber: ", e)
            }
        }
    }

    private val phonePickIntentResultLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val credentials: Credential? = result.data?.getParcelableExtra(Credential.EXTRA_KEY)
                credentials?.let { credential ->
                    CoroutineScopeUtils().runTaskOnCoroutineMain {
                        try {
                            mobileNumberEditText?.apply {
                                mMobileNumber = credential.id.substring(3)
                                setText(mMobileNumber)
                                setSelection(mobileNumberEditText?.text?.trim()?.length ?: 0)
                            }
                            otpTextView?.callOnClick()
                        } catch (e: Exception) {
                            Log.e(TAG, "onActivityResult: ${e.message}", e)
                        }
                    }
                }
            }
        }

    override fun onDestroy() {
        if (false == mActivity?.isDestroyed) mViewPagerTimer?.cancel()
        super.onDestroy()
    }

    private fun performOTPServerCall(validationFailed: Boolean, mobileNumber: String) {
        if (validationFailed) {
            mobileNumberEditText?.requestFocus()
        } else {
            if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
                return
            }
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_GET_OTP,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(AFInAppEventParameterName.PHONE to mobileNumber, AFInAppEventParameterName.IS_MERCHANT to "1")
            )
            showCancellableProgressDialog(mActivity)
            mActivity?.let { context -> UIUtil.hideKeyboard(context) }
            mLoginService?.generateOTP(mobileNumber, 0)
        }
    }

    override fun onGenerateOTPResponse(generateOtpResponse: GenerateOtpResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (generateOtpResponse.mStatus) {
                val mobileNumber = mobileNumberEditText?.text?.trim().toString()
                launchFragment(OtpVerificationFragment.newInstance(mobileNumber), true)
            } else showToast(generateOtpResponse.mMessage)
        }
    }

    override fun onValidateUserResponse(validateUserResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (validateUserResponse.mIsSuccessStatus) {
                showShortSnackBar(validateUserResponse.mMessage, true, R.drawable.ic_check_circle)
                val userResponse = Gson().fromJson<ValidateUserResponse>(validateUserResponse.mCommonDataStr, ValidateUserResponse::class.java)
                saveUserDetailsInPref(userResponse)
                val cleverTapProfile = CleverTapProfile()
                cleverTapProfile.mShopName = userResponse?.store?.storeInfo?.name
                var businessTypeStr = ""
                userResponse?.store?.storeBusiness?.forEachIndexed { _, businessResponse -> businessTypeStr += "$businessResponse ," }
                cleverTapProfile.mShopCategory = businessTypeStr
                cleverTapProfile.mPhone = userResponse?.user?.phone
                cleverTapProfile.mIdentity = userResponse?.user?.phone
                cleverTapProfile.mLat = userResponse?.store?.storeAddress?.latitude
                cleverTapProfile.mLong = userResponse?.store?.storeAddress?.longitude
                cleverTapProfile.mAddress = userResponse?.store?.storeAddress?.let {
                    "${it.address1}, ${it.googleAddress}, ${it.pinCode}"
                }
                AppEventsManager.pushCleverTapProfile(cleverTapProfile)
                sIsInvitationAvailable = userResponse?.mIsInvitationShown ?: false
                StaticInstances.sStaffInvitation = userResponse?.mStaffInvitation
                if (null == userResponse.store && userResponse.user.isNewUser) launchFragment(DukaanNameFragment.newInstance(userResponse?.user?.userId ?: ""), true) else launchFragment(OrderFragment.newInstance(), true)
            } else showShortSnackBar(validateUserResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    private fun saveUserDetailsInPref(validateOtpResponse: ValidateUserResponse) {
        PrefsManager.storeStringDataInSharedPref(Constants.USER_AUTH_TOKEN, validateOtpResponse.user.authToken)
        PrefsManager.storeStringDataInSharedPref(Constants.USER_ID, validateOtpResponse.user.userId)
        PrefsManager.storeStringDataInSharedPref(Constants.USER_MOBILE_NUMBER, validateOtpResponse.user.phone)
        validateOtpResponse.store?.let { store ->
            PrefsManager.storeStringDataInSharedPref(Constants.STORE_ID, "${store.storeId}")
            PrefsManager.storeStringDataInSharedPref(Constants.STORE_NAME, store.storeInfo.name)
        }
    }

    override fun onGenerateOTPException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (Constants.CREDENTIAL_PICKER_REQUEST == requestCode && CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE == resultCode) {
                showToast("No phone numbers found")
            } else if (TruecallerSDK.SHARE_PROFILE_REQUEST_CODE == requestCode) {
                mActivity?.let { context ->
                    mIsMobileNumberSearchingDone = true
                    mTrueCallerInstance?.onActivityResultObtained(context, requestCode, resultCode, data)
                }
            } else if (!mIsMobileNumberSearchingDone) initiateAutoDetectMobileNumber()
        } catch (e: Exception) {
            Log.e(TAG, "onActivityResult: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), "exception" to e.toString(), "exception point" to "onActivityResult")
            )
        }
    }
}