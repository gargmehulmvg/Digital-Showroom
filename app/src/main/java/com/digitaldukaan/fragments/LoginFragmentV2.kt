package com.digitaldukaan.fragments

import android.app.Activity
import android.app.PendingIntent
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
import com.digitaldukaan.adapters.CustomPagerAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.constants.StaticInstances.sHelpScreenList
import com.digitaldukaan.models.request.ValidateUserRequest
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import com.tbuonomo.viewpagerdotsindicator.WormDotsIndicator
import com.truecaller.android.sdk.*
import kotlinx.android.synthetic.main.layout_login_fragment_v2.*
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener
import net.yslibrary.android.keyboardvisibilityevent.util.UIUtil
import java.util.*

class LoginFragmentV2 : BaseFragment() {

    private var mIsDoublePressToExit = false
    private var mMobileNumber = ""
    private var mIsMobileNumberSearchingDone = false
    private var mViewPagerTimer: Timer? = null

    companion object {
        private val TAG = LoginFragmentV2::class.simpleName
        private const val USE_ANOTHER_NUMBER = 14

        fun newInstance(): LoginFragmentV2 {
            return LoginFragmentV2()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_login_fragment_v2, container, false)
        hideBottomNavigationView(true)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        mActivity?.let { context ->
            KeyboardVisibilityEvent.setEventListener(context, object : KeyboardVisibilityEventListener {

                override fun onVisibilityChanged(isOpen: Boolean) {
                    trueCallerContainer?.visibility = if (isOpen) View.GONE else View.VISIBLE
                }

            })
        }
        setupViewPager()
        mobileNumberEditText?.apply {
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "beforeTextChanged: mobileNumberEditText")
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    Log.d(TAG, "onTextChanged: mobileNumberEditText")
                }

                override fun afterTextChanged(editable: Editable?) {
                    val str = editable?.toString() ?: ""
                    otpTextView?.isEnabled = isNotEmpty(str)
                }

            })
        }
    }

    private fun setupViewPager() {
        val pagerAdapter = CustomPagerAdapter(mActivity)
        val viewPager: ViewPager? = mContentView?.findViewById(R.id.viewpager)
        val indicator: WormDotsIndicator? = mContentView?.findViewById(R.id.indicator)
        viewPager?.adapter = pagerAdapter
        viewPager?.let { indicator?.setViewPager(it) }
        val timerTask = object : TimerTask() {
            override fun run() {
                viewPager?.post {
                    viewPager.currentItem = ((viewPager.currentItem + 1) % (sHelpScreenList.size))
                }
            }
        }
        mViewPagerTimer = Timer()
        mViewPagerTimer?.schedule(timerTask, 3000,3000)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
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
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_TRUE_CALLER_VERIFIED,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(
                        Constants.STORE_ID
                    )
                )
            )
            val request = ValidateUserRequest(
                response.payload,
                response.signature,
                StaticInstances.sCleverTapId,
                StaticInstances.sFireBaseMessagingToken,
                if (response.phoneNumber.length >= 10) response.phoneNumber.substring(response.phoneNumber.length - 10) else ""
            )
//            mLoginService.validateUser(request)
        }

        override fun onVerificationRequired(p0: TrueError?) {
            Log.d(TAG, "onVerificationRequired: $p0")
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
        mViewPagerTimer?.cancel()
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
//            showProgressDialog(mActivity)
//            mLoginService.generateOTP(mobileNumber)
            mActivity?.let { context -> UIUtil.hideKeyboard(context) }
            launchFragment(OtpVerificationFragment.newInstance(mobileNumberEditText?.text?.trim().toString()), true)
        }
    }
}