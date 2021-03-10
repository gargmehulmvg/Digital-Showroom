package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.digitaldukaan.models.response.ProfileStaticTextResponse
import com.digitaldukaan.services.isInternetConnectionAvailable
import kotlinx.android.synthetic.main.bank_account_fragment.*

class BankAccountFragment : BaseFragment() {

    private lateinit var mProfilePreviewResponse: ProfilePreviewSettingsKeyResponse
    private lateinit var mProfilePreviewStaticData: ProfileStaticTextResponse

    companion object {
        fun newInstance(profilePreviewResponse: ProfilePreviewSettingsKeyResponse, staticData: ProfileStaticTextResponse): BankAccountFragment {
            val fragment = BankAccountFragment()
            fragment.mProfilePreviewResponse = profilePreviewResponse
            fragment.mProfilePreviewStaticData = staticData
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.bank_account_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("")
            onBackPressed(this@BankAccountFragment)
            hideBackPressFromToolBar(mActivity, false)
        }
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        setupUIFromStaticData()
    }

    private fun setupUIFromStaticData() {
        bankHeadingTextView.text = mProfilePreviewStaticData.heading_bank_page
        materialTextView3.text = mProfilePreviewStaticData.sub_heading_bank_page
        accountHolderNameLayout.hint = mProfilePreviewStaticData.hint_bank_account_holder_name
        accountNumberLayout.hint = mProfilePreviewStaticData.hint_bank_account_number
        verifyAccountNumberLayout.hint = mProfilePreviewStaticData.hint_bank_verify_account_number
        ifscLayout.hint = mProfilePreviewStaticData.hint_bank_ifsc_code
        mobileNumberLayout.hint = mProfilePreviewStaticData.hint_bank_registered_mobile_number
        mobileNumberLayout.hint = mProfilePreviewStaticData.hint_bank_registered_mobile_number
        saveTextView.text = mProfilePreviewStaticData.hint_bank_save_changes
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            saveTextView.id -> {
                //launchFragment(CreateStoreFragment.newInstance(), true)
                val accountHolderName = accountHolderNameEditText.text.trim().toString()
            }
        }
    }

}