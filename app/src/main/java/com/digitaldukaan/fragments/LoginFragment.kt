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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.transition.TransitionInflater
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.Constants.Companion.CREDENTIAL_PICKER_REQUEST
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.AuthNewResponseData
import com.digitaldukaan.models.response.GenerateOtpResponse
import com.digitaldukaan.services.LoginService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ILoginServiceInterface
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsApi
import com.google.android.gms.auth.api.credentials.HintRequest
import com.truecaller.android.sdk.*
import kotlinx.android.synthetic.main.layout_login_fragment.*

class LoginFragment : BaseFragment(), ILoginServiceInterface {

    private var mIsDoublePressToExit = false
    private var mIsMobileNumberSearchingDone = false
    private lateinit var mLoginService: LoginService
    private val mAuthStaticData: AuthNewResponseData = mStaticData.mStaticData.mAuthNew

    companion object {
        private const val TAG = "LoginFragment"
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
        val trueScope = TruecallerSdkScope.Builder(mActivity, sdkCallback)
            .consentMode(TruecallerSdkScope.CONSENT_MODE_BOTTOMSHEET)
            .loginTextPrefix(TruecallerSdkScope.LOGIN_TEXT_PREFIX_TO_GET_STARTED)
            .loginTextSuffix(TruecallerSdkScope.LOGIN_TEXT_SUFFIX_PLEASE_VERIFY_MOBILE_NO)
            .buttonColor(ContextCompat.getColor(mActivity, R.color.black))
            .buttonTextColor(ContextCompat.getColor(mActivity, R.color.white))
            .consentTitleOption(TruecallerSdkScope.SDK_CONSENT_TITLE_VERIFY)
            .footerType(TruecallerSdkScope.FOOTER_TYPE_NONE)
            .sdkOptions(TruecallerSdkScope.SDK_OPTION_WITHOUT_OTP)
            .buttonShapeOptions(TruecallerSdkScope.BUTTON_SHAPE_ROUNDED)
            .build()
        TruecallerSDK.init(trueScope)
    }

    private val sdkCallback = object : ITrueCallback {

        override fun onFailureProfileShared(p0: TrueError) {
            Log.d(TAG, "onFailureProfileShared: $p0")
            showToast("onFailureProfileShared")
        }

        override fun onSuccessProfileShared(p0: TrueProfile) {
            Log.d(TAG, "onSuccessProfileShared: $p0")
            showToast("onSuccessProfileShared")
        }

        override fun onVerificationRequired(p0: TrueError?) {
            Log.d(TAG, "onVerificationRequired: $p0")
            showToast("onVerificationRequired")
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_login_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply {
            setHeaderTitle("")
            hideToolBar(mActivity, true)
        }
        setupDataFromStaticResponse()
        setupMobileNumberEditText()
        if (mMobileNumber.isNotEmpty()) {
            mobileNumberInputLayout.visibility = View.VISIBLE
            mobileNumberTextView.visibility = View.GONE
        }
    }

    private fun setupDataFromStaticResponse() {
        enterMobileNumberHeading.text = mAuthStaticData.mHeadingText
        send4DigitOtpHeading.text = mAuthStaticData.mSubHeadingText
        mobileNumberInputLayout.hint = mAuthStaticData.mInputText
        mobileNumberTextView.text = mAuthStaticData.mInputText
        getOtpTextView.text = mAuthStaticData.mButtonText
    }

    private fun setupMobileNumberEditText() {
        mobileNumberEditText.apply {
            requestFocus()
            showKeyboard()
        }
        mobileNumberEditText.setOnEditorActionListener { _, actionId, _ ->
            if (EditorInfo.IME_ACTION_DONE == actionId) getOtpTextView.callOnClick()
            true
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            mobileNumberTextView.id -> {
                mobileNumberTextView.visibility = View.GONE
                mobileNumberInputLayout.visibility = View.VISIBLE
                initiateAutoDetectMobileNumber()
            }
            getOtpTextView.id -> {
                val mobileNumber = mobileNumberEditText.text.trim().toString()
                val validationFailed = isMobileNumberValidationNotCorrect(mobileNumber)
                performOTPServerCall(validationFailed, mobileNumber)
            }
        }
    }

    private fun performOTPServerCall(validationFailed: Boolean, mobileNumber: String) {
        if (validationFailed) {
            mobileNumberEditText.requestFocus()
        } else {
            if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
                return
            }
            showProgressDialog(mActivity)
            mobileNumberEditText.hideKeyboard()
            mLoginService.generateOTP(mobileNumber)
        }
    }

    private fun isMobileNumberValidationNotCorrect(mobileNumber: String): Boolean {
        return when {
            mobileNumber.isEmpty() -> {
                mobileNumberEditText.error = getString(R.string.mandatory_field_message)
                true
            }
            resources.getInteger(R.integer.mobile_number_length) != mobileNumber.length -> {
                mobileNumberEditText.error = getString(R.string.mobile_number_length_validation_message)
                true
            }
            else -> false
        }
    }

    override fun onBackPressed(): Boolean {
        if (mIsDoublePressToExit) mActivity.finish()
        showShortSnackBar(getString(R.string.msg_back_press))
        mIsDoublePressToExit = true
        Handler(Looper.getMainLooper()).postDelayed(
            { mIsDoublePressToExit = false },
            Constants.BACK_PRESS_INTERVAL
        )
        return true
    }

    private fun initiateAutoDetectMobileNumber() {
        mIsMobileNumberSearchingDone = true
        val hintRequest = HintRequest.Builder().setPhoneNumberIdentifierSupported(true).build()
        val intent: PendingIntent =
            Credentials.getClient(mActivity).getHintPickerIntent(hintRequest)
        try {
            startIntentSenderForResult(intent.intentSender, CREDENTIAL_PICKER_REQUEST, null, 0, 0, 0, Bundle())
        } catch (e: IntentSender.SendIntentException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val credentials: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
            credentials?.let {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    mobileNumberEditText.apply {
                        text = null
                        mMobileNumber = it.id.substring(3)
                        setText(mMobileNumber)
                        setSelection(mobileNumberEditText.text.trim().length)
                    }
                    getOtpTextView.callOnClick()
                }
            }
        } else if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE) {
            Toast.makeText(context, "No phone numbers found", Toast.LENGTH_LONG).show()
        } else if (requestCode == TruecallerSDK.SHARE_PROFILE_REQUEST_CODE) {
            mIsMobileNumberSearchingDone = true
            showShortSnackBar("True Caller result :: ${data?.data}")
        } else {
            if (!mIsMobileNumberSearchingDone) initiateAutoDetectMobileNumber()
        }
    }

    override fun onGenerateOTPResponse(generateOtpResponse: GenerateOtpResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (generateOtpResponse.mStatus) {
                val mobileNumber = mobileNumberEditText.text.trim().toString()
                launchFragment(OtpVerificationFragment.newInstance(mobileNumber), true)
            } else {
                showToast(generateOtpResponse.mMessage)
            }
        }
    }

    override fun onGenerateOTPException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }
}