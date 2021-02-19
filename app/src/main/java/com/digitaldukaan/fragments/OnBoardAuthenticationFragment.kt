package com.digitaldukaan.fragments

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.Constants.Companion.CREDENTIAL_PICKER_REQUEST
import com.digitaldukaan.interfaces.IOnBackPressedListener
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.CredentialsApi
import com.google.android.gms.auth.api.credentials.HintRequest
import kotlinx.android.synthetic.main.on_board_authentication_fragment.*


class OnBoardAuthenticationFragment : BaseFragment(), IOnBackPressedListener {

    private var mIsDoublePressToExit = false
    private val mTag = OnBoardAuthenticationFragment::class.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        }
        mNavController = findNavController()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.on_board_authentication_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupMobileNumberEditText()
        initiateAutoDetectMobileNumber()
    }

    private fun setupMobileNumberEditText() {
        mobileNumberEditText.requestFocus()
        mobileNumberEditText.showKeyboard()
        getOtpTextView.setOnClickListener {
            val mobileNumber = mobileNumberEditText.text.trim().toString()
            val validationFailed = isMobileNumberValidationNotCorrect(mobileNumber)
            performOTPServerCall(validationFailed)
        }
        mobileNumberEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getOtpTextView.callOnClick()
            }
            true
        }
    }

    private fun performOTPServerCall(validationFailed: Boolean) {
        if (validationFailed) {
            mobileNumberEditText.requestFocus()
        } else {
            mobileNumberEditText.hideKeyboard()
            val action =
                OnBoardAuthenticationFragmentDirections.actionOnBoardAuthenticationFragmentToOtpVerificationFragment()
            mNavController.navigate(action)
        }
    }

    private fun isMobileNumberValidationNotCorrect(mobileNumber: String) : Boolean{
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

    override fun onBackPressedToExit() {
        if (mIsDoublePressToExit) mActivity.finish()
        showShortSnackBar(getString(R.string.msg_back_press))
        mIsDoublePressToExit = true
        Handler(Looper.getMainLooper()).postDelayed(
            { mIsDoublePressToExit = false },
            Constants.BACK_PRESS_INTERVAL
        )
    }

    private fun initiateAutoDetectMobileNumber() {
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
        } catch (e: SendIntentException) {
            e.printStackTrace()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == Activity.RESULT_OK) {
            // Obtain the phone number from the result
            val credentials: Credential? = data?.getParcelableExtra(Credential.EXTRA_KEY)
            Log.d(mTag, "onActivityResult: ${credentials?.id?.substring(3)}") //get the selected phone number
            //Do what ever you want to do with your selected phone number here
        } else if (requestCode == CREDENTIAL_PICKER_REQUEST && resultCode == CredentialsApi.ACTIVITY_RESULT_NO_HINTS_AVAILABLE) {
            // *** No phone numbers available ***
            Toast.makeText(context, "No phone numbers found", Toast.LENGTH_LONG).show()
        }
    }

}