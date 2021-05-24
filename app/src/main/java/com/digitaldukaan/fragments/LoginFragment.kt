package com.digitaldukaan.fragments

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.content.ContextCompat
import androidx.transition.TransitionInflater
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.constants.Constants.Companion.CREDENTIAL_PICKER_REQUEST
import com.digitaldukaan.models.dto.CleverTapProfile
import com.digitaldukaan.models.request.ValidateUserRequest
import com.digitaldukaan.models.response.AuthNewResponseData
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
import com.google.gson.Gson
import com.truecaller.android.sdk.*
import kotlinx.android.synthetic.main.layout_login_fragment.*

class LoginFragment : BaseFragment(), ILoginServiceInterface {

    private var mIsDoublePressToExit = false
    private var mIsMobileNumberSearchingDone = false
    private lateinit var mLoginService: LoginService
    private var mAuthStaticData: AuthNewResponseData? = null

    companion object {
        private const val TAG = "LoginFragment"
        private const val USE_ANOTHER_NUMBER = 14
        private var mMobileNumber = ""
        fun newInstance(): LoginFragment{
            return LoginFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        mLoginService = LoginService()
        mLoginService.setLoginServiceInterface(this)
        initializeTrueCaller()
        if (TruecallerSDK.getInstance().isUsable) TruecallerSDK.getInstance().getUserProfile(this) else initiateAutoDetectMobileNumber()
    }

    private fun initializeTrueCaller() {
        try {
            mActivity?.let {
                val trueScope = TruecallerSdkScope.Builder(it, sdkCallback)
                    .consentMode(TruecallerSdkScope.CONSENT_MODE_BOTTOMSHEET)
                    .loginTextPrefix(TruecallerSdkScope.LOGIN_TEXT_PREFIX_TO_GET_STARTED)
                    .loginTextSuffix(TruecallerSdkScope.LOGIN_TEXT_SUFFIX_PLEASE_VERIFY_MOBILE_NO)
                    .buttonColor(ContextCompat.getColor(it, R.color.black))
                    .buttonTextColor(ContextCompat.getColor(it, R.color.white))
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
    }

    private val sdkCallback = object : ITrueCallback {

        override fun onFailureProfileShared(p0: TrueError) {
            Log.d(TAG, "onFailureProfileShared: $p0")
            when (p0.errorType) {
                USE_ANOTHER_NUMBER -> mobileNumberTextView?.callOnClick()
            }
        }

        override fun onSuccessProfileShared(response: TrueProfile) {
            Log.d(TAG, "onSuccessProfileShared: $response")
            val request = ValidateUserRequest(
                response.payload,
                response.signature,
                StaticInstances.sCleverTapId,
                StaticInstances.sFireBaseMessagingToken,
                if (response.phoneNumber.length >= 10) response.phoneNumber.substring(response.phoneNumber.length - 10) else ""
            )
            mLoginService.validateUser(request)
        }

        override fun onVerificationRequired(p0: TrueError?) {
            Log.d(TAG, "onVerificationRequired: $p0")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_login_fragment, container, false)
        mAuthStaticData = StaticInstances.mStaticData?.mAuthNew
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.apply {
            setHeaderTitle("")
            hideToolBar(mActivity, true)
        }
        setupDataFromStaticResponse()
        setupMobileNumberEditText()
        if (mMobileNumber.isNotEmpty()) {
            mobileNumberInputLayout?.visibility = View.VISIBLE
            mobileNumberTextView?.visibility = View.GONE
        }
        hideBottomNavigationView(true)
    }

    private fun setupDataFromStaticResponse() {
        enterMobileNumberHeading?.text = mAuthStaticData?.mHeadingText
        send4DigitOtpHeading?.text = mAuthStaticData?.mSubHeadingText
        mobileNumberInputLayout?.hint = mAuthStaticData?.mInputText
        mobileNumberTextView?.text = mAuthStaticData?.mInputText
        getOtpTextView?.text = mAuthStaticData?.mButtonText
    }

    private fun setupMobileNumberEditText() {
        mobileNumberEditText?.apply {
            requestFocus()
            showKeyboard()
        }
        mobileNumberEditText?.setOnEditorActionListener { _, actionId, _ ->
            if (EditorInfo.IME_ACTION_DONE == actionId) getOtpTextView?.callOnClick()
            true
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            mobileNumberTextView?.id -> {
                mobileNumberTextView?.visibility = View.GONE
                mobileNumberInputLayout?.visibility = View.VISIBLE
                initiateAutoDetectMobileNumber()
            }
            getOtpTextView?.id -> {
                if (mobileNumberInputLayout?.visibility == View.GONE) return
                val mobileNumber = mobileNumberEditText?.text?.trim().toString()
                val validationFailed = isMobileNumberValidationNotCorrect(mobileNumber)
                performOTPServerCall(validationFailed, mobileNumber)
            }
        }
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
            showProgressDialog(mActivity)
            mobileNumberEditText?.hideKeyboard()
            mLoginService.generateOTP(mobileNumber)
        }
    }

    private fun isMobileNumberValidationNotCorrect(mobileNumber: String): Boolean {
        return when {
            mobileNumber.isEmpty() -> {
                mobileNumberEditText?.error = getString(R.string.mandatory_field_message)
                true
            }
            resources.getInteger(R.integer.mobile_number_length) != mobileNumber.length -> {
                mobileNumberEditText?.error = getString(R.string.mobile_number_length_validation_message)
                true
            }
            else -> false
        }
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

    private fun initiateAutoDetectMobileNumber() {
        mActivity?.let {
            try {
                mIsMobileNumberSearchingDone = true
                val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()
                val intent: PendingIntent = Credentials.getClient(it).getHintPickerIntent(hintRequest)
                startIntentSenderForResult(intent.intentSender, CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0, Bundle())
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        try {
            if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
                val credentials: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
                credentials?.let {
                    CoroutineScopeUtils().runTaskOnCoroutineMain {
                        try {
                            mobileNumberEditText?.apply {
                                text = null
                                mMobileNumber = it.id.substring(3)
                                setText(mMobileNumber)
                                setSelection(mobileNumberEditText?.text?.trim()?.length ?: 0)
                            }
                            getOtpTextView?.callOnClick()
                        } catch (e: Exception) {
                            Log.e(TAG, "onActivityResult: ${e.message}", e)
                        }
                    }
                }
            } else if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE) {
                showToast("No phone numbers found")
            } else if (requestCode == TruecallerSDK.SHARE_PROFILE_REQUEST_CODE) {
                mActivity?.let { TruecallerSDK.getInstance().onActivityResultObtained(it, requestCode, resultCode, data) }
                mIsMobileNumberSearchingDone = true
            } else {
                if (!mIsMobileNumberSearchingDone) initiateAutoDetectMobileNumber()
            }
        } catch (e: Exception) {
            Log.e(TAG, "onActivityResult: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "exception" to e.toString(),
                    "exception point" to "onActivityResult"
                )
            )
        }
    }

    override fun onGenerateOTPResponse(generateOtpResponse: GenerateOtpResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (generateOtpResponse.mStatus) {
                val mobileNumber = mobileNumberEditText?.text?.trim().toString()
                launchFragment(OtpVerificationFragment.newInstance(mobileNumber), true)
            } else {
                showToast(generateOtpResponse.mMessage)
            }
        }
    }

    override fun onValidateUserResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                val validateUserResponse = Gson().fromJson<ValidateUserResponse>(response.mCommonDataStr, ValidateUserResponse::class.java)
                saveUserDetailsInPref(validateUserResponse)
                val cleverTapProfile = CleverTapProfile()
                cleverTapProfile.mShopName = validateUserResponse?.store?.storeInfo?.name
                var businessTypeStr = ""
                validateUserResponse?.store?.storeBusiness?.forEachIndexed { _, businessResponse -> businessTypeStr += "$businessResponse ," }
                cleverTapProfile.mShopCategory = businessTypeStr
                cleverTapProfile.mPhone = validateUserResponse?.user?.phone
                cleverTapProfile.mIdentity = validateUserResponse?.user?.phone
                cleverTapProfile.mLat = validateUserResponse?.store?.storeAddress?.latitude
                cleverTapProfile.mLong = validateUserResponse?.store?.storeAddress?.longitude
                cleverTapProfile.mAddress = validateUserResponse?.store?.storeAddress?.let {
                    "${it.address1}, ${it.googleAddress}, ${it.pinCode}"
                }
                AppEventsManager.pushCleverTapProfile(cleverTapProfile)
                if (validateUserResponse.store == null || validateUserResponse.user.isNewUser) launchFragment(OnBoardScreenDukaanNameFragment(), true) else launchFragment(HomeFragment(), true)
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    private fun saveUserDetailsInPref(validateOtpResponse: ValidateUserResponse) {
        PrefsManager.storeStringDataInSharedPref(Constants.USER_AUTH_TOKEN, validateOtpResponse.user.authToken)
        PrefsManager.storeStringDataInSharedPref(Constants.USER_ID, validateOtpResponse.user.userId)
        PrefsManager.storeStringDataInSharedPref(Constants.USER_MOBILE_NUMBER, validateOtpResponse.user.phone)
        validateOtpResponse.store?.run {
            StaticInstances.sStoreId = storeId
            PrefsManager.storeStringDataInSharedPref(Constants.STORE_ID, "$storeId")
            PrefsManager.storeStringDataInSharedPref(Constants.STORE_NAME, storeInfo.name)
        }
    }

    override fun onGenerateOTPException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }
}