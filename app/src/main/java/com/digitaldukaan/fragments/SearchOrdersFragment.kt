package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.OrderAdapterV2
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.request.SearchOrdersRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.OrderItemResponse
import com.digitaldukaan.services.SearchOrdersService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ISearchOrderServiceInterface
import com.timehop.stickyheadersrecyclerview.StickyRecyclerHeadersDecoration
import kotlinx.android.synthetic.main.home_fragment.*

class SearchOrdersFragment: BaseFragment(), IOnToolbarIconClick, ISearchOrderServiceInterface {

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
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("\"${if (mMobileNumberString.isEmpty()) mOrderIdString else mMobileNumberString}\"")
            setSideIconVisibility(true)
            onBackPressed(this@SearchOrdersFragment)
            setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_search), this@SearchOrdersFragment)
        }
        ordersRecyclerView.apply {
            orderAdapter = OrderAdapterV2(mActivity, mOrderList)
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
                        if (mIsMoreSearchOrderAvailable) {
                            searchPageCount++
                            val request = SearchOrdersRequest(mOrderIdString.toInt(), mMobileNumberString, searchPageCount)
                            if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else
                                mService.getSearchOrders(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
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
        val request = SearchOrdersRequest(mOrderIdString.toInt(), mMobileNumberString, searchPageCount)
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog()
        showProgressDialog(mActivity)
        mService.getSearchOrders(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
    }

    override fun onSearchOrderResponse(commonResponse: CommonApiResponse) {
        stopProgress()
    }

    override fun onSearchOrderException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}