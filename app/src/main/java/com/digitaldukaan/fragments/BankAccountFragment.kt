package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.digitaldukaan.services.isInternetConnectionAvailable

class BankAccountFragment : BaseFragment() {

    private lateinit var mProfilePreviewResponse: ProfilePreviewSettingsKeyResponse

    companion object {
        fun newInstance(profilePreviewResponse: ProfilePreviewSettingsKeyResponse): BankAccountFragment {
            val fragment = BankAccountFragment()
            fragment.mProfilePreviewResponse = profilePreviewResponse
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.bank_account_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
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
        /*val splashService = SplashService()
        splashService.setSplashServiceInterface(this)
        splashService.getStaticData("0")*/
    }

}