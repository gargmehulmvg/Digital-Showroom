package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.services.isInternetConnectionAvailable

class MoreControlsFragment : BaseFragment() {

    companion object {
        fun newInstance(): MoreControlsFragment {
            val fragment = MoreControlsFragment()
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.more_control_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("")
            onBackPressed(this@MoreControlsFragment)
            hideBackPressFromToolBar(mActivity, false)
        }
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
    }

}