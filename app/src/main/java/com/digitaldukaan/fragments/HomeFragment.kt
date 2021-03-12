package com.digitaldukaan.fragments

import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.digitaldukaan.R
import com.digitaldukaan.adapters.OrderAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.OrdersRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.HomeFragmentService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IHomeServiceInterface
import com.digitaldukaan.views.StickHeaderItemDecoration
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.layout_analytics.*
import kotlinx.android.synthetic.main.otp_verification_fragment.*
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class HomeFragment : BaseFragment(), IHomeServiceInterface,
    SwipeRefreshLayout.OnRefreshListener {

    companion object {
        private val TAG = HomeFragment::class.simpleName
        private val mOrderListStaticData = mStaticData.mStaticData.mOrderListStaticData
        private var mIsDoublePressToExit = false
        private lateinit var mHomeFragmentService: HomeFragmentService

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
        if (!askContactPermission()) if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else fetchLatestOrders()
        showProgressDialog(mActivity)
        mHomeFragmentService.getAnalyticsData(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
        mHomeFragmentService.getOrderPageInfo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateNavigationBarState(R.id.menuHome)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        showBottomNavigationView(false)
        swipeRefreshLayout.setOnRefreshListener(this)
        completedOrderTextView.text = mOrderListStaticData.completedText
        pendingOrderTextView.text = mOrderListStaticData.pendingText
    }

    private fun fetchLatestOrders() {
        //showProgressDialog(mActivity, getString(R.string.authenticating_user))
        //mHomeFragmentService.verifyUserAuthentication(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
        //mHomeFragmentService.getOrders(getStringDataFromSharedPref(Constants.STORE_ID), 1)
        //mHomeFragmentService.getOrders("4252", 1)
        //mHomeFragmentService.getCompletedOrders("4252", 1)
    }

    private fun fetchLatestOrders(mode: String, fetchingOrderStr: String) {
        showProgressDialog(mActivity, fetchingOrderStr)
        val request = OrdersRequest(mode, 1)
        mHomeFragmentService.getOrders(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            analyticsImageView.id -> {
                analyticsContainer.visibility = View.VISIBLE
                analyticsImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_analytics_green))
            }
            searchImageView.id -> showSearchDialog()
            closeAnalyticsImageView.id -> {
                analyticsImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_analytics_black))
                analyticsContainer.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed(): Boolean {
        if (mIsDoublePressToExit) mActivity.finish()
        showShortSnackBar(getString(R.string.msg_back_press))
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

    override fun onGetOrdersResponse(getOrderResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (getOrderResponse.mIsSuccessStatus) {
                val listType = object : TypeToken<List<OrderItemResponse>>() {}.type
                val ordersList = Gson().fromJson<ArrayList<OrderItemResponse>>(getOrderResponse.mCommonDataStr, listType)
                if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
                stopProgress()
                if (ordersList.isEmpty()) {
                    homePageWebViewLayout.visibility = View.VISIBLE
                    orderLayout.visibility = View.GONE
                    //setupHomePageWebView()
                    swipeRefreshLayout.isEnabled = false
                } else {
                    homePageWebViewLayout.visibility = View.GONE
                    orderLayout.visibility = View.VISIBLE
                    swipeRefreshLayout.isEnabled = true
                    //showOrderDataOnRecyclerView(getOrderResponse, ordersRecyclerView)
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
            Log.d(TAG, "setupHomePageWebView:: $url")
            loadUrl(url)
        }
    }

    private fun showOrderDataOnRecyclerView(getOrderResponse: OrdersResponse, recyclerView: RecyclerView) {
        val list = getOrderResponse.mOrdersList
        val updatedOrdersList = ArrayList<OrderItemResponse>()
        val updatedOrdersHeaderList = ArrayList<OrderItemResponse>()
        convertDateStringOfOrders(list)
        val groupMap: HashMap<Date?, ArrayList<OrderItemResponse>?>? =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                list.stream().collect(Collectors.groupingBy(OrderItemResponse::updatedDate)) as HashMap<Date?, ArrayList<OrderItemResponse>?>
            } else null
        groupMap?.run {
            this.forEach { (key, value) ->
                val headerItem = OrderItemResponse()
                headerItem.updatedDate = key
                headerItem.viewType = Constants.VIEW_TYPE_HEADER
                updatedOrdersList.add(headerItem)
                updatedOrdersHeaderList.add(headerItem)
                value?.forEachIndexed { _, itemResponse ->
                    itemResponse.viewType = Constants.VIEW_TYPE_ITEM
                    updatedOrdersList.add(itemResponse)
                }
            }
            recyclerView.apply {
                val orderAdapter = OrderAdapter(mActivity ,updatedOrdersList, updatedOrdersHeaderList)
                showToast(list.size.toString())
                layoutManager = LinearLayoutManager(mActivity)
                adapter = orderAdapter
                addItemDecoration(StickHeaderItemDecoration(orderAdapter))
            }
        }
    }

    override fun onCompletedOrdersResponse(getOrderResponse: OrdersResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
            showOrderDataOnRecyclerView(getOrderResponse, completedOrdersRecyclerView)
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
                    if (mIsZeroOrder.mIsActive) {
                        homePageWebViewLayout.visibility = View.VISIBLE
                        orderLayout.visibility = View.GONE
                        setupHomePageWebView(mIsZeroOrder.mUrl)
                    } else {
                        homePageWebViewLayout.visibility = View.GONE
                        orderLayout.visibility = View.VISIBLE
                        fetchLatestOrders(Constants.MODE_PENDING, "")
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

    private fun convertDateStringOfOrders(list: ArrayList<OrderItemResponse>) {
        list.forEachIndexed { _, itemResponse ->
            itemResponse.updatedDate = getDateFromOrderString(itemResponse.updatedAt)
            itemResponse.updatedCompleteDate = getCompleteDateFromOrderString(itemResponse.updatedAt)
        }
    }

    override fun onHomePageException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
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
        fetchLatestOrders()
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
                    if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else fetchLatestOrders()
                }
                else -> showShortSnackBar("Permission was denied")
            }
        }
    }

    private fun showSearchDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.let {
                val view = LayoutInflater.from(mActivity).inflate(R.layout.search_dialog, null)
                val dialog = Dialog(mActivity)
                dialog.apply {
                    setContentView(view)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    view?.run {
                        val searchRadioGroup: RadioGroup = findViewById(R.id.searchRadioGroup)
                        val orderIdRadioButton: RadioButton = findViewById(R.id.orderIdRadioButton)
                        val phoneRadioButton: RadioButton = findViewById(R.id.phoneNumberRadioButton)
                        val searchInputLayout: TextInputLayout = findViewById(R.id.searchInputLayout)
                        val mobileNumberEditText: EditText = findViewById(R.id.mobileNumberEditText)
                        searchRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                            when(checkedId) {
                                orderIdRadioButton.id -> {
                                    searchInputLayout.hint = "Enter Search ID"
                                }
                                phoneRadioButton.id -> {
                                    searchInputLayout.hint = "Enter Mobile Number"
                                }
                            }
                            mobileNumberEditText.setText("")
                        }
                        orderIdRadioButton.isChecked = true
                    }
                }.show()
            }
        }
    }

}