package com.digitaldukaan.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.CustomerDeliveryAddressAdapter
import com.digitaldukaan.adapters.DeliveryTimeAdapter
import com.digitaldukaan.adapters.OrderDetailsAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IChipItemClickListener
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.dto.CustomerDeliveryAddressDTO
import com.digitaldukaan.models.request.UpdateOrderRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.OrderDetailService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IOrderDetailServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.bottom_layout_send_bill.*
import kotlinx.android.synthetic.main.order_detail_fragment.*
import java.io.File


class OrderDetailFragment : BaseFragment(), IOrderDetailServiceInterface, IOnToolbarIconClick {

    private var mOrderId = ""
    private var mMobileNumber = ""
    private lateinit var mOrderDetailService: OrderDetailService
    private var mOrderDetailStaticData: OrderDetailsStaticTextResponse? = null
    private lateinit var mDeliveryTimeAdapter: DeliveryTimeAdapter
    private var deliveryTimeResponse: DeliveryTimeResponse? = null
    private var mDeliveryTimeStr: String? = ""
    private var orderDetailMainResponse: OrderDetailMainResponse? = null
    private var mBillSentCameraClicked = false

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
            billCameraImageView.id -> {
                mBillSentCameraClicked = true
                handleDeliveryTimeBottomSheet(false)
            }
            sendBillTextView.id -> {
                handleDeliveryTimeBottomSheet(true)
            }
        }
    }

    private fun handleDeliveryTimeBottomSheet(isCallSendBillServerCall: Boolean) {
        if (deliveryTimeResponse != null) {
            showDeliveryTimeBottomSheet(deliveryTimeResponse, isCallSendBillServerCall)
        } else {
            if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
                return
            }
            showProgressDialog(mActivity)
            mOrderDetailService.getDeliveryTime(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
        }
    }

    private fun initiateSendBillServerCall() {
        orderDetailMainResponse?.orders?.run {
            val request = UpdateOrderRequest(
                orderId,
                amount,
                false,
                deliveryInfo?.deliveryTo,
                deliveryInfo?.deliveryFrom,
                mDeliveryTimeStr,
                orderDetailsItemsList,
                ""
            )
            showProgressDialog(mActivity)
            mOrderDetailService.updateOrder(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
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
            orderDetailMainResponse = Gson().fromJson<OrderDetailMainResponse>(commonResponse.mCommonDataStr, OrderDetailMainResponse::class.java)
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
            amountEditText.setText("${orderDetailMainResponse?.orders?.amount}")
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

    override fun onDeliveryTimeResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                deliveryTimeResponse = Gson().fromJson<DeliveryTimeResponse>(commonResponse.mCommonDataStr, DeliveryTimeResponse::class.java)
                showDeliveryTimeBottomSheet(deliveryTimeResponse, mBillSentCameraClicked)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onUpdateOrderResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_green_check_small)
                val response = Gson().fromJson<UpdateOrderResponse>(commonResponse.mCommonDataStr, UpdateOrderResponse::class.java)
                shareDataOnWhatsApp(response?.whatsAppText)
                launchFragment(HomeFragment.newInstance(), true)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onImageSelectionResultFile(file: File?) {
        super.onImageSelectionResultFile(file)
        launchFragment(SendBillPhotoFragment.newInstance(orderDetailMainResponse, file, mDeliveryTimeStr), true)
    }

    override fun onOrderDetailException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onToolbarSideIconClicked() {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mMobileNumber, null))
        mActivity.startActivity(intent)
    }

    private fun showDeliveryTimeBottomSheet(deliveryTimeResponse: DeliveryTimeResponse?, isCallSendBillServerCall: Boolean) {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_delivery_time,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            view.run {
                val bottomSheetHeading: TextView = findViewById(R.id.bottomSheetHeading)
                val bottomSheetSendBillText: TextView = findViewById(R.id.bottomSheetSendBillText)
                val deliveryTimeRecyclerView: RecyclerView = findViewById(R.id.deliveryTimeRecyclerView)
                deliveryTimeResponse?.staticText?.run {
                    bottomSheetHeading.text = heading_choose_delivery_time
                    bottomSheetSendBillText.text = text_send_bill
                    deliveryTimeRecyclerView.apply {
                        layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.HORIZONTAL)
                        mDeliveryTimeAdapter = DeliveryTimeAdapter(deliveryTimeResponse.deliveryTimeList, object : IChipItemClickListener {
                            override fun onChipItemClickListener(position: Int) {
                                deliveryTimeResponse.deliveryTimeList?.forEachIndexed { _, itemResponse -> itemResponse.isSelected = false }
                                deliveryTimeResponse.deliveryTimeList?.get(position)?.isSelected = true
                                mDeliveryTimeAdapter.setDeliveryTimeList(deliveryTimeResponse.deliveryTimeList)
                                mDeliveryTimeStr = deliveryTimeResponse.deliveryTimeList?.get(position)?.value
                                bottomSheetSendBillText.isEnabled = true
                            }
                        })
                        adapter = mDeliveryTimeAdapter
                    }
                }
                bottomSheetSendBillText.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    if (isCallSendBillServerCall) initiateSendBillServerCall() else openFullCamera()
                }
            }
        }.show()
    }

}