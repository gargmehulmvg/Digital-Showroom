package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.MyPaymentsPagerAdapter
import com.digitaldukaan.constants.ToolBarManager
import com.google.android.material.tabs.TabLayout

class MyPaymentsFragment: BaseFragment() {

    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var mMyPaymentsPagerAdapter: MyPaymentsPagerAdapter? = null

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
        tabLayout = mContentView?.findViewById(R.id.tabLayout)
        viewPager = mContentView?.findViewById(R.id.viewPager)
        mMyPaymentsPagerAdapter = MyPaymentsPagerAdapter(mActivity?.supportFragmentManager)
        viewPager?.adapter = mMyPaymentsPagerAdapter
        tabLayout?.setupWithViewPager(viewPager)
    }

}