package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.AddBankBottomSheetAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.BankDetailsRequest
import com.digitaldukaan.models.response.BankDetailsPageInfoResponse
import com.digitaldukaan.models.response.BankDetailsPageStaticTextResponse
import com.digitaldukaan.models.response.BankDetailsResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.services.BankDetailsService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IBankDetailsServiceInterface
import com.digitaldukaan.views.allowOnlyAlphaNumericCharacters
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_bank_account_fragment.*

class OnBoardScreenBankDetailsFragment : BaseFragment(), IBankDetailsServiceInterface {

    private lateinit var mService: BankDetailsService
    private var mProfilePreviewStaticData: BankDetailsPageStaticTextResponse? = null

    companion object {
        fun newInstance(): OnBoardScreenBankDetailsFragment {
            return OnBoardScreenBankDetailsFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showAddBankBottomSheet()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_bank_account_fragment, container, false)
        mService = BankDetailsService()
        mService.setServiceInterface(this)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, true)
            setHeaderTitle("")
            onBackPressed(this@OnBoardScreenBankDetailsFragment)
            hideBackPressFromToolBar(mActivity, false)
            setSideIconVisibility(false)
            setSecondSideIconVisibility(false)
        }
        hideBottomNavigationView(true)
        skipTextView.visibility = View.GONE
        parentLayout?.visibility = View.INVISIBLE
    }

    private fun setupUIFromStaticData(bankDetail: BankDetailsResponse?) {
        bankHeadingTextView.text = mProfilePreviewStaticData?.heading_bank_page
        materialTextView3.text = mProfilePreviewStaticData?.sub_heading_bank_page
        accountHolderNameLayout.hint = mProfilePreviewStaticData?.hint_bank_account_holder_name
        accountNumberLayout.hint = mProfilePreviewStaticData?.hint_bank_account_number
        verifyAccountNumberLayout.hint = mProfilePreviewStaticData?.hint_bank_verify_account_number
        ifscLayout.hint = mProfilePreviewStaticData?.hint_bank_ifsc_code
        mobileNumberLayout.hint = mProfilePreviewStaticData?.hint_bank_registered_mobile_number
        mobileNumberLayout.hint = mProfilePreviewStaticData?.hint_bank_registered_mobile_number
        saveTextView.text = mProfilePreviewStaticData?.hint_bank_save_changes
        bankDetail?.run {
            accountHolderNameEditText.setText(this.accountHolderName)
            mobileNumberEditText.setText(this.registeredPhone)
        }
        mobileNumberEditText.setText(PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER))
        mobileNumberEditText.isEnabled = false
        ifscEditText.allowOnlyAlphaNumericCharacters()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            skipTextView.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SKIP_BANK_ACCOUNT,
                    isCleverTapEvent = true, isAppFlyerEvent = false, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                launchFragment(CreateStoreFragment.newInstance(), true)
            }
            saveTextView.id -> {
                var isValidationFailed = false
                val accountHolderNameStr = accountHolderNameEditText.run {
                    if (text.trim().toString().isEmpty()) {
                        requestFocus()
                        error = mProfilePreviewStaticData?.error_mandatory_field
                        isValidationFailed = true
                    }
                    text.trim().toString()
                }
                val accountNumberStr = accountNumberEditText.run {
                    if (text.trim().toString().isEmpty()) {
                        requestFocus()
                        error = mProfilePreviewStaticData?.error_mandatory_field
                        isValidationFailed = true
                    } else if (text.trim().toString().length <= resources.getInteger(R.integer.account_number_length)) {
                        requestFocus()
                        error = mProfilePreviewStaticData?.error_invalid_account_number
                        isValidationFailed = true
                    }
                    text.trim().toString()
                }
                val verifyAccountNumberStr = verifyAccountNumberEditText.run {
                    if (text.trim().toString().isEmpty()) {
                        requestFocus()
                        error = mProfilePreviewStaticData?.error_mandatory_field
                        isValidationFailed = true
                    } else if (text.trim().toString().length <= resources.getInteger(R.integer.account_number_length)) {
                        requestFocus()
                        error = mProfilePreviewStaticData?.error_invalid_account_number
                        isValidationFailed = true
                    }
                    text.trim().toString()
                }
                if (accountNumberStr != verifyAccountNumberStr) {
                    verifyAccountNumberEditText.run {
                        requestFocus()
                        error = mProfilePreviewStaticData?.error_both_account_number_verify_account_number_must_be_same
                        isValidationFailed = true
                    }
                }
                val ifscCodeStr = ifscEditText.run {
                    if (text.trim().toString().isEmpty()) {
                        requestFocus()
                        error = mProfilePreviewStaticData?.error_mandatory_field
                        isValidationFailed = true
                    }
                    text.trim().toString()
                }
                val mobileNumberStr = mobileNumberEditText.run {
                    if (text.trim().toString().isEmpty()) {
                        requestFocus()
                        error = mProfilePreviewStaticData?.error_mandatory_field
                        isValidationFailed = true
                    } else if (text.trim().toString().length != resources.getInteger(R.integer.mobile_number_length)) {
                        requestFocus()
                        error = mProfilePreviewStaticData?.error_invalid_mobile_number
                        isValidationFailed = true
                    }
                    text.trim().toString()
                }
                if (isValidationFailed) return
                val request = BankDetailsRequest(accountHolderNameStr, mobileNumberStr, accountNumberStr, ifscCodeStr)
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                showProgressDialog(mActivity)
                mService.setBankDetails(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
            }
        }
    }

    override fun onBankDetailsResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_BANK_ACCOUNT_ADDED,
                    isCleverTapEvent = true, isAppFlyerEvent = false, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                launchFragment(CreateStoreFragment.newInstance(), true)
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onBankDetailsPageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val bankPageInfoResponse = Gson().fromJson<BankDetailsPageInfoResponse>(response.mCommonDataStr, BankDetailsPageInfoResponse::class.java)
                mProfilePreviewStaticData = bankPageInfoResponse?.mBankPageStaticText
                setupUIFromStaticData(bankPageInfoResponse?.mBankDetail)
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onBankDetailsServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    private fun showAddBankBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_bank_account, mActivity.findViewById(R.id.bottomSheetContainer))
        bottomSheetDialog.apply {
            setContentView(view)
            setCancelable(false)
            setBottomSheetCommonProperty()
            view.run {
                val bottomSheetBankRecyclerView: RecyclerView = view.findViewById(R.id.bottomSheetBankRecyclerView)
                val howItWorksTextView: TextView = view.findViewById(R.id.howItWorksTextView)
                val getOtpTextView: TextView = view.findViewById(R.id.getOtpTextView)
                getOtpTextView.setOnClickListener {
                    parentLayout?.visibility = View.VISIBLE
                    if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
                        showProgressDialog(mActivity)
                        mService.getBankDetailsPageInfo()
                    }
                    ToolBarManager.getInstance().apply {
                        hideToolBar(mActivity, false)
                        setHeaderTitle("")
                        onBackPressed(this@OnBoardScreenBankDetailsFragment)
                        hideBackPressFromToolBar(mActivity, false)
                        setSideIconVisibility(false)
                        setSecondSideIconVisibility(false)
                    }
                    bottomSheetDialog.dismiss()
                }
                howItWorksTextView.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_SKIP_BANK_ACCOUNT,
                        isCleverTapEvent = true, isAppFlyerEvent = false, isServerCallEvent = true,
                        data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                    )
                    launchFragment(CreateStoreFragment.newInstance(), true)
                }
                bottomSheetBankRecyclerView.apply {
                    setHasFixedSize(true)
                    layoutManager = LinearLayoutManager(mActivity)
                    val bankList = ArrayList<String>()
                    bankList.add("Get Instant Settlements.")
                    bankList.add("Get money directly in your bank account.")
                    bankList.add("0% commission & No hidden fees.")
                    adapter = AddBankBottomSheetAdapter(bankList)
                }
            }
        }.show()
    }

}