package com.digitaldukaan.fragments

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.transition.TransitionInflater
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.Constants.Companion.CREDENTIAL_PICKER_REQUEST
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.response.GenerateOtpResponse
import com.digitaldukaan.services.LoginService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ILoginServiceInterface
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsApi
import com.google.android.gms.auth.api.credentials.HintRequest
import kotlinx.android.synthetic.main.login_fragment.*

class LoginFragment : BaseFragment(), ILoginServiceInterface {

    private var mIsDoublePressToExit = false
    private var mIsMobileNumberSearchingDone = false
    private lateinit var mLoginService: LoginService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        }
        mLoginService = LoginService()
        mLoginService.setLoginServiceInterface(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.login_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupMobileNumberEditText()
        if (!mIsMobileNumberSearchingDone) initiateAutoDetectMobileNumber()
    }

    private fun setupMobileNumberEditText() {
        mobileNumberEditText.apply {
            requestFocus()
            showKeyboard()
        }
        getOtpTextView.setOnClickListener {
            val mobileNumber = mobileNumberEditText.text.trim().toString()
            val validationFailed = isMobileNumberValidationNotCorrect(mobileNumber)
            performOTPServerCall(validationFailed, mobileNumber)
        }
        mobileNumberEditText.setOnEditorActionListener { _, actionId, _ ->
            if (EditorInfo.IME_ACTION_DONE == actionId) getOtpTextView.callOnClick()
            true
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
        if (mIsDoublePressToExit) {
            mActivity.finish()
        }
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
        Handler(Looper.getMainLooper()).postDelayed({
            val hintRequest = HintRequest.Builder()
                .setPhoneNumberIdentifierSupported(true)
                .build()
            val intent: PendingIntent =
                Credentials.getClient(mActivity).getHintPickerIntent(hintRequest)
            try {
                startIntentSenderForResult(
                    intent.intentSender,
                    CREDENTIAL_PICKER_REQUEST,
                    null,
                    0,
                    0,
                    0,
                    Bundle()
                )
            } catch (e: IntentSender.SendIntentException) {
                e.printStackTrace()
            }
        }, Constants.TIMER_INTERVAL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            val credentials: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
            credentials?.let {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    mobileNumberEditText.apply {
                        text = null
                        setText(it.id.substring(3))
                        setSelection(mobileNumberEditText.text.trim().length)
                    }
                    getOtpTextView.callOnClick()
                }
            }
        } else if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE) {
            Toast.makeText(context, "No phone numbers found", Toast.LENGTH_LONG).show()
        }
    }

    override fun onGenerateOTPResponse(generateOtpResponse: GenerateOtpResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (generateOtpResponse.mStatus) {
                val mobileNumber = mobileNumberEditText.text.trim().toString()
                launchFragment(OtpVerificationFragment().newInstance(mobileNumber), true)
            } else {
                showToast(generateOtpResponse.mMessage)
            }
        }
    }

    override fun onGenerateOTPException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }
}