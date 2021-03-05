package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.constants.openHelpFromToolbar
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.services.isInternetConnectionAvailable

class MarketingFragment : BaseFragment(), IOnToolbarIconClick {

    companion object {
        fun newInstance(): MarketingFragment {
            return MarketingFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.marketing_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            onBackPressed(this@MarketingFragment)
            setHeaderTitle("")
            setSideIconVisibility(true)
            hideBackPressFromToolBar(mActivity, false)
            setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_setting_toolbar), this@MarketingFragment)
        }
        showBottomNavigationView(false)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
        }

    }

    override fun onToolbarSideIconClicked() = openHelpFromToolbar(this)

}