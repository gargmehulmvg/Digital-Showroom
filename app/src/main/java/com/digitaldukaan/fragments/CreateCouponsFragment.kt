package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager
import kotlinx.android.synthetic.main.layout_create_coupons_fragment.*

class CreateCouponsFragment : BaseFragment() {

    companion object {
        private const val TAG = "CreateCouponsFragment"
        fun newInstance(): CreateCouponsFragment = CreateCouponsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_create_coupons_fragment, container, false)
        hideBottomNavigationView(true)
        ToolBarManager.getInstance()?.hideToolBar(mActivity, true)
        return mContentView
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            backButtonToolbar?.id -> mActivity?.onBackPressed()
            createCouponsTextView?.id -> launchFragment(CustomCouponsFragment.newInstance(), true)
        }
    }

}