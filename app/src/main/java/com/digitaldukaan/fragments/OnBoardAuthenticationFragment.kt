package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.interfaces.IOnBackPressedListener
import kotlinx.android.synthetic.main.on_board_authentication_fragment.*


class OnBoardAuthenticationFragment : BaseFragment(), IOnBackPressedListener {

    private var mIsDoublePressToExit = false

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

}