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


class MyPaymentsFragment: BaseFragment(), TabLayout.OnTabSelectedListener,
    ViewPager.OnPageChangeListener {

    private var tabLayout: TabLayout? = null
    private var viewPager: ViewPager? = null
    private var mMyPaymentsPagerAdapter: MyPaymentsPagerAdapter? = null
    private lateinit var mSettlementsFragment: SettlementsFragment

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
            setHeaderTitle(getString(R.string.my_payments))
            onBackPressed(this@MyPaymentsFragment)
        }
        hideBottomNavigationView(true)
        tabLayout = mContentView?.findViewById(R.id.tabLayout)
        viewPager = mContentView?.findViewById(R.id.viewPager)
        val fragmentList = ArrayList<BaseFragment>()
        fragmentList.add(TransactionsFragment.newInstance())
        mSettlementsFragment = SettlementsFragment.newInstance()
        fragmentList.add(mSettlementsFragment)
        val fragmentHeaderList = ArrayList<String>()
        fragmentHeaderList.add(getString(R.string.orders))
        fragmentHeaderList.add(getString(R.string.settlements))
        mMyPaymentsPagerAdapter = MyPaymentsPagerAdapter(mActivity?.supportFragmentManager, fragmentList, fragmentHeaderList)
        viewPager?.adapter = mMyPaymentsPagerAdapter
        tabLayout?.setupWithViewPager(viewPager)
        tabLayout?.addOnTabSelectedListener(this)
        viewPager?.addOnPageChangeListener(this)
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

    override fun onPageScrollStateChanged(state: Int) {
        Log.d(TAG, "onPageScrollStateChanged :: $state")
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        Log.d(TAG, "onPageScrolled :: $position")
    }

    override fun onPageSelected(position: Int) {
        Log.d(TAG, "onPageSelected :: $position")
        mSettlementsFragment.onNoInternetButtonClick(false)
    }

}