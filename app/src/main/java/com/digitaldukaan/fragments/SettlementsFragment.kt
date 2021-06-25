package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.adapters.TransactionsAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.ITransactionItemClicked
import com.digitaldukaan.models.request.TransactionRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.MyPaymentsItemResponse
import com.digitaldukaan.models.response.MyPaymentsResponse
import com.digitaldukaan.services.MyPaymentsService
import com.digitaldukaan.services.serviceinterface.IMyPaymentsServiceInterface
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.gson.Gson
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import kotlinx.android.synthetic.main.layout_settlements.*
import ru.slybeaver.slycalendarview.SlyCalendarDialog
import java.util.*
import kotlin.collections.ArrayList

class SettlementsFragment : BaseFragment(), IMyPaymentsServiceInterface, ITransactionItemClicked {

    private var settlementsRecyclerView: RecyclerView? = null
    private var amountContainer: View? = null
    private var zeroOrderContainer: View? = null
    private var startDateTextView: TextView? = null
    private var shareButtonTextView: TextView? = null
    private val mService: MyPaymentsService = MyPaymentsService()
    private var mStartDateStr: String? = ""
    private var mEndDateStr: String? = ""
    private var mPageNumber = 1
    private var mIsDateSelectionDone = false
    private var mIsPrevDateSelected = false
    private var mIsMoreTransactionsAvailable = false
    private var mTxnAdapter: TransactionsAdapter? = null
    private var mPaymentList: ArrayList<MyPaymentsItemResponse>? = ArrayList()

    companion object {
        private const val TAG = "SettlementsFragment"
        fun newInstance(): SettlementsFragment = SettlementsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_settlements, container, false)
        settlementsRecyclerView = mContentView?.findViewById(R.id.transactionRecyclerView)
        shareButtonTextView = mContentView?.findViewById(R.id.shareButtonTextView)
        startDateTextView = mContentView?.findViewById(R.id.startDateTextView)
        amountContainer = mContentView?.findViewById(R.id.amountContainer)
        zeroOrderContainer = mContentView?.findViewById(R.id.zeroOrderContainer)
        shareButtonTextView?.setOnClickListener {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_STORE_SHARE,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    AFInAppEventParameterName.IS_SETTLEMENT_PAGE to AFInAppEventParameterName.TRUE
                )
            )
            shareStoreOverWhatsAppServerCall()
        }
        startDateTextView?.setOnClickListener { showDatePickerDialog() }
        mService.setServiceInterface(this)
        applyPagination()
        val noTxnImageView: ImageView? = mContentView?.findViewById(R.id.noTxnImageView)
        mActivity?.let { context -> noTxnImageView?.let { view -> Glide.with(context).load("https://cdn.dotpe.in/kiranaStatic/image/zero_settlement.png").into(view) } }
        return mContentView
    }

    private fun applyPagination() {
        setupRecyclerView()
        val scrollView: NestedScrollView? = mContentView?.findViewById(R.id.nestedScrollView)
        scrollView?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY == (v.getChildAt(0).measuredHeight - v.measuredHeight)) {
                if (mIsMoreTransactionsAvailable) {
                    mPageNumber++
                    mIsDateSelectionDone = false
                    getSettlementsList()
                }
            }
        })
    }

    private fun setupCalenderView() {
        mService.setServiceInterface(this)
        val startDate = PrefsManager.getStringDataFromSharedPref(PrefsManager.KEY_SETTLEMENT_START_DATE)
        val endDate = PrefsManager.getStringDataFromSharedPref(PrefsManager.KEY_SETTLEMENT_END_DATE)
        if (isEmpty(startDate) && isEmpty(endDate)) {
            mIsPrevDateSelected = false
            showDatePickerDialog()
        } else {
            initiateSettlementServerCall(endDate, startDate)
        }
    }

    private fun initiateSettlementServerCall(endDate: String, startDate: String) {
        mIsPrevDateSelected = true
        val secondDateCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        secondDateCalendar.timeInMillis = endDate.toLong()
        mEndDateStr = "${secondDateCalendar.get(Calendar.YEAR)}-${secondDateCalendar.get(Calendar.MONTH) + 1}-${secondDateCalendar.get(Calendar.DATE)}"
        val firstDateCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        firstDateCalendar.timeInMillis = startDate.toLong()
        mStartDateStr = "${firstDateCalendar.get(Calendar.YEAR)}-${firstDateCalendar.get(Calendar.MONTH) + 1}-${firstDateCalendar.get(Calendar.DATE)}"
        val displayDate = "${getTxnDateStringFromTxnDate(firstDateCalendar.time)} - ${getTxnDateStringFromTxnDate(secondDateCalendar.time)}"
        startDateTextView?.text = displayDate
        mIsDateSelectionDone = true
        getSettlementsList()
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
                            if (!mIsPrevDateSelected) mActivity?.onBackPressed()
                        }

                        override fun onDataSelected(firstDate: Calendar?, secondDate: Calendar?, hours: Int, minutes: Int) {
                            Log.d(TAG, "onDataSelected: firstDate ${firstDate?.time}")
                            Log.d(TAG, "onDataSelected: secondDate:: ${secondDate?.time}")
                            val secondDateCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            secondDateCalendar.timeInMillis = secondDate?.timeInMillis ?: firstDate?.timeInMillis ?: 0
                            mEndDateStr = "${secondDateCalendar.get(Calendar.YEAR)}-${secondDateCalendar.get(Calendar.MONTH) + 1}-${secondDateCalendar.get(Calendar.DATE)}"
                            val firstDateCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                            firstDateCalendar.timeInMillis = firstDate?.timeInMillis ?: (secondDate?.timeInMillis ?: 0)
                            mStartDateStr = "${firstDateCalendar.get(Calendar.YEAR)}-${firstDateCalendar.get(Calendar.MONTH) + 1}-${firstDateCalendar.get(Calendar.DATE)}"
                            val displayDate = "${getTxnDateStringFromTxnDate(firstDateCalendar.time)} - ${getTxnDateStringFromTxnDate(secondDateCalendar.time)}"
                            startDateTextView?.text = displayDate
                            mIsDateSelectionDone = true
                            PrefsManager.storeStringDataInSharedPref(PrefsManager.KEY_SETTLEMENT_START_DATE, "${firstDateCalendar.timeInMillis}")
                            PrefsManager.storeStringDataInSharedPref(PrefsManager.KEY_SETTLEMENT_END_DATE, "${secondDateCalendar.timeInMillis}")
                            mPageNumber = 1
                            getSettlementsList()
                            AppEventsManager.pushAppEvents(
                                eventName = AFInAppEventType.EVENT_SET_SETTLEMENTS_DATE_SELECTION,
                                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                data = mapOf(
                                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                    AFInAppEventParameterName.DATE_FROM to mStartDateStr,
                                    AFInAppEventParameterName.DATE_TO to mEndDateStr
                                )
                            )
                        }
                    })
                    .show(context.supportFragmentManager, TAG)
            }
        }
    }

    private fun getSettlementsList() {
        showProgressDialog(mActivity)
        val request = TransactionRequest(mPageNumber, mStartDateStr, mEndDateStr, Constants.MODE_SETTLEMENTS)
        mService.getTransactionsList(request)
    }

    override fun onGetTransactionsListResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                stopProgress()
                if (response.mIsSuccessStatus) {
                    val myPaymentList = Gson().fromJson<MyPaymentsResponse>(response.mCommonDataStr, MyPaymentsResponse::class.java)
                    val paymentList: ArrayList<MyPaymentsItemResponse>? = myPaymentList?.mMyPaymentList
                    if (mIsDateSelectionDone) mPaymentList?.clear()
                    paymentList?.let { mPaymentList?.addAll(it) }
                    mIsMoreTransactionsAvailable = myPaymentList?.mIsNextPage ?: false
                    val settleAmount = "₹ ${myPaymentList?.mSettledAmount}"
                    amountSettledValueTextView?.text = settleAmount
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
            } catch (e: Exception) {
                Log.e(TAG, "onGetTransactionsListResponse: ${e.message}", e)
            }
        }
    }

    private fun setupRecyclerView() {
        convertDateStringOfTransactions()
        settlementsRecyclerView?.apply {
            mTxnAdapter = TransactionsAdapter(mActivity, mPaymentList, Constants.MODE_SETTLEMENTS, this@SettlementsFragment)
            layoutManager = LinearLayoutManager(mActivity)
            adapter = mTxnAdapter
            addItemDecoration(StickyRecyclerHeadersDecoration(mTxnAdapter))
        }
    }

    private fun convertDateStringOfTransactions() {
        mPaymentList?.forEachIndexed { _, itemResponse ->
            itemResponse.updatedDate = getDateFromOrderString(itemResponse.transactionTimestamp)
            itemResponse.updatedCompleteDate =
                getCompleteDateFromOrderString(itemResponse.transactionTimestamp)
        }
    }

    override fun onMyPaymentsServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onGetMyPaymentPageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
        }
    }

    override fun onTransactionItemClicked(idStr: String?) {
        if (isEmpty(idStr)) {
            showShortSnackBar(getString(R.string.txn_id_is_blank), true, R.drawable.ic_close_red)
            return
        }
        getTransactionDetailBottomSheet(idStr, AFInAppEventParameterName.SETTLEMENTS)
    }

    override fun onMyPaymentFragmentTabChanged() {
        super.onMyPaymentFragmentTabChanged()
        setupCalenderView()
        if (null != StaticInstances.sMyPaymentPageInfoResponse) {
            StaticInstances.sMyPaymentPageInfoResponse?.run {
                shareButtonTextView?.text = text_share
                noSettlementForSelectedDateTextView?.text = message_settlements_no_payment_received
                shareYourStoreTextView?.text = message_share_your_store_now_to_get_orders
                amountSettledTextView?.text = text_amount_settled
            }
        }
    }

}