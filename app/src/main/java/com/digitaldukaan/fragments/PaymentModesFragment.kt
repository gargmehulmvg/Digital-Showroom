package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager

class PaymentModesFragment: BaseFragment() {

    companion object {
        private const val TAG = "PaymentModesFragment"

        fun newInstance(): PaymentModesFragment{
            return PaymentModesFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_payment_modes, container, false)
        initializeUI()
        return mContentView
    }

    private fun initializeUI() {
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("")
            onBackPressed(this@PaymentModesFragment)
            setSideIconVisibility(false)
            setSecondSideIconVisibility(false)
        }
        hideBottomNavigationView(true)
        mContentView?.let { view ->
            //payBothRadioButton = view.findViewById(R.id.payBothRadioButton)
        }
    }

}