package com.digitaldukaan.fragments

import android.app.Activity
import android.app.PendingIntent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.CustomPagerAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.ValidateUserRequest
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import com.truecaller.android.sdk.*
import kotlinx.android.synthetic.main.layout_login_fragment.*
import kotlinx.android.synthetic.main.layout_login_fragment_v2.*
import kotlinx.android.synthetic.main.layout_login_fragment_v2.mobileNumberEditText
import kotlinx.android.synthetic.main.layout_login_fragment_v2.mobileNumberTextView
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEventListener

class LoginFragmentV2 : BaseFragment() {

    private var mIntentUri: Uri? = null
    private var mIsDoublePressToExit = false
    private var mMobileNumber = ""
    private var mIsMobileNumberSearchingDone = false

    companion object {
        private val TAG = LoginFragmentV2::class.simpleName
        private const val USE_ANOTHER_NUMBER = 14

        fun newInstance(intentUri: Uri?): LoginFragmentV2 {
            val fragment = LoginFragmentV2()
            fragment.mIntentUri = intentUri
            return fragment
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
        val pagerAdapter = CustomPagerAdapter(mActivity)
        val viewpager: ViewPager? = mContentView?.findViewById(R.id.viewpager)
        val indicator: DotsIndicator? = mContentView?.findViewById(R.id.indicator)
        viewpager?.adapter = pagerAdapter
        viewpager?.let { indicator?.setViewPager(it) }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            otpTextView?.id -> {
                mobileNumberEditText?.apply {
                    hideSoftKeyboard()
                    clearFocus()
                }
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

    private fun launchHomeFragment() {
        when {
            null != mIntentUri -> switchToFragmentByDeepLink()
            "" == getStringDataFromSharedPref(Constants.STORE_ID) -> launchFragment(
                OnBoardHelpScreenFragment.newInstance(),
                true
            )
            else -> launchFragment(HomeFragment.newInstance(), true)
        }
    }

    private fun switchToFragmentByDeepLink() {
        val intentUriStr = mIntentUri.toString()
        Log.d(TAG, "switchToFragmentByDeepLink:: $intentUriStr")
        val deepLinkStr = "digitaldukaan://"
        clearFragmentBackStack()
        when {
            intentUriStr.contains("${deepLinkStr}Settings") -> launchFragment(
                SettingsFragment.newInstance(),
                true
            )
            intentUriStr.contains("${deepLinkStr}ProfilePage") -> launchFragment(
                ProfilePreviewFragment.newInstance(),
                true
            )
            intentUriStr.contains("${deepLinkStr}ProductList") -> launchFragment(
                HomeFragment.newInstance(),
                true
            )
            intentUriStr.contains("${deepLinkStr}OrderList") -> launchFragment(
                HomeFragment.newInstance(),
                true
            )
            intentUriStr.contains("${deepLinkStr}ProductAdd") -> launchFragment(
                ProductFragment.newInstance(),
                true
            )
            intentUriStr.contains("${deepLinkStr}MarketingBroadCast") -> launchFragment(
                MarketingFragment.newInstance(),
                true
            )
            intentUriStr.contains("${deepLinkStr}OTP") -> launchFragment(
                LoginFragment.newInstance(),
                true
            )
            intentUriStr.contains("${deepLinkStr}PaymentOptions") -> launchFragment(
                PaymentModesFragment.newInstance(),
                true
            )
            intentUriStr.contains("${deepLinkStr}Webview") -> {
                try {
                    var webViewUrl = intentUriStr.split("${deepLinkStr}Webview?webURL=")[1]
                    if (webViewUrl.contains("&")) webViewUrl = webViewUrl.split("&")[0]
                    openWebViewFragment(this, "", "${BuildConfig.WEB_VIEW_URL}$webViewUrl")
                } catch (e: Exception) {
                    Log.e(TAG, "switchToFragmentByDeepLink: ${e.message}", e)
                }
            }
            else -> {
                mIntentUri = null
                launchHomeFragment()
            }
        }
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
            }
        }
}