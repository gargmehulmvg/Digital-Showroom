package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.digitaldukaan.R
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.google.android.material.datepicker.MaterialDatePicker

class SettlementsFragment: BaseFragment() {

    companion object {
        private const val TAG = "SettlementsFragment"
        private var shareButtonTextView: TextView? = null
        private var startDateTextView: TextView? = null
        private var endDateTextView: TextView? = null

        fun newInstance(): SettlementsFragment = SettlementsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_settlements, container, false)
        shareButtonTextView = mContentView?.findViewById(R.id.shareButtonTextView)
        startDateTextView = mContentView?.findViewById(R.id.startDateTextView)
        endDateTextView = mContentView?.findViewById(R.id.endDateTextView)
        shareButtonTextView?.setOnClickListener { shareStoreOverWhatsAppServerCall() }
        startDateTextView?.setOnClickListener { showDatePickerDialog() }
        endDateTextView?.setOnClickListener { showDatePickerDialog() }
        return mContentView
    }

    private fun showDatePickerDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val dateRangePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select start And end date").build()
            mActivity?.let { context -> dateRangePicker.show(context.supportFragmentManager, TAG) }
            dateRangePicker.addOnPositiveButtonClickListener { showToast("Yes") }
            dateRangePicker.addOnNegativeButtonClickListener { showToast("No") }
        }
    }

}