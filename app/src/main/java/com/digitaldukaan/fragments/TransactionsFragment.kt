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
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.MyPaymentsItemResponse
import com.digitaldukaan.models.response.MyPaymentsResponse
import com.digitaldukaan.services.MyPaymentsService
import com.digitaldukaan.services.serviceinterface.IMyPaymentsServiceInterface
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import java.util.*
import kotlin.collections.ArrayList

class TransactionsFragment: BaseFragment(), IMyPaymentsServiceInterface {

    private var transactionRecyclerView: RecyclerView? = null
    private var amountContainer: View? = null
    private var zeroOrderContainer: View? = null
    private var startDateTextView: TextView? = null
    private var endDateTextView: TextView? = null
    private val mService: MyPaymentsService = MyPaymentsService()
    private var mStartDateStr: String? = ""
    private var mEndDateStr: String? = ""
    private var mPageNumber = 1
    private var mTxnAdapter: TransactionsAdapter? = null
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
        amountContainer = mContentView?.findViewById(R.id.amountContainer)
        zeroOrderContainer = mContentView?.findViewById(R.id.zeroOrderContainer)
        startDateTextView?.setOnClickListener { showDatePickerDialog("Select Start date", startDateTextView) }
        endDateTextView?.setOnClickListener { showDatePickerDialog("Select End Date", endDateTextView) }
        val shareButtonTextView: TextView? = mContentView?.findViewById(R.id.shareButtonTextView)
        shareButtonTextView?.setOnClickListener { shareStoreOverWhatsAppServerCall() }
        mService.setServiceInterface(this)
        return mContentView
    }

    private fun showDatePickerDialog(message: String, textView: TextView?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
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
                val todayCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                todayCalendar.timeInMillis = date
                val displayDate = getTxnDateStringFromTxnDate(todayCalendar.time)
                if (textView?.id == startDateTextView?.id) {
                    startDateTextView?.text = displayDate
                    mStartDateStr = "${todayCalendar.get(Calendar.YEAR)}-${todayCalendar.get(Calendar.MONTH) + 1}-${todayCalendar.get(Calendar.DAY_OF_WEEK)}"
                    if (!isEmpty(endDateTextView?.text?.toString())) getTxnList()
                }
                else {
                    endDateTextView?.text = displayDate
                    mEndDateStr = "${todayCalendar.get(Calendar.YEAR)}-${todayCalendar.get(Calendar.MONTH) + 1}-${todayCalendar.get(Calendar.DAY_OF_WEEK)}"
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
                val paymentList: ArrayList<MyPaymentsItemResponse>? = myPaymentList?.mMyPaymentList
                mPaymentList = paymentList
                if (isEmpty(mPaymentList)) {
                    zeroOrderContainer?.visibility = View.VISIBLE
                    amountContainer?.visibility = View.GONE
                    mTxnAdapter?.setMyPaymentsList(ArrayList())
                } else {
                    setupRecyclerView()
                }
            }
        }
    }

    private fun setupRecyclerView() {
        amountContainer?.visibility = View.VISIBLE
        zeroOrderContainer?.visibility = View.GONE
        transactionRecyclerView?.apply {
            mTxnAdapter = TransactionsAdapter(mActivity, mPaymentList)
            convertDateStringOfTransactions()
            layoutManager = LinearLayoutManager(mActivity)
            adapter = mTxnAdapter
            addItemDecoration(StickyRecyclerHeadersDecoration(mTxnAdapter))
        }
    }

    override fun onMyPaymentsServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

    private fun convertDateStringOfTransactions() {
        mPaymentList?.forEachIndexed { _, itemResponse ->
            itemResponse.updatedDate = getDateFromOrderString(itemResponse.transactionTimestamp)
            itemResponse.updatedCompleteDate = getCompleteDateFromOrderString(itemResponse.transactionTimestamp)
        }
    }

}