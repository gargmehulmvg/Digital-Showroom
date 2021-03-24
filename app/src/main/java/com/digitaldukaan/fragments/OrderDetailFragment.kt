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
import com.digitaldukaan.models.response.OrderDetailsStaticTextResponse
import com.digitaldukaan.services.OrderDetailService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IOrderDetailServiceInterface
import com.google.gson.Gson
import kotlinx.android.synthetic.main.bottom_layout_send_bill.*
import kotlinx.android.synthetic.main.order_detail_fragment.*


class OrderDetailFragment : BaseFragment(), IOrderDetailServiceInterface, IOnToolbarIconClick {

    private var mOrderId = ""
    private var mMobileNumber = ""
    private lateinit var mOrderDetailService: OrderDetailService
    private var mOrderDetailStaticData: OrderDetailsStaticTextResponse? = null

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
            mOrderDetailStaticData = orderDetailMainResponse?.staticText
            mOrderDetailStaticData?.run {
                sendBillToCustomerTextView.setHtmlData(heading_send_bill_to_your_customer)
                sendBillTextView.text = text_send_bill
                amountEditText.hint = text_rupees_symbol
                otherChargesEditText.hint = hint_other_charges
                discountsEditText.hint = hint_discount
                discountsValueEditText.hint = text_rupees_symbol
                otherChargesValueEditText.hint = text_rupees_symbol
                customerCanPayUsingTextView.text = text_customer_can_pay_via
                deliveryChargeLabel.text = text_delivery_charges
                addDeliveryChargesLabel.text = heading_add_delivery_and_other_charges
                instructionsLabel.text = heading_instructions
                customerDetailsLabel.text = heading_customer_details
                newOrderTextView.text = text_new_order
                ToolBarManager.getInstance().setHeaderTitle("$text_order #$mOrderId")
            }
            customerDeliveryDetailsRecyclerView.apply {
                layoutManager = LinearLayoutManager(mActivity)
                val customerDetailsList = ArrayList<CustomerDeliveryAddressDTO>()
                orderDetailResponse?.deliveryInfo?.run {
                    customerDetailsList.add(CustomerDeliveryAddressDTO(mOrderDetailStaticData?.text_name, deliverTo))
                    customerDetailsList.add(CustomerDeliveryAddressDTO(mOrderDetailStaticData?.text_address, "$address1,$address2"))
                    customerDetailsList.add(CustomerDeliveryAddressDTO(mOrderDetailStaticData?.text_city_and_pincode, "$city,$pincode"))
                    customerDetailsList.add(CustomerDeliveryAddressDTO(mOrderDetailStaticData?.text_landmark, landmark))
                }
                adapter = CustomerDeliveryAddressAdapter(customerDetailsList)
            }
            orderDetailResponse?.instruction = "Please mention Happy birthday aditya on the top of the cake"
            if (orderDetailResponse?.instruction?.isNotEmpty() == true) {
                instructionsValue.visibility = View.VISIBLE
                instructionsValue.text = orderDetailResponse.instruction
                instructionsLabel.visibility = View.VISIBLE
            }
            if (orderDetailMainResponse?.storeServices?.mDeliveryPrice != 0.0) {
                deliveryChargeLabel.visibility = View.VISIBLE
                deliveryChargeValue.visibility = View.VISIBLE
                deliveryChargeValue.text = "${mOrderDetailStaticData?.text_rupees_symbol} ${orderDetailMainResponse?.storeServices?.mDeliveryPrice}"
            }
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