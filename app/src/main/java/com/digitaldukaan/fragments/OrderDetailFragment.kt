package com.digitaldukaan.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.CustomerDeliveryAddressAdapter
import com.digitaldukaan.adapters.OrderDetailsAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.dto.CustomerDeliveryAddressDTO
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.OrderDetailMainResponse
import com.digitaldukaan.services.OrderDetailService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IOrderDetailServiceInterface
import com.google.gson.Gson
import kotlinx.android.synthetic.main.order_detail_fragment.*


class OrderDetailFragment : BaseFragment(), IOrderDetailServiceInterface, IOnToolbarIconClick {

    private var mOrderId = ""
    private var mMobileNumber = ""
    private lateinit var mOrderDetailService: OrderDetailService

    companion object {
        fun newInstance(orderId: String): OrderDetailFragment {
            val fragment = OrderDetailFragment()
            fragment.mOrderId = orderId
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mOrderDetailService = OrderDetailService()
        mOrderDetailService.setOrderDetailServiceListener(this)
        mContentView = inflater.inflate(R.layout.order_detail_fragment, container, false)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
        } else {
            showProgressDialog(mActivity)
            mOrderDetailService.getOrderDetail(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), mOrderId)
        }
        return mContentView
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            addDeliveryChargesLabel.id -> {
                addDeliveryChargesLabel.visibility = View.GONE
                addDeliveryChargesGroup.visibility = View.VISIBLE
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("Order #$mOrderId")
            onBackPressed(this@OrderDetailFragment)
            setHeaderSubTitle("11 Feb | 12:23PM")
            setSideIconVisibility(true)
            setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_call), this@OrderDetailFragment)
        }
        hideBottomNavigationView(true)
    }

    override fun onOrderDetailResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            val orderDetailMainResponse = Gson().fromJson<OrderDetailMainResponse>(commonResponse.mCommonDataStr, OrderDetailMainResponse::class.java)
            val orderDetailResponse = orderDetailMainResponse?.orders
            newOrderTextView.visibility = if (orderDetailResponse?.displayStatus == Constants.DS_NEW) View.VISIBLE else View.GONE
            val createdDate = orderDetailResponse?.createdAt?.let { getCompleteDateFromOrderString(it) }
            createdDate?.run { ToolBarManager.getInstance().setHeaderSubTitle(getStringDateTimeFromOrderDate(createdDate)) }
            orderDetailItemRecyclerView.apply {
                layoutManager = LinearLayoutManager(mActivity)
                adapter = OrderDetailsAdapter(orderDetailResponse?.orderDetailsItemsList)
            }
            customerDeliveryDetailsRecyclerView.apply {
                layoutManager = LinearLayoutManager(mActivity)
                val customerDetailsList = ArrayList<CustomerDeliveryAddressDTO>()
                orderDetailResponse?.deliveryInfo?.run {
                    customerDetailsList.add(CustomerDeliveryAddressDTO("Name", deliverTo))
                    customerDetailsList.add(CustomerDeliveryAddressDTO("Address", "$address1,$address2"))
                    customerDetailsList.add(CustomerDeliveryAddressDTO("City & Pin Code", "$city,$pincode"))
                    customerDetailsList.add(CustomerDeliveryAddressDTO("Landmark", landmark))
                }
                adapter = CustomerDeliveryAddressAdapter(customerDetailsList)
            }
            orderDetailResponse?.instruction = "Please mention Happy birthday aditya on the top of the cake"
            instructionsValue.text = orderDetailResponse?.instruction
            mMobileNumber = orderDetailResponse?.phone ?: ""
        }
    }

    override fun onOrderDetailException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onToolbarSideIconClicked() {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mMobileNumber, null))
        mActivity.startActivity(intent)
    }

}