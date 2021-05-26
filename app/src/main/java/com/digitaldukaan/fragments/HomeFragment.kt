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
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.appsflyer.AppsFlyerLib
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.OrderAdapterV2
import com.digitaldukaan.adapters.OrderPageBannerAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.interfaces.IOrderListItemListener
import com.digitaldukaan.models.dto.CleverTapProfile
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
import com.squareup.picasso.Picasso
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.layout_analytics.*
import kotlinx.android.synthetic.main.layout_common_webview_fragment.*
import org.json.JSONObject
import java.io.File


class HomeFragment : BaseFragment(), IHomeServiceInterface,
    SwipeRefreshLayout.OnRefreshListener, IOrderListItemListener {

    companion object {
        private val TAG = HomeFragment::class.simpleName
        private var mOrderPageInfoStaticData: OrderPageStaticTextResponse? = null
        private var mIsDoublePressToExit = false
        private var mHomeFragmentService: HomeFragmentService? = null
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
        private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
        private var orderPageInfoResponse: OrderPageInfoResponse? = null
        private var analyticsResponse: AnalyticsResponse? = null

        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mHomeFragmentService = HomeFragmentService()
        mHomeFragmentService?.setHomeFragmentServiceListener(this)
        AppsFlyerLib.getInstance().setCustomerUserId(PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.home_fragment, container, false)
        if (!askContactPermission()) if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
            if (orderPageInfoResponse == null) {
                mHomeFragmentService?.getOrderPageInfo()
                mHomeFragmentService?.getAnalyticsData()
            } else {
                setupOrderPageInfoUI()
                setupAnalyticsUI()
            }
        }
        mSwipeRefreshLayout = mContentView?.findViewById(R.id.swipeRefreshLayout)
        return mContentView
    }

    override fun onResume() {
        super.onResume()
        stopProgress()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateNavigationBarState(R.id.menuHome)
        ToolBarManager.getInstance()?.hideToolBar(mActivity, true)
        WebViewBridge.mWebViewListener = this
        hideBottomNavigationView(false)
        mSwipeRefreshLayout?.setOnRefreshListener(this)
        pendingPageCount = 1
        completedPageCount = 1
        mOrderList.clear()
        mCompletedOrderList.clear()
        orderLayout?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            if (scrollY == (v.getChildAt(0).measuredHeight - v.measuredHeight)) {
                when {
                    mIsMorePendingOrderAvailable -> {
                        ++pendingPageCount
                        fetchLatestOrders(Constants.MODE_PENDING, "", pendingPageCount)
                    }
                mIsMoreCompletedOrderAvailable -> {
                    ++completedPageCount
                    fetchLatestOrders(Constants.MODE_COMPLETED, "", pendingPageCount)
                }
                !mIsMoreCompletedOrderAvailable -> {
                    if (mCompletedOrderList.isEmpty()) {
                        completedPageCount = 1
                        fetchLatestOrders(Constants.MODE_COMPLETED, "", completedPageCount)
                    }
                }
                }
            }
        })
        ordersRecyclerView?.apply {
            isNestedScrollingEnabled = false
            mActivity?.run {
                orderAdapter = OrderAdapterV2(this, mOrderList)
            }
            orderAdapter.setCheckBoxListener(this@HomeFragment)
            linearLayoutManager = LinearLayoutManager(mActivity)
            layoutManager = linearLayoutManager
            adapter = orderAdapter
            addItemDecoration(StickyRecyclerHeadersDecoration(orderAdapter))
        }
        completedOrdersRecyclerView?.apply {
            isNestedScrollingEnabled = false
            mActivity?.run {
                completedOrderAdapter = OrderAdapterV2(this, mCompletedOrderList)
            }
            completedOrderAdapter.setCheckBoxListener(this@HomeFragment)
            linearLayoutManager = LinearLayoutManager(mActivity)
            layoutManager = linearLayoutManager
            adapter = completedOrderAdapter
            addItemDecoration(StickyRecyclerHeadersDecoration(completedOrderAdapter))
        }
    }

    private fun fetchLatestOrders(mode: String, fetchingOrderStr: String?, page: Int = 1) {
        if (fetchingOrderStr?.isNotEmpty() == true) showCancellableProgressDialog(mActivity, fetchingOrderStr)
        val request = OrdersRequest(mode, page)
        mHomeFragmentService?.getOrders(request)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            analyticsImageView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_ORDER_ANALYTICS,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                analyticsContainer?.visibility = View.VISIBLE
                mActivity?.let {
                    analyticsImageView?.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_analytics_green))
                }
            }
            takeOrderTextView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_TAKE_ORDER,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                showTakeOrderBottomSheet()
            }
            searchImageView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SEARCH_INTENT,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                showSearchDialog(mOrderPageInfoStaticData, mMobileNumberString, mOrderIdString)
            }
            closeAnalyticsImageView?.id -> {
                mActivity?.let {
                    analyticsImageView?.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_analytics_black))
                }
                analyticsContainer?.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (mIsDoublePressToExit) mActivity?.finish()
        showShortSnackBar(if (mDoubleClickToExitStr?.isNotEmpty() == true) mDoubleClickToExitStr else getString(
                R.string.msg_back_press
            ))
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

    private fun setupHomePageWebView(webViewUrl: String) {
        try {
            val homePageWebView: WebView? = mContentView?.findViewById(R.id.homePageWebView)
            homePageWebView?.apply {
                val webViewController = CommonWebViewFragment.WebViewController()
                webViewController.commonWebView = commonWebView
                webViewController.activity = mActivity
                webViewClient = webViewController
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                addJavascriptInterface(WebViewBridge(), "Android")
                val url = webViewUrl + "?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&storeName=${getStringDataFromSharedPref(Constants.STORE_NAME)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"
                Log.d(TAG, "setupHomePageWebView: $url")
                loadUrl(url)
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupHomePageWebView: ${e.message}", e)
        }
    }

    override fun showAndroidToast(data: String) {
        showToast(data)
    }

    override fun showAndroidLog(data: String) {
        Log.d(TAG, "showAndroidLog :: $data")
    }

    override fun onPendingOrdersResponse(getOrderResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (mSwipeRefreshLayout?.isRefreshing == true) mSwipeRefreshLayout?.isRefreshing = false
            if (getOrderResponse.mIsSuccessStatus) {
                val ordersResponse = Gson().fromJson<OrdersResponse>(getOrderResponse.mCommonDataStr, OrdersResponse::class.java)
                mIsMorePendingOrderAvailable = ordersResponse.mIsNextDataAvailable
                if (pendingPageCount == 1) mOrderList.clear()
                if (ordersResponse.mOrdersList.isNotEmpty()) {
                    pendingOrderTextView?.visibility = View.VISIBLE
                    mOrderList.addAll(ordersResponse.mOrdersList)
                    if (!mIsMorePendingOrderAvailable) fetchLatestOrders(Constants.MODE_COMPLETED, "", 1)
                } else {
                    pendingOrderTextView?.visibility = View.GONE
                    mOrderList.addAll(ArrayList())
                    mCompletedOrderList.clear()
                    fetchLatestOrders(Constants.MODE_COMPLETED, "", 1)
                }
                convertDateStringOfOrders(mOrderList)
                orderAdapter.setOrderList(mOrderList)
            }
        }
    }

    override fun onCompletedOrdersResponse(getOrderResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (mSwipeRefreshLayout?.isRefreshing == true) mSwipeRefreshLayout?.isRefreshing = false
            if (getOrderResponse.mIsSuccessStatus) {
                val ordersResponse = Gson().fromJson<OrdersResponse>(
                    getOrderResponse.mCommonDataStr,
                    OrdersResponse::class.java
                )
                mIsMoreCompletedOrderAvailable = ordersResponse.mIsNextDataAvailable
                if (completedPageCount == 1) mCompletedOrderList.clear()
                if (ordersResponse.mOrdersList != null) {
                    completedOrderTextView?.visibility = View.VISIBLE
                    mCompletedOrderList.addAll(ordersResponse.mOrdersList)
                } else {
                    completedOrderTextView?.visibility = View.GONE
                    mCompletedOrderList.addAll(ArrayList())
                }
                convertDateStringOfOrders(mCompletedOrderList)
                completedOrderAdapter.setOrderList(mCompletedOrderList)
            }
        }
    }

    override fun onAnalyticsDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                analyticsResponse = Gson().fromJson<AnalyticsResponse>(commonResponse.mCommonDataStr, AnalyticsResponse::class.java)
                setupAnalyticsUI()
            }
        }
    }

    private fun setupAnalyticsUI() {
        try {
            val todaySaleValue: TextView? = mContentView?.findViewById(R.id.todaySaleValue)
            val amountValue: TextView? = mContentView?.findViewById(R.id.amountValue)
            val weekSaleValue: TextView? = mContentView?.findViewById(R.id.weekSaleValue)
            val weekAmountValue: TextView? = mContentView?.findViewById(R.id.weekAmountValue)
            val todaySaleHeading: TextView? = mContentView?.findViewById(R.id.todaySaleHeading)
            val amountHeading: TextView? = mContentView?.findViewById(R.id.amountHeading)
            val weekSaleHeading: TextView? = mContentView?.findViewById(R.id.weekSaleHeading)
            val weekAmountHeading: TextView? = mContentView?.findViewById(R.id.weekAmountHeading)
            todaySaleValue?.text = analyticsResponse?.today?.totalCount.toString()
            val amountValueStr = "${analyticsResponse?.analyticsStaticData?.textRuppeeSymbol} ${analyticsResponse?.today?.totalAmount}"
            amountValue?.text = amountValueStr
            weekSaleValue?.text = analyticsResponse?.thisWeek?.totalCount.toString()
            val weekValueStr = "${analyticsResponse?.analyticsStaticData?.textRuppeeSymbol} ${analyticsResponse?.thisWeek?.totalAmount}"
            weekAmountValue?.text = weekValueStr
            analyticsResponse?.analyticsStaticData?.run {
                todaySaleHeading?.text = textTodaySale
                amountHeading?.text = textTodayAmount
                weekSaleHeading?.text = textWeekSale
                weekAmountHeading?.text = textWeekAmount
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupAnalyticsUI: ${e.message}", e)
        }
    }

    override fun onOrderPageInfoResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                orderPageInfoResponse = Gson().fromJson<OrderPageInfoResponse>(commonResponse.mCommonDataStr, OrderPageInfoResponse::class.java)
                setupOrderPageInfoUI()
                pushProfileToCleverTap()
            }
        }
    }

    private fun pushProfileToCleverTap() {
        try {
            val storeResponse = orderPageInfoResponse?.mStoreInfo
            storeResponse?.run {
                val cleverTapProfile = CleverTapProfile()
                cleverTapProfile.mShopName = storeInfo?.name
                var businessTypeStr = ""
                storeBusiness?.forEachIndexed { _, businessResponse -> businessTypeStr += "$businessResponse ," }
                cleverTapProfile.mShopCategory = businessTypeStr
                cleverTapProfile.mPhone = storeOwner?.phone
                cleverTapProfile.mIdentity = storeOwner?.phone
                cleverTapProfile.mLat = storeAddress?.latitude
                cleverTapProfile.mLong = storeAddress?.longitude
                cleverTapProfile.mAddress = storeAddress?.let {
                    "${it.address1}, ${it.googleAddress}, ${it.pinCode}"
                }
                AppEventsManager.pushCleverTapProfile(cleverTapProfile)
            }
        } catch (e: Exception) {
            Log.e(TAG, "pushProfileToCleverTap: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "Exception Point" to "pushProfileToCleverTap",
                    "Exception Message" to e.message,
                    "Exception Logs" to e.toString()
                )
            )
        }
    }

    private fun setupOrderPageInfoUI() {
        try {
            orderPageInfoResponse?.run {
                mOrderPageInfoStaticData = mOrderPageStaticText?.run {
                    val appTitleTextView: TextView? = mContentView?.findViewById(R.id.appTitleTextView)
                    mFetchingOrdersStr = fetching_orders
                    mDoubleClickToExitStr = msg_double_click_to_exit
                    appTitleTextView?.text = heading_order_page
                    this
                }
                StaticInstances.sOrderPageInfoStaticData = mOrderPageInfoStaticData
                val pendingOrderTextView: TextView? = mContentView?.findViewById(R.id.pendingOrderTextView)
                val completedOrderTextView: TextView? = mContentView?.findViewById(R.id.completedOrderTextView)
                val takeOrderTextView: TextView? = mContentView?.findViewById(R.id.takeOrderTextView)
                val homePageWebViewLayout: View? = mContentView?.findViewById(R.id.homePageWebViewLayout)
                val orderLayout: View? = mContentView?.findViewById(R.id.orderLayout)
                val analyticsImageView: View? = mContentView?.findViewById(R.id.analyticsImageView)
                val searchImageView: View? = mContentView?.findViewById(R.id.searchImageView)
                val helpImageView: View? = mContentView?.findViewById(R.id.helpImageView)
                val bannerRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.bannerRecyclerView)
                pendingOrderTextView?.text = mOrderPageInfoStaticData?.text_pending
                completedOrderTextView?.text = mOrderPageInfoStaticData?.text_completed
                if (mIsZeroOrder.mIsActive) {
                    homePageWebViewLayout?.visibility = View.VISIBLE
                    orderLayout?.visibility = View.GONE
                    takeOrderTextView?.visibility = View.GONE
                    setupHomePageWebView(mIsZeroOrder.mUrl)
                    mSwipeRefreshLayout?.isEnabled = false
                } else {
                    mSwipeRefreshLayout?.isEnabled = true
                    homePageWebViewLayout?.visibility = View.GONE
                    orderLayout?.visibility = View.VISIBLE
                    val homePageBannerList = orderPageInfoResponse?.mBannerList
                    if (isEmpty(homePageBannerList)) {
                        bannerRecyclerView?.visibility = View.GONE
                    } else {
                        bannerRecyclerView?.visibility = View.VISIBLE
                        bannerRecyclerView?.apply {
                            linearLayoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                            layoutManager = linearLayoutManager
                            adapter = OrderPageBannerAdapter(
                                homePageBannerList,
                                object : IAdapterItemClickListener {
                                    override fun onAdapterItemClickListener(position: Int) {
                                        val item = homePageBannerList?.get(position)
                                        when (item?.mAction) {
                                            Constants.ACTION_ADD_BANK -> launchFragment(BankAccountFragment.newInstance(null, 0, false, null), true)
                                        }
                                    }
                                })
                        }
                    }
                    Handler(Looper.getMainLooper()).postDelayed({ fetchLatestOrders(Constants.MODE_PENDING, mFetchingOrdersStr, pendingPageCount) }, 150)
                }
                if (mIsHelpOrder.mIsActive) {
                    helpImageView?.visibility = View.VISIBLE
                    helpImageView?.setOnClickListener {
                        openWebViewFragmentV2(
                            this@HomeFragment,
                            getString(R.string.help),
                            mIsHelpOrder.mUrl,
                            Constants.SETTINGS
                        )
                    }
                }
                takeOrderTextView?.text = mOrderPageInfoStaticData?.text_add_new_order
                analyticsImageView?.visibility = if (mIsAnalyticsOrder) View.VISIBLE else View.GONE
                searchImageView?.visibility = if (mIsSearchOrder) View.VISIBLE else View.GONE
                takeOrderTextView?.visibility = if (mIsTakeOrder) View.VISIBLE else View.GONE
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupOrderPageInfoUI: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "Exception Point" to "setupOrderPageInfoUI",
                    "Exception Message" to e.message,
                    "Exception Logs" to e.toString()
                )
            )
        }
    }

    override fun onSearchOrdersResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (mSwipeRefreshLayout?.isRefreshing == true) mSwipeRefreshLayout?.isRefreshing = false
            if (commonResponse.mIsSuccessStatus) {
                val ordersResponse = Gson().fromJson<OrdersResponse>(
                    commonResponse.mCommonDataStr,
                    OrdersResponse::class.java
                )
                if (ordersResponse?.mOrdersList?.isNotEmpty() == true) launchFragment(SearchOrdersFragment.newInstance(mOrderIdString, mMobileNumberString, ordersResponse.mOrdersList), true) else {
                    showSearchDialog(StaticInstances.sOrderPageInfoStaticData, mMobileNumberString, mOrderIdString, true)
                }
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onOrdersUpdatedStatusResponse(commonResponse: CommonApiResponse) {
        Log.d(TAG, "onOrdersUpdatedStatusResponse: do nothing :: $commonResponse")
    }

    override fun onCompleteOrderStatusResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (mSwipeRefreshLayout?.isRefreshing == true) mSwipeRefreshLayout?.isRefreshing = false
            if (commonResponse.mIsSuccessStatus) {
                orderLayout?.fullScroll(ScrollView.FOCUS_UP)
                completedPageCount = 1
                pendingPageCount = 1
                mOrderList.clear()
                mCompletedOrderList.clear()
                fetchLatestOrders(Constants.MODE_PENDING, mFetchingOrdersStr, pendingPageCount)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onHomePageException(e: Exception) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (mSwipeRefreshLayout?.isRefreshing == true) mSwipeRefreshLayout?.isRefreshing = false
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
            showToast(validateOtpErrorResponse.mMessage)
        }
    }

    override fun onRefresh() {
        mOrderList.clear()
        mCompletedOrderList.clear()
        completedPageCount = 1
        pendingPageCount = 1
        fetchLatestOrders(Constants.MODE_PENDING, mFetchingOrdersStr, pendingPageCount)
        mHomeFragmentService?.getOrderPageInfo()
        mHomeFragmentService?.getAnalyticsData()
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
                    CoroutineScopeUtils().runTaskOnCoroutineBackground { getContactsFromStorage2(mActivity) }
                    if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
                        mHomeFragmentService?.getOrderPageInfo()
                        mHomeFragmentService?.getAnalyticsData()
                    }
                }
                else -> {
                    mHomeFragmentService?.getOrderPageInfo()
                    mHomeFragmentService?.getAnalyticsData()
                }
            }
        } else if (requestCode == Constants.IMAGE_PICK_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> openCameraWithoutCrop()
            }
        }
    }

    override fun onSearchDialogContinueButtonClicked(
        inputOrderId: String,
        inputMobileNumber: String
    ) {
        mOrderIdString = inputOrderId
        mMobileNumberString = inputMobileNumber
        val request = SearchOrdersRequest(if (mOrderIdString.isNotEmpty()) mOrderIdString.toLong() else 0, mMobileNumberString, 1)
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog()
        showProgressDialog(mActivity)
        mHomeFragmentService?.getSearchOrders(request)
    }

    override fun onOrderCheckBoxChanged(isChecked: Boolean, item: OrderItemResponse?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (Constants.TEXT_YES != PrefsManager.getStringDataFromSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN)) {
                if (Constants.DS_PAID_ONLINE == item?.displayStatus || Constants.DS_PREPAID_PICKUP_READY == item?.displayStatus || Constants.DS_PREPAID_DELIVERY_READY == item?.displayStatus) {
                    onDontShowDialogPositiveButtonClicked(item)
                } else if (isChecked) showDontShowDialog(item, mOrderPageInfoStaticData)
            } else onDontShowDialogPositiveButtonClicked(item)
        }
    }

    override fun onOrderItemCLickChanged(item: OrderItemResponse?) {
        var isNewOrder = false
        if (item?.displayStatus == Constants.DS_NEW) {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_VERIFY_ORDER_SEEN,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.ORDER_ID to "${item.orderId}",
                    AFInAppEventParameterName.PHONE to item.phone,
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    AFInAppEventParameterName.ORDER_TYPE to if (item.orderType == Constants.ORDER_TYPE_PICK_UP) "pickup" else "delivery",
                    AFInAppEventParameterName.IS_MERCHANT to "1"
                )
            )
            val request = UpdateOrderStatusRequest(
                item.orderId.toLong(),
                Constants.StatusSeenByMerchant.toLong()
            )
            mHomeFragmentService?.updateOrderStatus(request)
            isNewOrder = true
        }
        launchFragment(OrderDetailFragment.newInstance(item?.orderId.toString(), isNewOrder), true)
    }

    override fun onDontShowDialogPositiveButtonClicked(item: OrderItemResponse?) {
        val request = CompleteOrderRequest(item?.orderId?.toLong())
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
            showProgressDialog(mActivity)
            mHomeFragmentService?.completeOrder(request)
        }
    }

    override fun onNativeBackPressed() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.onBackPressed()
        }
    }

    override fun sendData(data: String) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                stopProgress()
                Log.d(TAG, "sendData: $data")
                val jsonData = JSONObject(data)
                when {
                    jsonData.optBoolean("takeOrder") -> {
                        showTakeOrderBottomSheet()
                    }
                    jsonData.optBoolean("redirectBrowser") -> {
                        val domain = jsonData.optString("data")
                        openUrlInBrowser(domain)
                    }
                    jsonData.optBoolean("trackEventData") -> {
                        val eventName = jsonData.optString("eventName")
                        val additionalData = jsonData.optString("additionalData")
                        val map = Gson().fromJson<HashMap<String, String>>(additionalData.toString(), HashMap::class.java)
                        Log.d(TAG, "sendData: working $map")
                        AppEventsManager.pushAppEvents(
                            eventName = eventName,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = map
                        )
                    }
                    else -> {
                        launchFragment(AddProductFragment.newInstance(0, true), true)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "sendData: ${e.message}", e)
            }
        }
    }

    override fun onImageSelectionResultFileAndUri(fileUri: Uri?, file: File?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                launchFragment(SendBillFragment.newInstance(fileUri, file), true)
            } catch (e: Exception) {
                Log.e(TAG, "onImageSelectionResultUri: ${e.message}", e)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        "Exception Point" to "onImageSelectionResultUri",
                        "Exception Message" to e.message,
                        "Exception Logs" to e.toString()
                    )
                )
            }
        }
    }

    private fun showTakeOrderBottomSheet() {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(
                R.layout.bottom_sheet_take_order,
                it.findViewById(R.id.bottomSheetContainer)
            )
            bottomSheetDialog.apply {
                setContentView(view)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                view?.run {
                    val imageViewSendBill: ImageView = findViewById(R.id.imageViewSendBill)
                    val shareButtonTextView: TextView = findViewById(R.id.shareButtonTextView)
                    val takeOrderMessageTextView: TextView = findViewById(R.id.takeOrderMessageTextView)
                    val createNewBillTextView: TextView = findViewById(R.id.createNewBillTextView)
                    val clickBillPhotoContainer: View = findViewById(R.id.clickBillPhotoContainer)
                    clickBillPhotoContainer.setOnClickListener {
                        clickBillPhotoContainer.isEnabled = false
                        bottomSheetDialog.dismiss()
                        openCameraWithoutCrop()
                    }
                    shareButtonTextView.text = mOrderPageInfoStaticData?.bottom_sheet_click_bill_photo
                    takeOrderMessageTextView.text = mOrderPageInfoStaticData?.bottom_sheet_take_order_message
                    createNewBillTextView.text = mOrderPageInfoStaticData?.bottom_sheet_create_a_new_bill
                    imageViewSendBill?.let {
                        try {
                            Picasso.get().load(orderPageInfoResponse?.mTakeOrderImage).into(it)
                        } catch (e: Exception) {
                            Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                        }
                    }
                    createNewBillTextView.setOnClickListener {
                        createNewBillTextView.isEnabled = false
                        bottomSheetDialog.dismiss()
                        launchFragment(
                            CommonWebViewFragment().newInstance(
                                "",
                                "${BuildConfig.WEB_VIEW_URL}${WebViewUrls.WEB_VIEW_TAKE_A_ORDER}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"
                            ), true
                        )
                    }
                }
            }.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        orderPageInfoResponse = null
    }
}
