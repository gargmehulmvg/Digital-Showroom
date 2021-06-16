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
import com.digitaldukaan.constants.getDayOfTheWeek
import com.digitaldukaan.constants.getMonthOfTheWeek
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.MyPaymentsItemResponse
import com.digitaldukaan.models.response.MyPaymentsResponse
import com.digitaldukaan.services.MyPaymentsService
import com.digitaldukaan.services.serviceinterface.IMyPaymentsServiceInterface
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import java.util.*
import kotlin.collections.ArrayList

class TransactionsFragment: BaseFragment(), IMyPaymentsServiceInterface {

    private var transactionRecyclerView: RecyclerView? = null
    private var startDateTextView: TextView? = null
    private var endDateTextView: TextView? = null
    private val mService: MyPaymentsService = MyPaymentsService()
    private var mStartDateStr: String? = ""
    private var mEndDateStr: String? = ""
    private var mPageNumber = 1
    private var mPaymentList: ArrayList<MyPaymentsItemResponse>? = ArrayList()

    companion object {
        private const val TAG = "TransactionsFragment"

        fun newInstance(): TransactionsFragment = TransactionsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_transactions, container, false)
        transactionRecyclerView = mContentView?.findViewById(R.id.transactionRecyclerView)
        startDateTextView = mContentView?.findViewById(R.id.startDateTextView)
        endDateTextView = mContentView?.findViewById(R.id.endDateTextView)
        startDateTextView?.setOnClickListener { showDatePickerDialog("Select Start date", startDateTextView) }
        endDateTextView?.setOnClickListener { showDatePickerDialog("Select End Date", endDateTextView) }
        val shareButtonTextView: TextView? = mContentView?.findViewById(R.id.shareButtonTextView)
        shareButtonTextView?.setOnClickListener { shareStoreOverWhatsAppServerCall() }
        mService.setServiceInterface(this)
        return mContentView
    }

    private fun showDatePickerDialog(message: String, textView: TextView?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            showToast()

            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            calendar.clear()
            calendar.timeInMillis = MaterialDatePicker.todayInUtcMilliseconds()
            calendar.set(Calendar.MONTH, Calendar.OCTOBER)
            calendar.set(Calendar.YEAR, 2020)
            val constraints = CalendarConstraints.Builder()
            constraints.setStart(calendar.timeInMillis)
            constraints.setValidator(DateValidatorPointBackward.now())

            val dateRangePicker = MaterialDatePicker.Builder.datePicker().setTitleText(message).setCalendarConstraints(constraints.build()).build()

            mActivity?.let { context -> dateRangePicker.show(context.supportFragmentManager, TAG) }
            dateRangePicker.addOnPositiveButtonClickListener {
                date ->
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = date
                val displayDate = "${calendar.get(Calendar.DATE)} ${getMonthOfTheWeek(calendar.get(Calendar.MONTH))}, ${getDayOfTheWeek(calendar.get(Calendar.DAY_OF_WEEK))}, ${calendar.get(Calendar.YEAR)}"
                if (textView?.id == startDateTextView?.id) {
                    startDateTextView?.text = displayDate
                    mStartDateStr = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_WEEK)}"
                    if (!isEmpty(endDateTextView?.text?.toString())) getTxnList()
                }
                else {
                    endDateTextView?.text = displayDate
                    mEndDateStr = "${calendar.get(Calendar.YEAR)}-${calendar.get(Calendar.MONTH) + 1}-${calendar.get(Calendar.DAY_OF_WEEK)}"
                    if (!isEmpty(startDateTextView?.text?.toString())) getTxnList()
                }
            }
            dateRangePicker.addOnNegativeButtonClickListener { showToast("No") }
        }
    }

    private fun getTxnList() {
        showProgressDialog(mActivity)
        mService.getMyPaymentsList(mPageNumber, mStartDateStr, mEndDateStr)
    }

    override fun onMyPaymentsListResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val myPaymentList = Gson().fromJson<MyPaymentsResponse>(response.mCommonDataStr, MyPaymentsResponse::class.java)
                mPaymentList = myPaymentList?.mMyPaymentList
                setupRecyclerView()
            }
        }
    }

    private fun setupRecyclerView() {
        transactionRecyclerView?.apply {
            layoutManager = LinearLayoutManager(mActivity)
            adapter = TransactionsAdapter(mActivity, mPaymentList)
        }
    }

    override fun onMyPaymentsServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

}