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
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.OrderAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.response.OrderItemResponse
import com.digitaldukaan.models.response.OrdersResponse
import com.digitaldukaan.models.response.ValidateOtpErrorResponse
import com.digitaldukaan.models.response.ValidateOtpResponse
import com.digitaldukaan.services.HomeFragmentService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IHomeFragmentServiceInterface
import com.digitaldukaan.views.StickHeaderItemDecoration
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.home_fragment.*
import kotlinx.android.synthetic.main.otp_verification_fragment.*
import java.util.*
import java.util.stream.Collectors
import kotlin.collections.ArrayList

class HomeFragment : BaseFragment(), IHomeFragmentServiceInterface,
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
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        showBottomNavigationView(false)
        swipeRefreshLayout.setOnRefreshListener(this)
        setupAnalyticsLayout()
        completedOrderTextView.text = mOrderListStaticData.completedText
        pendingOrderTextView.text = mOrderListStaticData.pendingText
    }

    private fun setupAnalyticsLayout() {
        val todaySaleHeading: TextView = mContentView.findViewById(R.id.todaySaleHeading)
        val weekSaleHeading: TextView = mContentView.findViewById(R.id.weekSaleHeading)
        val amountHeading: TextView = mContentView.findViewById(R.id.amountHeading)
        val weekAmountHeading: TextView = mContentView.findViewById(R.id.weekAmountHeading)
        todaySaleHeading.text = mOrderListStaticData.todaySale
        weekSaleHeading.text = mOrderListStaticData.weekSale
        amountHeading.text = mOrderListStaticData.amount
        weekAmountHeading.text = mOrderListStaticData.amount
    }

    private fun fetchLatestOrders() {
        showProgressDialog(mActivity, getString(R.string.authenticating_user))
        mHomeFragmentService.verifyUserAuthentication(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
        mHomeFragmentService.getOrders(getStringDataFromSharedPref(Constants.STORE_ID), 1)
        //mHomeFragmentService.getOrders("4252", 1)
        //mHomeFragmentService.getCompletedOrders("4252", 1)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            helpImageView.id -> openWebViewFragment(this, getString(R.string.help), Constants.WEB_VIEW_HELP, Constants.SETTINGS)
            analyticsImageView.id -> analyticsContainer.visibility = (if (analyticsContainer.visibility == View.VISIBLE) View.GONE else View.VISIBLE)
            searchImageView.id -> showSearchDialog()
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

    override fun onGetOrdersResponse(getOrderResponse: OrdersResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
            stopProgress()
            if (getOrderResponse.mOrdersList.isEmpty()) {
                homePageWebViewLayout.visibility = View.VISIBLE
                orderLayout.visibility = View.GONE
                setupHomePageWebView()
                swipeRefreshLayout.isEnabled = false
            } else {
                homePageWebViewLayout.visibility = View.GONE
                orderLayout.visibility = View.VISIBLE
                swipeRefreshLayout.isEnabled = true
                showOrderDataOnRecyclerView(getOrderResponse, ordersRecyclerView)
            }
        }
    }

    private fun setupHomePageWebView() {
        homePageWebView.apply {
            webViewClient = CommonWebViewFragment.WebViewController()
            settings.javaScriptEnabled = true
            Log.d(TAG, "setupHomePageWebView: $url")
            val url = BuildConfig.WEB_VIEW_URL + Constants.WEB_VIEW_NO_ORDER_PAGE + "?storeid=${getStringDataFromSharedPref(
                Constants.STORE_ID
            )}&" + "storeName=${getStringDataFromSharedPref(Constants.STORE_NAME)}" + "&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"
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