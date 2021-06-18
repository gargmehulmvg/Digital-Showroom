package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.MyPaymentsPagerAdapter
import com.digitaldukaan.constants.ToolBarManager
import com.google.android.material.tabs.TabLayout


class MyPaymentsFragment: BaseFragment(), TabLayout.OnTabSelectedListener {

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
        val transactionFragment = TransactionsFragment()
        val settlementFragment = SettlementsFragment()
        val fragmentList = ArrayList<BaseFragment>()
        fragmentList.add(transactionFragment)
        fragmentList.add(settlementFragment)
        val fragmentHeaderList = ArrayList<String>()
        fragmentHeaderList.add("Transactions")
        fragmentHeaderList.add("Settlements")
        mMyPaymentsPagerAdapter = MyPaymentsPagerAdapter(mActivity?.supportFragmentManager, fragmentList, fragmentHeaderList)
        viewPager?.adapter = mMyPaymentsPagerAdapter
        tabLayout?.setupWithViewPager(viewPager)
        tabLayout?.addOnTabSelectedListener(this)
    }

    override fun onTabReselected(tab: TabLayout.Tab?) {
        Log.d(TAG, "onTabReselected :: ${tab?.text}")
    }

    override fun onTabUnselected(tab: TabLayout.Tab?) {
        Log.d(TAG, "onTabUnselected :: ${tab?.text}")
    }

    override fun onTabSelected(tab: TabLayout.Tab?) {
        Log.d(TAG, "onTabSelected :: ${tab?.text}")
    }

}