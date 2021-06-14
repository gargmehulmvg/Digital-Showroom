package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R

class TransactionsFragment: BaseFragment() {

    companion object {
        private const val TAG = "TransactionsFragment"

        fun newInstance(): TransactionsFragment = TransactionsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_transactions, container, false)
        return mContentView
    }

}