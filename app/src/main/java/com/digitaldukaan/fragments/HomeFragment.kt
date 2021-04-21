package com.digitaldukaan.fragments

import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.digitaldukaan.R
import com.digitaldukaan.adapters.OrderAdapterV2
import com.digitaldukaan.adapters.OrderPageBannerAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IOrderListItemListener
import com.digitaldukaan.models.request.CompleteOrderRequest
import com.digitaldukaan.models.request.OrdersRequest
import com.digitaldukaan.models.request.SearchOrdersRequest
import com.digitaldukaan.models.request.UpdateOrderStatusRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.HomeFragmentService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
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
        private var mDoubleClickToExitStr: String? = ""
        private var mFetchingOrdersStr: String? = ""
        private lateinit var orderAdapter: OrderAdapterV2
        private lateinit var completedOrderAdapter: OrderAdapterV2
        private lateinit var linearLayoutManager: LinearLayoutManager
        private var mOrderList: ArrayList<OrderItemResponse> = ArrayList()
        private var mCompletedOrderList: ArrayList<OrderItemResponse> = ArrayList()
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
        WebViewBridge.mWebViewListener = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.home_fragment, container, false)
        if (!askContactPermission()) if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
            mHomeFragmentService.getOrderPageInfo()
            mHomeFragmentService.getAnalyticsData()
        }
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateNavigationBarState(R.id.menuHome)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        hideBottomNavigationView(false)
        swipeRefreshLayout.setOnRefreshListener(this)
        orderAdapter = OrderAdapterV2(mActivity, mOrderList)
        completedOrderAdapter = OrderAdapterV2(mActivity, mCompletedOrderList)
        pendingPageCount = 1
        completedPageCount = 1
    }

    private fun fetchLatestOrders(mode: String, fetchingOrderStr: String?, page: Int = 1) {
        if (fetchingOrderStr?.isNotEmpty() == true) showCancellableProgressDialog(
            mActivity,
            fetchingOrderStr
        )
        val request = OrdersRequest(mode, page)
        mHomeFragmentService.getOrders(
            getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN),
            request
        )
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            analyticsImageView.id -> {
                analyticsContainer.visibility = View.VISIBLE
                analyticsImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        mActivity,
                        R.drawable.ic_analytics_green
                    )
                )
            }
            takeOrderTextView.id -> showTakeOrderBottomSheet()
            searchImageView.id -> showSearchDialog(
                mOrderPageInfoStaticData,
                mMobileNumberString,
                mOrderIdString
            )
            closeAnalyticsImageView.id -> {
                analyticsImageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        mActivity,
                        R.drawable.ic_analytics_black
                    )
                )
                analyticsContainer.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (mIsDoublePressToExit) mActivity.finish()
        showShortSnackBar(
            if (mDoubleClickToExitStr?.isNotEmpty() == true) mDoubleClickToExitStr else getString(
                R.string.msg_back_press
            )
        )
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
            if (!authenticationUserResponse.mIsSuccessStatus) showShortSnackBar(
                authenticationUserResponse.mMessage,
                true,
                R.drawable.ic_close_red
            )
        }
    }

    private fun setupHomePageWebView(webViewUrl: String) {
        homePageWebView.apply {
            webViewClient = CommonWebViewFragment.WebViewController()
            settings.javaScriptEnabled = true
            val url = webViewUrl + "?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&" +
                    "storeName=${getStringDataFromSharedPref(Constants.STORE_NAME)}" + "&token=${getStringDataFromSharedPref(
                Constants.USER_AUTH_TOKEN
            )}"
            Log.d(TAG, "setupHomePageWebView: $url")
            loadUrl(url)
        }
    }

    override fun onPendingOrdersResponse(getOrderResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
            if (getOrderResponse.mIsSuccessStatus) {
                val ordersResponse = Gson().fromJson<OrdersResponse>(
                    getOrderResponse.mCommonDataStr,
                    OrdersResponse::class.java
                )
                mIsMorePendingOrderAvailable = ordersResponse.mIsNextDataAvailable
                if (pendingPageCount == 1) mOrderList.clear()
                mOrderList.addAll(ordersResponse.mOrdersList)
                convertDateStringOfOrders(mOrderList)
                orderAdapter.notifyDataSetChanged()
                if (mIsMorePendingOrderAvailable) {
                    ++pendingPageCount
                    fetchLatestOrders(Constants.MODE_PENDING, "", pendingPageCount)
                } else {
                    completedPageCount = 1
                    fetchLatestOrders(Constants.MODE_COMPLETED, "", completedPageCount)
                }
            }
        }
    }

    override fun onCompletedOrdersResponse(getOrderResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
            if (getOrderResponse.mIsSuccessStatus) {
                val ordersResponse = Gson().fromJson<OrdersResponse>(
                    getOrderResponse.mCommonDataStr,
                    OrdersResponse::class.java
                )
                mIsMoreCompletedOrderAvailable = ordersResponse.mIsNextDataAvailable
                if (completedPageCount == 1) mCompletedOrderList.clear()
                mCompletedOrderList.addAll(ordersResponse.mOrdersList)
                convertDateStringOfOrders(mCompletedOrderList)
                completedOrderAdapter.notifyDataSetChanged()
                if (mIsMoreCompletedOrderAvailable) {
                    ++completedPageCount
                    fetchLatestOrders(Constants.MODE_COMPLETED, "", pendingPageCount)
                }

            }
        }
    }

    override fun onAnalyticsDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val analyticsResponse = Gson().fromJson<AnalyticsResponse>(
                    commonResponse.mCommonDataStr,
                    AnalyticsResponse::class.java
                )
                todaySaleValue.text = analyticsResponse?.today?.totalCount.toString()
                amountValue.text =
                    "${analyticsResponse.analyticsStaticData?.textRuppeeSymbol} ${analyticsResponse?.today?.totalAmount}"
                weekSaleValue.text = analyticsResponse?.thisWeek?.totalCount.toString()
                weekAmountValue.text =
                    "${analyticsResponse.analyticsStaticData?.textRuppeeSymbol} ${analyticsResponse?.thisWeek?.totalAmount}"
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
                val orderPageInfoResponse = Gson().fromJson<OrderPageInfoResponse>(
                    commonResponse.mCommonDataStr,
                    OrderPageInfoResponse::class.java
                )
                orderPageInfoResponse?.run {
                    mOrderPageInfoStaticData = mOrderPageStaticText?.run {
                        mFetchingOrdersStr = fetching_orders
                        mDoubleClickToExitStr = msg_double_click_to_exit
                        appTitleTextView.text = heading_order_page
                        this
                    }
                    StaticInstances.sOrderPageInfoStaticData = mOrderPageInfoStaticData
                    pendingOrderTextView.text = mOrderPageInfoStaticData?.text_pending
                    completedOrderTextView.text = mOrderPageInfoStaticData?.text_completed
                    if (mIsZeroOrder.mIsActive) {
                        homePageWebViewLayout.visibility = View.VISIBLE
                        orderLayout.visibility = View.GONE
                        takeOrderTextView.visibility = View.GONE
                        setupHomePageWebView(mIsZeroOrder.mUrl)
                        swipeRefreshLayout.isEnabled = false
                    } else {
                        swipeRefreshLayout.isEnabled = true
                        homePageWebViewLayout.visibility = View.GONE
                        orderLayout.visibility = View.VISIBLE
                        fetchLatestOrders(
                            Constants.MODE_PENDING,
                            mFetchingOrdersStr,
                            pendingPageCount
                        )
                        ordersRecyclerView.isNestedScrollingEnabled = false
                        ordersRecyclerView.apply {
                            orderAdapter = OrderAdapterV2(mActivity, mOrderList)
                            orderAdapter.setCheckBoxListener(this@HomeFragment)
                            linearLayoutManager = LinearLayoutManager(mActivity)
                            layoutManager = linearLayoutManager
                            adapter = orderAdapter
                            addItemDecoration(StickyRecyclerHeadersDecoration(orderAdapter))
                        }
                        completedOrdersRecyclerView.isNestedScrollingEnabled = false
                        completedOrdersRecyclerView.apply {
                            completedOrderAdapter = OrderAdapterV2(mActivity, mCompletedOrderList)
                            completedOrderAdapter.setCheckBoxListener(this@HomeFragment)
                            linearLayoutManager = LinearLayoutManager(mActivity)
                            layoutManager = linearLayoutManager
                            adapter = completedOrderAdapter
                            addItemDecoration(StickyRecyclerHeadersDecoration(completedOrderAdapter))
                        }
                        bannerRecyclerView.apply {
                            linearLayoutManager = LinearLayoutManager(
                                mActivity,
                                LinearLayoutManager.HORIZONTAL,
                                false
                            )
                            layoutManager = linearLayoutManager
                            adapter = OrderPageBannerAdapter()
                        }
                    }
                    if (mIsHelpOrder.mIsActive) {
                        helpImageView.visibility = View.VISIBLE
                        helpImageView.setOnClickListener {
                            openWebViewFragmentV2(
                                this@HomeFragment,
                                getString(R.string.help),
                                mIsHelpOrder.mUrl,
                                Constants.SETTINGS
                            )
                        }
                    }
                    analyticsImageView.visibility =
                        if (mIsAnalyticsOrder) View.VISIBLE else View.GONE
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
                val ordersResponse = Gson().fromJson<OrdersResponse>(
                    commonResponse.mCommonDataStr,
                    OrdersResponse::class.java
                )
                if (ordersResponse?.mOrdersList?.isNotEmpty() == true) launchFragment(
                    SearchOrdersFragment.newInstance(
                        mOrderIdString,
                        mMobileNumberString,
                        ordersResponse.mOrdersList
                    ),
                    true
                )
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onOrdersUpdatedStatusResponse(commonResponse: CommonApiResponse) {
        Log.d(TAG, "onOrdersUpdatedStatusResponse: do nothing :: $commonResponse")
    }

    override fun onCompleteOrderStatusResponse(commonResponse: CommonApiResponse) {
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
        storeStringDataInSharedPref(
            Constants.USER_MOBILE_NUMBER,
            validateOtpResponse.mUserPhoneNumber
        )
        validateOtpResponse.mStore?.run {
            storeStringDataInSharedPref(Constants.STORE_ID, storeId.toString())
            storeStringDataInSharedPref(Constants.STORE_NAME, storeInfo.name)
            if (this.storeInfo.logoImage?.isNotEmpty() == true) StaticInstances.sIsStoreImageUploaded =
                true
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
                        mHomeFragmentService.getOrderPageInfo()
                        mHomeFragmentService.getAnalyticsData()
                    }
                }
                else -> showShortSnackBar("Permission was denied")
            }
        }
    }

    override fun onSearchDialogContinueButtonClicked(
        inputOrderId: String,
        inputMobileNumber: String
    ) {
        mOrderIdString = inputOrderId
        mMobileNumberString = inputMobileNumber
        val request = SearchOrdersRequest(
            if (mOrderIdString.isNotEmpty()) mOrderIdString.toLong() else 0,
            mMobileNumberString,
            1
        )
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog()
        showProgressDialog(mActivity)
        mHomeFragmentService.getSearchOrders(request)
    }

    override fun onOrderCheckBoxChanged(isChecked: Boolean, item: OrderItemResponse?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            showToast(isChecked.toString())
            if (isChecked) openDontShowDialog(item, mOrderPageInfoStaticData)
        }
    }

    override fun onOrderItemCLickChanged(item: OrderItemResponse?) {
        var isNewOrder = false
        if (item?.displayStatus == Constants.DS_NEW) {
            val request = UpdateOrderStatusRequest(
                item.orderId.toLong(),
                Constants.StatusSeenByMerchant.toLong()
            )
            mHomeFragmentService.updateOrderStatus(request)
            isNewOrder = true
        }
        launchFragment(OrderDetailFragment.newInstance(item?.orderId.toString(), isNewOrder), true)
    }

    override fun onDontShowDialogPositiveButtonClicked(item: OrderItemResponse?) {
        val request = CompleteOrderRequest(item?.orderId?.toLong())
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
            showProgressDialog(mActivity)
            mHomeFragmentService.completeOrder(request)
        }
    }

    override fun onNativeBackPressed() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.onBackPressed()
        }
    }

    override fun sendData(data: String) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            Log.d(TAG, "sendData: $data")
        }
    }

    override fun onImageSelectionResultUri(fileUri: Uri?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            launchFragment(SendBillFragment.newInstance(fileUri), true)
        }
    }

    private fun showTakeOrderBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_take_order,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            view?.run {
                val imageViewSendBill: ImageView = findViewById(R.id.imageViewSendBill)
                val takeOrderMessageTextView: TextView = findViewById(R.id.takeOrderMessageTextView)
                val createNewBillTextView: TextView = findViewById(R.id.createNewBillTextView)
                val clickBillPhotoContainer: View = findViewById(R.id.clickBillPhotoContainer)
                clickBillPhotoContainer.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    openFullCamera()
                }
            }
        }.show()
    }
}