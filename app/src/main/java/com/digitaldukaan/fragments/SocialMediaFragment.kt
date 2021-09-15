package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager
import kotlinx.android.synthetic.main.layout_on_board_help_screen.*

class SocialMediaFragment : BaseFragment() {

    companion object {
        fun newInstance(): SocialMediaFragment {
            return SocialMediaFragment()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_social_media, container, false)
        hideBottomNavigationView(true)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("Social Media Posts")
            setSideIconVisibility(false)
            onBackPressed(this@SocialMediaFragment)
        }
        return mContentView
    }

    override fun onClick(view: View?) {
        if (view?.id == startNowLayout.id) {

        }
    }

}