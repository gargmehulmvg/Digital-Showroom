package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.TransactionsAdapter
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.google.android.material.datepicker.MaterialDatePicker

class TransactionsFragment: BaseFragment() {

    private var transactionRecyclerView: RecyclerView? = null
    private var startDateTextView: TextView? = null
    private var endDateTextView: TextView? = null

    companion object {
        private const val TAG = "TransactionsFragment"

        fun newInstance(): TransactionsFragment = TransactionsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_transactions, container, false)
        transactionRecyclerView = mContentView?.findViewById(R.id.transactionRecyclerView)
        startDateTextView = mContentView?.findViewById(R.id.startDateTextView)
        endDateTextView = mContentView?.findViewById(R.id.endDateTextView)
        startDateTextView?.setOnClickListener { showDatePickerDialog("Select Start date") }
        endDateTextView?.setOnClickListener { showDatePickerDialog("Select End Date") }
        setupRecyclerView()
        return mContentView
    }

    private fun setupRecyclerView() {
        transactionRecyclerView?.apply {
            layoutManager = LinearLayoutManager(mActivity)
            adapter = TransactionsAdapter(mActivity)
        }
    }

    private fun showDatePickerDialog(messgae: String) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            showToast()
            val dateRangePicker = MaterialDatePicker.Builder.datePicker().setTitleText(messgae).build()
            mActivity?.let { context -> dateRangePicker.show(context.supportFragmentManager, TAG) }
            dateRangePicker.addOnPositiveButtonClickListener { showToast("Yes") }
            dateRangePicker.addOnNegativeButtonClickListener { showToast("No") }
        }
    }

}