package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.layout_transactions.*

class SettlementsFragment: BaseFragment() {

    companion object {
        private const val TAG = "SettlementsFragment"

        fun newInstance(): SettlementsFragment = SettlementsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_settlements, container, false)
        return mContentView
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            startDateTextView?.id -> showDatePickerDialog()
            endDateTextView?.id -> showDatePickerDialog()
        }
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