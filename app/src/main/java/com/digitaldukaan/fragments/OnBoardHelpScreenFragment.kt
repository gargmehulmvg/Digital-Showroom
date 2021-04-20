package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.CustomPagerAdapter
import com.digitaldukaan.constants.ToolBarManager
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import kotlinx.android.synthetic.main.layout_on_board_help_screen.*

class OnBoardHelpScreenFragment : BaseFragment() {

    companion object {
        fun newInstance(): OnBoardHelpScreenFragment {
            return OnBoardHelpScreenFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_on_board_help_screen, container, false)
        hideBottomNavigationView(true)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, true)
        }
        val pagerAdapter = CustomPagerAdapter(mActivity)
        val viewpager: ViewPager = mContentView.findViewById(R.id.viewpager)
        val indicator: DotsIndicator = mContentView.findViewById(R.id.indicator)
        viewpager.adapter = pagerAdapter
        indicator.setViewPager(viewpager)
        return mContentView
    }

    override fun onClick(view: View?) {
        if (view?.id == startNowLayout.id) {
            launchFragment(LoginFragment.newInstance(), true)
        }
    }

}