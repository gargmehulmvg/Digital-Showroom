package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.widget.NestedScrollView
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
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import ru.slybeaver.slycalendarview.SlyCalendarDialog
import java.util.*
import kotlin.collections.ArrayList

class TransactionsFragment: BaseFragment(), IMyPaymentsServiceInterface {

    private var transactionRecyclerView: RecyclerView? = null
    private var amountContainer: View? = null
    private var zeroOrderContainer: View? = null
    private var startDateTextView: TextView? = null
    private val mService: MyPaymentsService = MyPaymentsService()
    private var mStartDateStr: String? = ""
    private var mEndDateStr: String? = ""
    private var mPageNumber = 1
    private var mIsMoreTransactionsAvailable = false
    private var mIsDateSelectionDone = false
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
        amountContainer = mContentView?.findViewById(R.id.amountContainer)
        zeroOrderContainer = mContentView?.findViewById(R.id.zeroOrderContainer)
        startDateTextView?.setOnClickListener { showDatePickerDialog() }
        val shareButtonTextView: TextView? = mContentView?.findViewById(R.id.shareButtonTextView)
        shareButtonTextView?.setOnClickListener { shareStoreOverWhatsAppServerCall() }
        mService.setServiceInterface(this)
        applyPagination()
        return mContentView
    }

    private fun applyPagination() {
        setupRecyclerView()
        val scrollView: NestedScrollView? = mContentView?.findViewById(R.id.scrollView)
        scrollView?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY == (v.getChildAt(0).measuredHeight - v.measuredHeight)) {
                if (mIsMoreTransactionsAvailable) {
                    mPageNumber++
                    mIsDateSelectionDone = false
                    getTxnList()
                }
            }
        })
    }

    private fun showDatePickerDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val today = Date(MaterialDatePicker.todayInUtcMilliseconds())
            mActivity?.let { context ->
                SlyCalendarDialog()
                    .setEndDate(today)
                    .setSelectedColor(context.getColor(R.color.black))
                    .setHeaderColor(context.getColor(R.color.black))
                    .setHeaderTextColor(context.getColor(R.color.white))
                    .setSelectedTextColor(context.getColor(R.color.white))
                    .setSingle(false)
                    .setFirstMonday(true)
                    .setCallback(object : SlyCalendarDialog.Callback {

                        override fun onCancelled() {
                            mActivity?.onBackPressed()
                        }

                        override fun onDataSelected(firstDate: Calendar?, secondDate: Calendar?, hours: Int, minutes: Int) {
                            Log.d(TAG, "onDataSelected: firstDate ${firstDate?.time}")
                            Log.d(TAG, "onDataSelected: secondDate:: ${secondDate?.time}")
                            val secondDateCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            secondDateCalendar.timeInMillis = secondDate?.timeInMillis ?: 0
                            mEndDateStr = "${secondDateCalendar.get(Calendar.YEAR)}-${secondDateCalendar.get(Calendar.MONTH) + 1}-${secondDateCalendar.get(Calendar.DATE)}"
                            val firstDateCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            firstDateCalendar.timeInMillis = firstDate?.timeInMillis ?: (secondDate?.timeInMillis ?: 0)
                            mStartDateStr = "${firstDateCalendar.get(Calendar.YEAR)}-${firstDateCalendar.get(Calendar.MONTH) + 1}-${firstDateCalendar.get(Calendar.DATE)}"
                            val displayDate = "${getTxnDateStringFromTxnDate(firstDateCalendar.time)} - ${getTxnDateStringFromTxnDate(secondDateCalendar.time)}"
                            startDateTextView?.text = displayDate
                            mIsDateSelectionDone = true
                            getTxnList()
                        }
                    })
                    .show(context.supportFragmentManager, TAG)
            }
        }
    }

    private fun getTxnList() {
        showProgressDialog(mActivity)
        mService.getTransactionsList(mPageNumber, mStartDateStr, mEndDateStr)
    }

    override fun onGetTransactionsListResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val myPaymentList = Gson().fromJson<MyPaymentsResponse>(response.mCommonDataStr, MyPaymentsResponse::class.java)
                val paymentList: ArrayList<MyPaymentsItemResponse>? = myPaymentList?.mMyPaymentList
                if (mIsDateSelectionDone) if (isEmpty(paymentList)) mPaymentList?.clear()
                paymentList?.let { mPaymentList?.addAll(it) }
                mIsMoreTransactionsAvailable = myPaymentList?.mIsNextPage ?: false
                if (isEmpty(mPaymentList)) {
                    zeroOrderContainer?.visibility = View.VISIBLE
                    amountContainer?.visibility = View.GONE
                    mTxnAdapter?.setMyPaymentsList(ArrayList())
                } else {
                    amountContainer?.visibility = View.VISIBLE
                    zeroOrderContainer?.visibility = View.GONE
                    convertDateStringOfTransactions()
                    mTxnAdapter?.setMyPaymentsList(mPaymentList)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        convertDateStringOfTransactions()
        transactionRecyclerView?.apply {
            mTxnAdapter = TransactionsAdapter(mActivity, mPaymentList)
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