package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.interfaces.IOnBackPressedListener
import kotlinx.android.synthetic.main.on_board_authentication_fragment.*


class OnBoardAuthenticationFragment : BaseFragment(), IOnBackPressedListener {

    private var mIsDoublePressToExit = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.on_board_authentication_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mobileNumberEditText.requestFocus()
        mobileNumberEditText.showKeyboard()
        getOtpTextView.setOnClickListener {
            val mobileNumber = mobileNumberEditText.text.trim().toString()
            val validationFailed = checkMobileNumberValidation(mobileNumber)
            if (validationFailed) {
                mobileNumberEditText.requestFocus()
            } else {
                showToast("Validation success")
                mobileNumberEditText.hideKeyboard()
            }
        }
        mobileNumberEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                getOtpTextView.callOnClick()
            }
            true
        }
    }

    private fun checkMobileNumberValidation(mobileNumber: String) : Boolean{
        return when {
            mobileNumber.isEmpty() -> {
                mobileNumberEditText.error = getString(R.string.mandatory_field_message)
                true
            }
            mobileNumber.length != resources.getInteger(R.integer.mobile_number_length) -> {
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