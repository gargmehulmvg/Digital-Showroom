package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.BankDetailsRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.BankDetailsService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IBankDetailsServiceInterface
import com.digitaldukaan.views.allowOnlyAlphaNumericCharacters
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_bank_account_fragment.*

class BankAccountFragment : BaseFragment(), IBankDetailsServiceInterface {

    private var mProfilePreviewResponse: ProfilePreviewSettingsKeyResponse? = null
    private var mProfileInfoResponse: ProfileInfoResponse? = null
    private lateinit var mService: BankDetailsService
    private var mPosition: Int = 0
    private var mIsSingleStep: Boolean = false
    private lateinit var mBlankView: View
    private var mProfilePreviewStaticData: BankDetailsPageStaticTextResponse? = null

    companion object {
        fun newInstance(
            profilePreviewResponse: ProfilePreviewSettingsKeyResponse?,
            position: Int,
            isSingleStep: Boolean,
            profileInfoResponse: ProfileInfoResponse?
        ): BankAccountFragment {
            val fragment = BankAccountFragment()
            fragment.mProfilePreviewResponse = profilePreviewResponse
            fragment.mPosition = position
            fragment.mIsSingleStep = isSingleStep
            fragment.mProfileInfoResponse = profileInfoResponse
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_bank_account_fragment, container, false)
        mService = BankDetailsService()
        mService.setServiceInterface(this)
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
            showProgressDialog(mActivity)
            mService.getBankDetailsPageInfo()
        }
        mBlankView = mContentView.findViewById(R.id.blankView)
        mBlankView.visibility = View.GONE
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("")
            onBackPressed(this@BankAccountFragment)
            hideBackPressFromToolBar(mActivity, false)
            setSideIconVisibility(false)
            setSecondSideIconVisibility(false)
        }
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        hideBottomNavigationView(true)
    }

    private fun setupUIFromStaticData(bankDetail: BankDetailsResponse?) {
        bankHeadingTextView?.text = mProfilePreviewStaticData?.heading_bank_page
        materialTextView3?.text = mProfilePreviewStaticData?.sub_heading_bank_page
        accountHolderNameLayout?.hint = mProfilePreviewStaticData?.hint_bank_account_holder_name
        accountNumberLayout?.hint = mProfilePreviewStaticData?.hint_bank_account_number
        verifyAccountNumberLayout?.hint = mProfilePreviewStaticData?.hint_bank_verify_account_number
        ifscLayout?.hint = mProfilePreviewStaticData?.hint_bank_ifsc_code
        mobileNumberLayout?.hint = mProfilePreviewStaticData?.hint_bank_registered_mobile_number
        mobileNumberLayout?.hint = mProfilePreviewStaticData?.hint_bank_registered_mobile_number
        saveTextView?.text = mProfilePreviewStaticData?.hint_bank_save_changes
        bankDetail?.run {
            accountHolderNameEditText?.setText(accountHolderName)
            mobileNumberEditText?.setText(registeredPhone)
        }
        mobileNumberEditText?.setText(if (bankDetail == null || (bankDetail.registeredPhone?.isEmpty() == true)) PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER) else bankDetail.registeredPhone)
        mobileNumberEditText?.isEnabled = false
        ifscEditText?.allowOnlyAlphaNumericCharacters()
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            saveTextView.id -> {
                var isValidationFailed = false
                val accountHolderNameStr = accountHolderNameEditText?.run {
                    if (text.trim().toString().isEmpty()) {
                        requestFocus()
                        error = mProfilePreviewStaticData?.error_mandatory_field
                        isValidationFailed = true
                    }
                    text.trim().toString()
                }
                val accountNumberStr = accountNumberEditText?.run {
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
                val verifyAccountNumberStr = verifyAccountNumberEditText?.run {
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
                    verifyAccountNumberEditText?.run {
                        requestFocus()
                        error = mProfilePreviewStaticData?.error_both_account_number_verify_account_number_must_be_same
                        isValidationFailed = true
                    }
                }
                val ifscCodeStr = ifscEditText?.run {
                    if (text.trim().toString().isEmpty()) {
                        requestFocus()
                        error = mProfilePreviewStaticData?.error_mandatory_field
                        isValidationFailed = true
                    }
                    text.trim().toString()
                }
                val mobileNumberStr = mobileNumberEditText?.run {
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
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                val bankResponse = Gson().fromJson<BankDetailsResponse>(response.mCommonDataStr, BankDetailsResponse::class.java)
                StaticInstances.sBankDetails = bankResponse
                if (!mIsSingleStep) {
                    StaticInstances.sStepsCompletedList?.run {
                        for (completedItem in this) {
                            if (completedItem.action == Constants.ACTION_BANK) {
                                completedItem.isCompleted = true
                                break
                            }
                        }
                        switchToInCompleteProfileFragment(mProfileInfoResponse)
                    }
                } else {
                    mActivity.onBackPressed()
                }
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
}