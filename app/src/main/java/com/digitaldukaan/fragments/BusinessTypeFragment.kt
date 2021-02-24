package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.digitaldukaan.services.isInternetConnectionAvailable

class BusinessTypeFragment : BaseFragment() {

    private lateinit var mProfilePreviewResponse: ProfilePreviewSettingsKeyResponse

    companion object {
        fun newInstance(profilePreviewResponse: ProfilePreviewSettingsKeyResponse): BusinessTypeFragment {
            val fragment = BusinessTypeFragment()
            fragment.mProfilePreviewResponse = profilePreviewResponse
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.business_type_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("")
            onBackPressed(this@BusinessTypeFragment)
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