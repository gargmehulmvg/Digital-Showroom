package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.OrderAdapterV2
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.interfaces.IOrderListItemListener
import com.digitaldukaan.models.request.CompleteOrderRequest
import com.digitaldukaan.models.request.SearchOrdersRequest
import com.digitaldukaan.models.request.UpdateOrderStatusRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.OrderItemResponse
import com.digitaldukaan.models.response.OrdersResponse
import com.digitaldukaan.services.SearchOrdersService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ISearchOrderServiceInterface
import com.google.gson.Gson
import kotlinx.android.synthetic.main.home_fragment.*

class SearchOrdersFragment: BaseFragment(), IOnToolbarIconClick, ISearchOrderServiceInterface,
    IOrderListItemListener {

    private var mOrderIdString = ""
    private var mMobileNumberString = ""
    private lateinit var mService: SearchOrdersService
    private lateinit var orderAdapter: OrderAdapterV2
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var mOrderList: ArrayList<OrderItemResponse> = ArrayList()
    private var currentItems = 0
    private var totalItems = 0
    private var scrollOutItems = 0
    private var searchPageCount = 1
    private var mIsRecyclerViewScrolling = false
    private var mIsMoreSearchOrderAvailable = false

    companion object {
        private const val TAG = "SearchOrdersFragment"

        fun newInstance(orderIdString: String, mobileNumberString: String, orderList: ArrayList<OrderItemResponse>): SearchOrdersFragment {
            val fragment = SearchOrdersFragment()
            fragment.mMobileNumberString = mobileNumberString
            fragment.mOrderIdString = orderIdString
            fragment.mOrderList.addAll(orderList)
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.search_order_fragment, container, false)
        mService = SearchOrdersService()
        mService.setHomeFragmentServiceListener(this)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("\"${if (mMobileNumberString.isEmpty()) mOrderIdString else mMobileNumberString}\"")
            setSideIconVisibility(true)
            setSecondSideIconVisibility(false)
            onBackPressed(this@SearchOrdersFragment)
            mActivity?.let { setSideIcon(ContextCompat.getDrawable(it, R.drawable.ic_search), this@SearchOrdersFragment) }
        }
        ordersRecyclerView?.apply {
            convertDateStringOfOrders(mOrderList)
            mActivity?.let { orderAdapter = OrderAdapterV2(it, mOrderList) }
            linearLayoutManager = LinearLayoutManager(mActivity)
            layoutManager = linearLayoutManager
            adapter = orderAdapter
            orderAdapter.setCheckBoxListener(this@SearchOrdersFragment)
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
                        if (mIsMoreSearchOrderAvailable) {
                            searchPageCount++
                            val request = SearchOrdersRequest(mOrderIdString.toLong(), mMobileNumberString, searchPageCount)
                            if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else
                                mService.getSearchOrders(request)
                        }
                    }
                }
            })
        }
    }

    override fun onToolbarSideIconClicked() {
        showSearchDialog(StaticInstances.sOrderPageInfoStaticData, mMobileNumberString, mOrderIdString)
    }

    override fun onSearchDialogContinueButtonClicked(inputOrderId: String, inputMobileNumber: String) {
        mOrderIdString = inputOrderId
        mMobileNumberString = inputMobileNumber
        val request = SearchOrdersRequest(if (mOrderIdString.isNotEmpty()) mOrderIdString.toLong() else 0, mMobileNumberString, searchPageCount)
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog()
        ToolBarManager.getInstance()?.setHeaderTitle("\"${if (inputMobileNumber.isEmpty()) inputOrderId else inputMobileNumber}\"")
        showProgressDialog(mActivity)
        mService.getSearchOrders(request)
    }

    override fun onSearchOrderResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            val ordersResponse = Gson().fromJson<OrdersResponse>(commonResponse.mCommonDataStr, OrdersResponse::class.java)
            mOrderList.clear()
            if (ordersResponse?.mOrdersList?.isNotEmpty() == true) mOrderList.addAll(ordersResponse.mOrdersList) else {
                showSearchDialog(StaticInstances.sOrderPageInfoStaticData, mMobileNumberString, mOrderIdString, true)
            }
            orderAdapter.notifyDataSetChanged()
        }
    }

    override fun onOrdersUpdatedStatusResponse(commonResponse: CommonApiResponse) {
        Log.d(TAG, "onOrdersUpdatedStatusResponse: do nothing :: $commonResponse")
    }

    override fun onCompleteOrderStatusResponse(commonResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                if (!PrefsManager.getBoolDataFromSharedPref(PrefsManager.KEY_FIRST_ITEM_COMPLETED)) {
                    PrefsManager.storeBoolDataInSharedPref(PrefsManager.KEY_FIRST_ITEM_COMPLETED, true)
                    mActivity?.launchInAppReviewDialog()
                }
                onSearchDialogContinueButtonClicked(mOrderIdString, mMobileNumberString)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onSearchOrderException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onOrderCheckBoxChanged(isChecked: Boolean, item: OrderItemResponse?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (PrefsManager.getStringDataFromSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN) != Constants.TEXT_YES) {
                if (isChecked) showDontShowDialog(item, StaticInstances.sOrderPageInfoStaticData)
            } else {
                onDontShowDialogPositiveButtonClicked(item)
            }
        }
    }

    override fun onDontShowDialogPositiveButtonClicked(item: OrderItemResponse?) {
        val request = CompleteOrderRequest(item?.orderId?.toLong())
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
            showProgressDialog(mActivity)
            mService.completeOrder(request)
        }
    }

    override fun onOrderItemCLickChanged(item: OrderItemResponse?) {
        var isNewOrder = false
        if (item?.displayStatus == Constants.DS_NEW) {
            val request = UpdateOrderStatusRequest(item.orderId.toLong(), Constants.StatusSeenByMerchant.toLong())
            mService.updateOrderStatus(request)
            isNewOrder = true
        }
        launchFragment(OrderDetailFragment.newInstance(item?.orderHash.toString(), isNewOrder), true)
    }

}