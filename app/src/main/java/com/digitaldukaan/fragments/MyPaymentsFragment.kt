package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager

class MyPaymentsFragment: BaseFragment() {

    companion object {
        private const val TAG = "MyPaymentsFragment"

        fun newInstance(): MyPaymentsFragment = MyPaymentsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_my_payment, container, false)
        initializeUI()
        return mContentView
    }

    private fun initializeUI() {
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("My Payments")
            onBackPressed(this@MyPaymentsFragment)
        }
        hideBottomNavigationView(true)
    }

}