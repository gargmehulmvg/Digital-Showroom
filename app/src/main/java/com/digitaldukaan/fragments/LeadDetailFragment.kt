package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.LeadsDetailItemAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.LeadDetailResponse
import com.digitaldukaan.models.response.LeadsResponse
import com.digitaldukaan.services.LeadsDetailService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ILeadsDetailServiceInterface
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main2.backButtonToolbar
import kotlinx.android.synthetic.main.layout_lead_detail_fragment.*
import java.util.*

class LeadDetailFragment: BaseFragment(), ILeadsDetailServiceInterface {

    private var mService: LeadsDetailService? = null
    private var mLeadResponse: LeadsResponse? = null

    companion object {
        fun newInstance(item: LeadsResponse?): LeadDetailFragment {
            val fragment = LeadDetailFragment()
            fragment.mLeadResponse = item
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "LeadsDetailFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_lead_detail_fragment, container, false)
        mService = LeadsDetailService()
        mService?.setServiceListener(this)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.hideToolBar(mActivity, true)
        hideBottomNavigationView(true)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        mService?.getOrderCartById(mLeadResponse?.cartId ?: "")
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            backButtonToolbar?.id -> mActivity?.onBackPressed()
        }
    }

    override fun onLeadsDetailException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onGetOrderCartByIdResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val leadDetailPageInfoResponse = Gson().fromJson(commonResponse.mCommonDataStr, LeadDetailResponse::class.java)
                setupUIFromResponse(leadDetailPageInfoResponse)
            }
        }
    }

    private fun setupUIFromResponse(pageInfoResponse: LeadDetailResponse?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            var displayStr: String
            pageInfoResponse?.staticText?.let { static ->
                reminderTextView?.text = static.headingSendReminder
                itemTextView?.text = static.headingItems
                priceTextView?.text = static.headingPrice
                okayTextView?.text = static.textOkay
                mobileTextView?.text = static.textMobileNumber
                amountTextView?.text = static.textTotalCartAmount
                totalItemsTextView?.text = static.textTotalCartItems
                appTitleTextView?.text = static.headingCart
                displayStr = "${static.textCartUpdatedOn} ${getDateStringForLeadsHeader(getDateFromOrderString(mLeadResponse?.lastUpdateOn) ?: Date())}"
                appSubTitleTextView?.text = displayStr
                deliveryTextView?.text = if (Constants.ORDER_TYPE_ADDRESS == pageInfoResponse.orderType) static.textDelivery else static.textPickup
                cartAbandonedTextView?.text = if (Constants.CART_TYPE_ABANDONED == pageInfoResponse.cartType) static.textCartAbandoned else static.textCartActive
                mActivity?.let { context -> cartAbandonedTextView?.background = ContextCompat.getDrawable(context,if (Constants.CART_TYPE_ABANDONED == pageInfoResponse.cartType) R.drawable.curve_red_cart_abandoned_background else R.drawable.curve_blue_cart_active_background) }
            }
            mobileDetailTextView?.text = pageInfoResponse?.userPhone
            displayStr = "₹${pageInfoResponse?.payAmount}"
            amountDetailTextView?.text = displayStr
            totalItemsDetailTextView?.text = "${pageInfoResponse?.orderDetailsItemsList?.size}"
            cartDetailItemRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity)
                adapter = LeadsDetailItemAdapter(mActivity, pageInfoResponse?.staticText, pageInfoResponse?.orderDetailsItemsList)
            }
        }
    }

}