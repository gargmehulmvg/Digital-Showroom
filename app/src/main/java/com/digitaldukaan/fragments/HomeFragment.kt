package com.digitaldukaan.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.digitaldukaan.R
import com.digitaldukaan.adapters.OrderAdapterV2
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IOrderListItemListener
import com.digitaldukaan.models.request.OrdersRequest
import com.digitaldukaan.models.request.SearchOrdersRequest
import com.digitaldukaan.models.request.UpdateOrderRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.HomeFragmentService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface
import com.google.gson.Gson
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.layout_analytics.*
import kotlinx.android.synthetic.main.otp_verification_fragment.*

class HomeFragment : BaseFragment(), IHomeServiceInterface,
    SwipeRefreshLayout.OnRefreshListener, IOrderListItemListener {

    companion object {
        private val TAG = HomeFragment::class.simpleName
        private var mOrderPageInfoStaticData: OrderPageStaticTextResponse? = null
        private var mIsDoublePressToExit = false
        private lateinit var mHomeFragmentService: HomeFragmentService
        private var mDoubleClickToExitStr:String? = ""
        private var mFetchingOrdersStr:String? = ""
        private var mIsRecyclerViewScrolling = false
        private lateinit var orderAdapter: OrderAdapterV2
        private lateinit var linearLayoutManager: LinearLayoutManager
        private var mOrderList: ArrayList<OrderItemResponse> = ArrayList()
        private var currentItems = 0
        private var totalItems = 0
        private var scrollOutItems = 0
        private var pendingPageCount = 1
        private var completedPageCount = 1
        private var mOrderIdString = ""
        private var mMobileNumberString = ""
        private var mIsMorePendingOrderAvailable = false
        private var mIsMoreCompletedOrderAvailable = false

        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mHomeFragmentService = HomeFragmentService()
        mHomeFragmentService.setHomeFragmentServiceListener(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.home_fragment, container, false)
        if (!askContactPermission()) if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
            showProgressDialog(mActivity)
            mHomeFragmentService.getOrderPageInfo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
            mHomeFragmentService.getAnalyticsData(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
        }
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateNavigationBarState(R.id.menuHome)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        showBottomNavigationView(false)
        swipeRefreshLayout.setOnRefreshListener(this)
        //completedOrderTextView.text = mOrderListStaticData.completedText
        //pendingOrderTextView.text = mOrderListStaticData.pendingText
    }

    private fun fetchLatestOrders(mode: String, fetchingOrderStr: String?, page:Int = 1) {
        if (fetchingOrderStr?.isNotEmpty() == true) showProgressDialog(mActivity, fetchingOrderStr)
        val request = OrdersRequest(mode, page)
        mHomeFragmentService.getOrders(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            analyticsImageView.id -> {
                analyticsContainer.visibility = View.VISIBLE
                analyticsImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_analytics_green))
            }
            searchImageView.id -> showSearchDialog(mOrderPageInfoStaticData, mMobileNumberString, mOrderIdString)
            closeAnalyticsImageView.id -> {
                analyticsImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_analytics_black))
                analyticsContainer.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (mIsDoublePressToExit) mActivity.finish()
        showShortSnackBar(if (mDoubleClickToExitStr?.isNotEmpty() == true) mDoubleClickToExitStr else getString(R.string.msg_back_press))
        mIsDoublePressToExit = true
        Handler(Looper.getMainLooper()).postDelayed(
            { mIsDoublePressToExit = false },
            Constants.BACK_PRESS_INTERVAL
        )
        return true
    }

    override fun onUserAuthenticationResponse(authenticationUserResponse: ValidateOtpResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            saveUserDetailsInPref(authenticationUserResponse)
            if (!authenticationUserResponse.mIsSuccessStatus) showShortSnackBar(authenticationUserResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onPendingOrdersResponse(getOrderResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
            if (getOrderResponse.mIsSuccessStatus) {
                val ordersResponse = Gson().fromJson<OrdersResponse>(getOrderResponse.mCommonDataStr, OrdersResponse::class.java)
                mIsMorePendingOrderAvailable = ordersResponse.mIsNextDataAvailable
                if (ordersResponse.mOrdersList.isEmpty()) {
                    homePageWebViewLayout.visibility = View.VISIBLE
                    orderLayout.visibility = View.GONE
                    swipeRefreshLayout.isEnabled = false
                } else {
                    homePageWebViewLayout.visibility = View.GONE
                    orderLayout.visibility = View.VISIBLE
                    swipeRefreshLayout.isEnabled = true
                    if (pendingPageCount == 1) mOrderList.clear()
                    mOrderList.addAll(ordersResponse.mOrdersList)
                    convertDateStringOfOrders(mOrderList)
                    orderAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun setupHomePageWebView(webViewUrl: String) {
        homePageWebView.apply {
            webViewClient = CommonWebViewFragment.WebViewController()
            settings.javaScriptEnabled = true
            Log.d(TAG, "setupHomePageWebView: $url")
            val url = webViewUrl + "?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&" +
                    "storeName=${getStringDataFromSharedPref(Constants.STORE_NAME)}" + "&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"
            loadUrl(url)
        }
    }

    override fun onCompletedOrdersResponse(getOrderResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
            if (getOrderResponse.mIsSuccessStatus) {
                val ordersResponse = Gson().fromJson<OrdersResponse>(getOrderResponse.mCommonDataStr, OrdersResponse::class.java)
                mIsMoreCompletedOrderAvailable = ordersResponse.mIsNextDataAvailable
                if (ordersResponse?.mOrdersList?.isNotEmpty() == true) {
                    mOrderList.addAll(ordersResponse.mOrdersList)
                    orderAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    override fun onAnalyticsDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val analyticsResponse = Gson().fromJson<AnalyticsResponse>(commonResponse.mCommonDataStr, AnalyticsResponse::class.java)
                todaySaleValue.text = analyticsResponse?.today?.totalCount.toString()
                amountValue.text = "${analyticsResponse.analyticsStaticData?.textRuppeeSymbol} ${analyticsResponse?.today?.totalAmount}"
                weekSaleValue.text = analyticsResponse?.thisWeek?.totalCount.toString()
                weekAmountValue.text = "${analyticsResponse.analyticsStaticData?.textRuppeeSymbol} ${analyticsResponse?.thisWeek?.totalAmount}"
                analyticsResponse?.analyticsStaticData?.run {
                    todaySaleHeading.text = textTodaySale
                    amountHeading.text = textTodayAmount
                    weekSaleHeading.text = textWeekSale
                    weekAmountHeading.text = textWeekAmount
                }
            }
        }
    }

    override fun onOrderPageInfoResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val orderPageInfoResponse = Gson().fromJson<OrderPageInfoResponse>(commonResponse.mCommonDataStr, OrderPageInfoResponse::class.java)
                orderPageInfoResponse?.run {
                    mOrderPageInfoStaticData = mOrderPageStaticText?.run {
                        mFetchingOrdersStr = fetching_orders
                        mDoubleClickToExitStr = msg_double_click_to_exit
                        appTitleTextView.text = heading_order_page
                        this
                    }
                    StaticInstances.sOrderPageInfoStaticData = mOrderPageInfoStaticData
                    if (mIsZeroOrder.mIsActive) {
                        homePageWebViewLayout.visibility = View.VISIBLE
                        orderLayout.visibility = View.GONE
                        setupHomePageWebView(mIsZeroOrder.mUrl)
                    } else {
                        homePageWebViewLayout.visibility = View.GONE
                        orderLayout.visibility = View.VISIBLE
                        fetchLatestOrders(Constants.MODE_PENDING, mFetchingOrdersStr, pendingPageCount)
                        ordersRecyclerView.apply {
                            orderAdapter = OrderAdapterV2(mActivity, mOrderList)
                            orderAdapter.setCheckBoxListener(this@HomeFragment)
                            linearLayoutManager = LinearLayoutManager(mActivity)
                            layoutManager = linearLayoutManager
                            adapter = orderAdapter
                            addItemDecoration(StickyRecyclerHeadersDecoration(orderAdapter))
                            addOnScrollListener(object : RecyclerView.OnScrollListener() {

                                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                                    super.onScrollStateChanged(recyclerView, newState)
                                    if (AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == newState) mIsRecyclerViewScrolling = true
                                }

                                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                                    super.onScrolled(recyclerView, dx, dy)
                                    currentItems = linearLayoutManager.childCount
                                    totalItems = orderAdapter.itemCount
                                    scrollOutItems = linearLayoutManager.findFirstVisibleItemPosition()
                                    if (mIsRecyclerViewScrolling && (currentItems + scrollOutItems == totalItems)) {
                                        mIsRecyclerViewScrolling = false
                                        if (mIsMorePendingOrderAvailable) {
                                            pendingPageCount++
                                            fetchLatestOrders(Constants.MODE_PENDING, mFetchingOrdersStr, pendingPageCount)
                                        } else if (completedPageCount == 1) {
                                            fetchLatestOrders(Constants.MODE_COMPLETED, mFetchingOrdersStr, completedPageCount)
                                            completedPageCount++
                                        }
                                    }
                                }
                            })
                        }
                    }
                    if (mIsHelpOrder.mIsActive) {
                        helpImageView.visibility = View.VISIBLE
                        helpImageView.setOnClickListener { openWebViewFragmentV2(this@HomeFragment, getString(R.string.help), mIsHelpOrder.mUrl, Constants.SETTINGS) }
                    }
                    analyticsImageView.visibility = if (mIsAnalyticsOrder) View.VISIBLE else View.GONE
                    searchImageView.visibility = if (mIsSearchOrder) View.VISIBLE else View.GONE
                }
            }
        }
    }

    override fun onSearchOrdersResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
            if (commonResponse.mIsSuccessStatus) {
                val ordersResponse = Gson().fromJson<OrdersResponse>(commonResponse.mCommonDataStr, OrdersResponse::class.java)
                if (ordersResponse?.mOrdersList?.isNotEmpty() == true) launchFragment(SearchOrdersFragment.newInstance(mOrderIdString, mMobileNumberString, ordersResponse?.mOrdersList), true)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onOrdersUpdatedStatusResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
            if (commonResponse.mIsSuccessStatus) {
                onRefresh()
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onHomePageException(e: Exception) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
            exceptionHandlingForAPIResponse(e)
        }
    }

    private fun saveUserDetailsInPref(validateOtpResponse: ValidateOtpResponse) {
        storeStringDataInSharedPref(Constants.USER_AUTH_TOKEN, validateOtpResponse.mUserAuthToken)
        storeStringDataInSharedPref(Constants.USER_MOBILE_NUMBER, validateOtpResponse.mUserPhoneNumber)
        validateOtpResponse.mStore?.run {
            storeStringDataInSharedPref(Constants.STORE_ID, storeId.toString())
            storeStringDataInSharedPref(Constants.STORE_NAME, storeInfo.name)
            if (this.storeInfo.logoImage?.isNotEmpty() == true) StaticInstances.sIsStoreImageUploaded = true
        }
    }

    override fun onOTPVerificationErrorResponse(validateOtpErrorResponse: ValidateOtpErrorResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            otpEditText.clearOTP()
            showToast(validateOtpErrorResponse.mMessage)
        }
    }

    override fun onRefresh() {
        completedPageCount = 1
        pendingPageCount = 1
        fetchLatestOrders(Constants.MODE_PENDING, mFetchingOrdersStr, pendingPageCount)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "$TAG onRequestPermissionResult")
        if (requestCode == Constants.CONTACT_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    CoroutineScopeUtils().runTaskOnCoroutineBackground {
                        getContactsFromStorage2(mActivity)
                    }
                    if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
                        mHomeFragmentService.getOrderPageInfo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
                        mHomeFragmentService.getAnalyticsData(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
                    }
                }
                else -> showShortSnackBar("Permission was denied")
            }
        }
    }

    override fun onSearchDialogContinueButtonClicked(inputOrderId: String, inputMobileNumber: String) {
        mOrderIdString = inputOrderId
        mMobileNumberString = inputMobileNumber
        val request = SearchOrdersRequest(if (mOrderIdString.isNotEmpty()) mOrderIdString.toLong() else 0, mMobileNumberString, 1)
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog()
        showProgressDialog(mActivity)
        mHomeFragmentService.getSearchOrders(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
    }

    override fun onOrderCheckBoxChanged(isChecked: Boolean, item: OrderItemResponse?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            showToast(isChecked.toString())
            if (isChecked) openDontShowDialog(item)
        }
    }

    override fun onOrderItemCLickChanged(item: OrderItemResponse?) {
        launchFragment(OrderDetailFragment.newInstance(item?.orderId.toString()), true)
    }

    override fun onDontShowDialogPositiveButtonClicked(item: OrderItemResponse?) {
        val request = UpdateOrderRequest(item?.orderId?.toLong(), Constants.StatusSeenByMerchant.toLong())
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
            showProgressDialog(mActivity)
            mHomeFragmentService.updateOrderStatus(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
        }
    }
}