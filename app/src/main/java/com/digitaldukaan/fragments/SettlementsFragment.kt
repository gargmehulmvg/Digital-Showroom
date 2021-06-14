package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R

class SettlementsFragment: BaseFragment() {

    companion object {
        private const val TAG = "MyPaymentsFragment"

        fun newInstance(): SettlementsFragment = SettlementsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_settlements, container, false)
        return mContentView
    }

}