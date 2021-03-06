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
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.MyPaymentsService
import com.digitaldukaan.services.serviceinterface.IMyPaymentsServiceInterface
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import kotlinx.android.synthetic.main.layout_transactions.*
import ru.slybeaver.slycalendarview.SlyCalendarDialog
import java.util.*
import kotlin.collections.ArrayList

class TransactionsFragment: BaseFragment(), IMyPaymentsServiceInterface, ITransactionItemClicked {

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
    private var mIsPrevDateSelected = false
    private var myPaymentPageInfoResponse: MyPaymentsPageInfoResponse? = null
    private var mPaymentList: ArrayList<MyPaymentsItemResponse>? = ArrayList()

    companion object {
        fun newInstance(): TransactionsFragment = TransactionsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "TransactionsFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_transactions, container, false)
        transactionRecyclerView = mContentView?.findViewById(R.id.transactionRecyclerView)
        startDateTextView = mContentView?.findViewById(R.id.startDateTextView)
        amountContainer = mContentView?.findViewById(R.id.amountContainer)
        zeroOrderContainer = mContentView?.findViewById(R.id.zeroOrderContainer)
        startDateTextView?.setOnClickListener { showDatePickerDialog() }
        val shareButtonTextView: TextView? = mContentView?.findViewById(R.id.shareButtonTextView)
        shareButtonTextView?.setOnClickListener {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_STORE_SHARE,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    AFInAppEventParameterName.IS_ORDER_PAGE to AFInAppEventParameterName.TRUE
                )
            )
            if (StaticInstances.sIsShareStoreLocked) {
                getLockedStoreShareDataServerCall(Constants.MODE_SHARE_STORE)
            } else shareStoreOverWhatsAppServerCall()
        }
        mService.setServiceInterface(this)
        applyPagination()
        mService.getMyPaymentPageInfo()
        val startDate = PrefsManager.getStringDataFromSharedPref(PrefsManager.KEY_TXN_START_DATE)
        val endDate = PrefsManager.getStringDataFromSharedPref(PrefsManager.KEY_TXN_END_DATE)
        if (isEmpty(startDate) && isEmpty(endDate)) {
            mIsPrevDateSelected = false
            showDatePickerDialog()
        } else {
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
            mPageNumber = 1
            getTxnList()
        }
        val noTxnImageView: ImageView? = mContentView?.findViewById(R.id.noTxnImageView)
        mActivity?.let { context -> noTxnImageView?.let { view -> Glide.with(context).load("https://cdn.dotpe.in/kiranaStatic/image/zero_orders.png").into(view) } }
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
                            PrefsManager.storeStringDataInSharedPref(PrefsManager.KEY_TXN_START_DATE, "${firstDateCalendar.timeInMillis}")
                            PrefsManager.storeStringDataInSharedPref(PrefsManager.KEY_TXN_END_DATE, "${secondDateCalendar.timeInMillis}")
                            getTxnList()
                            AppEventsManager.pushAppEvents(
                                eventName = AFInAppEventType.EVENT_SET_ORDER_DATE_SELECTION,
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

    private fun getTxnList() {
        showCancellableProgressDialog(mActivity)
        val request = TransactionRequest(mPageNumber, mStartDateStr, mEndDateStr, Constants.MODE_ORDERS)
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
                    val settleAmount = "??? ${myPaymentList?.mSettledAmount}"
                    amountSettledValueTextView?.text = settleAmount
                    val unSettleAmount = "??? ${myPaymentList?.mUnsettledAmount}"
                    amountToSettledValueTextView?.text = unSettleAmount
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

    override fun onGetMyPaymentPageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                myPaymentPageInfoResponse = Gson().fromJson<MyPaymentsPageInfoResponse>(response.mCommonDataStr, MyPaymentsPageInfoResponse::class.java)
                StaticInstances.sMyPaymentPageInfoResponse = myPaymentPageInfoResponse
                shareButtonTextView?.text = myPaymentPageInfoResponse?.text_share
                shareYourStoreTextView?.text = myPaymentPageInfoResponse?.message_share_your_store_now_to_get_orders
                noSettlementForSelectedDateTextView?.text = myPaymentPageInfoResponse?.message_order_no_payment_received
                amountSettledTextView?.text = myPaymentPageInfoResponse?.text_amount_settled
                amountToSettledTextView?.text = myPaymentPageInfoResponse?.text_amount_to_settle
                cashTxnNotShownTextView?.text = myPaymentPageInfoResponse?.message_cash_transactions_are_not_shown
                mActivity?.let { context ->
                    val tickGreenImageView: ImageView? = mContentView?.findViewById(R.id.tickGreenImageView)
                    tickGreenImageView?.let { view -> Glide.with(context).load(myPaymentPageInfoResponse?.amount_settle_cdn).into(view) }
                    val waitingImageView: ImageView? = mContentView?.findViewById(R.id.waitingImageView)
                    waitingImageView?.let { view -> Glide.with(context).load(myPaymentPageInfoResponse?.amount_to_settle_cdn).into(view) }
                }
            }
        }
    }

    private fun setupRecyclerView() {
        convertDateStringOfTransactions()
        transactionRecyclerView?.apply {
            mTxnAdapter = TransactionsAdapter(mActivity, mPaymentList, Constants.MODE_ORDERS, this@TransactionsFragment)
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

    override fun onTransactionItemClicked(idStr: String?) {
        if (isEmpty(idStr)) {
            showShortSnackBar(getString(R.string.txn_id_is_blank), true, R.drawable.ic_close_red)
            return
        }
        getTransactionDetailBottomSheet(idStr, AFInAppEventParameterName.PAYMENT_ORDERS)
    }

    override fun onLockedStoreShareSuccessResponse(lockedShareResponse: LockedStoreShareResponse) = showLockedStoreShareBottomSheet(lockedShareResponse)

}