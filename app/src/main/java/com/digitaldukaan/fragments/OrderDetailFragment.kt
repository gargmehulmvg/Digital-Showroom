package com.digitaldukaan.fragments

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Log
import android.view.*
import android.widget.*
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
import com.digitaldukaan.interfaces.IOnToolbarSecondIconClick
import com.digitaldukaan.models.dto.CustomerDeliveryAddressDTO
import com.digitaldukaan.models.request.CompleteOrderRequest
import com.digitaldukaan.models.request.UpdateOrderRequest
import com.digitaldukaan.models.request.UpdateOrderStatusRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.OrderDetailService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IOrderDetailServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.bottom_layout_send_bill.*
import kotlinx.android.synthetic.main.layout_order_detail_fragment.*
import java.io.File
import java.util.*


class OrderDetailFragment : BaseFragment(), IOrderDetailServiceInterface, IOnToolbarIconClick,
    IOnToolbarSecondIconClick, PopupMenu.OnMenuItemClickListener {

    private var mOrderId = ""
    private var mMobileNumber = ""
    private lateinit var mOrderDetailService: OrderDetailService
    private var mOrderDetailStaticData: OrderDetailsStaticTextResponse? = null
    private lateinit var mDeliveryTimeAdapter: DeliveryTimeAdapter
    private var deliveryTimeResponse: DeliveryTimeResponse? = null
    private var mDeliveryTimeStr: String? = ""
    private var orderDetailMainResponse: OrderDetailMainResponse? = null
    private var mBillSentCameraClicked = false
    private var mIsNewOrder = false
    private lateinit var deliveryTimeEditText: EditText
    private var mTotalDisplayAmount = 0.0
    private var mDeliveryChargeAmount = 0.0
    private var mOtherChargeAmount = 0.0
    private var mDiscountAmount = 0.0
    private var mIsPickUpOrder = false

    companion object {
        private const val TAG = "OrderDetailFragment"
        private const val CUSTOM = "custom"
        private var mShareBillResponseStr: String? = ""
        fun newInstance(orderId: String, isNewOrder: Boolean): OrderDetailFragment {
            val fragment = OrderDetailFragment()
            fragment.mOrderId = orderId
            fragment.mIsNewOrder = isNewOrder
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mOrderDetailService = OrderDetailService()
        mOrderDetailService.setOrderDetailServiceListener(this)
        mContentView = inflater.inflate(R.layout.layout_order_detail_fragment, container, false)
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
                minusTextView.visibility = View.VISIBLE
            }
            billCameraImageView.id -> {
                mBillSentCameraClicked = true
                handleDeliveryTimeBottomSheet(false)
            }
            sendBillTextView.id -> {
                if (mIsPickUpOrder) initiateSendBillServerCall() else handleDeliveryTimeBottomSheet(true)
            }
            detailTextView.id -> {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                } else {
                    showProgressDialog(mActivity)
                    mOrderDetailService.getOrderDetailStatus(orderDetailMainResponse?.orders?.orderId)
                }
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
            mOrderDetailService.getDeliveryTime()
        }
    }

    private fun initiateSendBillServerCall() {
        orderDetailMainResponse?.orders?.run {
            if (orderDetailMainResponse?.storeServices?.mDeliveryChargeType == Constants.FIXED_DELIVERY_CHARGE && orderDetailMainResponse?.storeServices?.mDeliveryPrice != 0.0) {
                val orderDetailItemResponse = OrderDetailItemResponse(
                    0,
                    0,
                    "Delivery Charges",
                    1,
                    orderDetailMainResponse?.storeServices?.mDeliveryPrice,
                    1,
                    Constants.ITEM_TYPE_DELIVERY_CHARGE,
                    Constants.CREATOR_TYPE_MERCHANT
                )
                orderDetailsItemsList?.add(orderDetailItemResponse)
            }
            if (deliveryChargeValueEditText.text?.isNotEmpty() == true) {
                val orderDetailItemResponse = OrderDetailItemResponse(
                    0,
                    0,
                    "Delivery Charges",
                    1,
                    if (deliveryChargeValueEditText.text?.isNotEmpty() == true) deliveryChargeValueEditText.text.toString().toDouble() else 0.0,
                    1,
                    Constants.ITEM_TYPE_DELIVERY_CHARGE,
                    Constants.CREATOR_TYPE_MERCHANT
                )
                orderDetailsItemsList?.add(orderDetailItemResponse)
            }
            if (otherChargesValueEditText.text?.isNotEmpty() == true) {
                val orderDetailItemResponse = OrderDetailItemResponse(
                    0,
                    0,
                    if (otherChargesEditText.text?.isNotEmpty() == true) otherChargesEditText.text.toString() else "Charges",
                    1,
                    if (otherChargesValueEditText.text?.isNotEmpty() == true) otherChargesValueEditText.text.toString().toDouble() else 0.0,
                    1,
                    Constants.ITEM_TYPE_CHARGE,
                    Constants.CREATOR_TYPE_MERCHANT
                )
                orderDetailsItemsList?.add(orderDetailItemResponse)
            }
            if (discountsValueEditText.text?.isNotEmpty() == true) {
                val orderDetailItemResponse = OrderDetailItemResponse(
                    0,
                    0,
                    if (discountsEditText.text?.isNotEmpty() == true) discountsEditText.text.toString() else "Discount",
                    1,
                    if (discountsValueEditText.text?.isNotEmpty() == true) discountsValueEditText.text.toString().toDouble() else 0.0,
                    1,
                    Constants.ITEM_TYPE_DISCOUNT,
                    Constants.CREATOR_TYPE_MERCHANT
                )
                orderDetailsItemsList?.add(orderDetailItemResponse)
            }
            val request = UpdateOrderRequest(
                orderId,
                if (amountEditText.text?.isNotEmpty() == true) amountEditText.text.toString().toDouble() else amount,
                false,
                deliveryInfo?.deliveryTo,
                deliveryInfo?.deliveryFrom,
                mDeliveryTimeStr,
                orderDetailsItemsList,
                ""
            )
            Log.d(OrderDetailFragment::class.java.simpleName, "initiateSendBillServerCall: $request")
            showProgressDialog(mActivity)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_BILL_SENT,
                isCleverTapEvent = true, isAppFlyerEvent = false, isServerCallEvent = true,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    AFInAppEventParameterName.ORDER_TYPE to "${orderDetailMainResponse?.orders?.orderType}",
                    AFInAppEventParameterName.ADDRESS to "${orderDetailMainResponse?.orders?.deliveryInfo?.address1}",
                    AFInAppEventParameterName.ORDER_ID to "${orderDetailMainResponse?.orders?.orderId}",
                    AFInAppEventParameterName.IS_MERCHANT to "1",
                    AFInAppEventParameterName.NUMBER_OF_ITEM to "${orderDetailMainResponse?.orders?.orderDetailsItemsList?.size}",
                    AFInAppEventParameterName.LOCATION to "${orderDetailMainResponse?.orders?.deliveryInfo?.latitude} ${orderDetailMainResponse?.orders?.deliveryInfo?.longitude}",
                    AFInAppEventParameterName.PHONE to "${orderDetailMainResponse?.orders?.phone}"
                )
            )
            mOrderDetailService.updateOrder(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            onBackPressed(this@OrderDetailFragment)
            setSecondSideIconVisibility(true)
            setSideIconVisibility(true)
            setSecondSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_call), this@OrderDetailFragment)
            setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_options_menu), this@OrderDetailFragment)
        }
        hideBottomNavigationView(true)
    }

    override fun onOrderDetailResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            orderDetailMainResponse = Gson().fromJson<OrderDetailMainResponse>(commonResponse.mCommonDataStr, OrderDetailMainResponse::class.java)
            val orderDetailResponse = orderDetailMainResponse?.orders
            mOrderDetailStaticData = orderDetailMainResponse?.staticText
            newOrderTextView.visibility = if (mIsNewOrder) View.VISIBLE else View.GONE
            sendBillLayout.visibility = if (orderDetailResponse?.displayStatus == Constants.DS_SEND_BILL || orderDetailResponse?.displayStatus == Constants.DS_NEW) View.VISIBLE else View.GONE
            orderDetailContainer.visibility = if (orderDetailResponse?.displayStatus == Constants.DS_SEND_BILL || orderDetailResponse?.displayStatus == Constants.DS_NEW) View.GONE else View.VISIBLE
            addDeliveryChargesLabel.visibility = if (orderDetailResponse?.displayStatus == Constants.DS_SEND_BILL) View.VISIBLE else View.GONE
            orderDetailItemRecyclerView.apply {
                layoutManager = LinearLayoutManager(mActivity)
                val list = orderDetailResponse?.orderDetailsItemsList
                list?.forEachIndexed { _, itemResponse ->
                    mTotalDisplayAmount += itemResponse.item_price ?: 0.0
                }
                var orderDetailAdapter: OrderDetailsAdapter? = null
                orderDetailAdapter = OrderDetailsAdapter(list, orderDetailResponse?.displayStatus, mOrderDetailStaticData, object : IChipItemClickListener {

                    override fun onChipItemClickListener(position: Int) {
                        val item = list?.get(position)
                        item?.item_status = if (list?.get(position)?.item_status == 2) 1 else 2
                        if (item?.item_status == 2) mTotalDisplayAmount -= item.item_price ?: 0.0 else mTotalDisplayAmount += item?.item_price ?: 0.0
                        orderDetailAdapter?.setOrderDetailList(list)
                        setAmountToEditText()
                        var isValidOrderAvailable = false
                        list?.forEachIndexed { _, itemResponse -> if (itemResponse.item_status != 2) isValidOrderAvailable = true }
                        sendBillTextView.isEnabled = isValidOrderAvailable
                    }
                })
                adapter = orderDetailAdapter
            }
            amountEditText.setText("$mTotalDisplayAmount")
            setStaticDataToUI(orderDetailResponse)
            ToolBarManager.getInstance().setHeaderSubTitle(getStringDateTimeFromOrderDate(getCompleteDateFromOrderString(orderDetailMainResponse?.orders?.createdAt)))
            setupDeliveryChargeUI(orderDetailResponse?.displayStatus, orderDetailMainResponse?.storeServices)
            when (orderDetailResponse?.orderType) {
                Constants.ORDER_TYPE_SELF -> {
                    customerDetailsLabel.visibility = View.GONE
                }
                Constants.ORDER_TYPE_PICK_UP -> {
                    mIsPickUpOrder = true
                    deliveryChargeLabel?.visibility = View.GONE
                    deliveryChargeValue?.visibility = View.GONE
                    customerDetailsLabel.visibility = View.GONE
                    deliveryChargeValueEditText?.visibility = View.GONE
                    addDeliveryChargesLabel.text = getString(R.string.add_discount_and_other_charges)
                    mDeliveryChargeAmount = 0.0
                    setAmountToEditText()
                }
                else -> {
                    mIsPickUpOrder = false
                    customerDeliveryDetailsRecyclerView.apply {
                        layoutManager = LinearLayoutManager(mActivity)
                        val customerDetailsList = ArrayList<CustomerDeliveryAddressDTO>()
                        orderDetailResponse?.deliveryInfo?.run {
                            customerDetailsList.add(CustomerDeliveryAddressDTO("${mOrderDetailStaticData?.text_name}:", deliverTo))
                            customerDetailsList.add(CustomerDeliveryAddressDTO("${mOrderDetailStaticData?.text_address}:", "$address1,$address2"))
                            customerDetailsList.add(CustomerDeliveryAddressDTO("${mOrderDetailStaticData?.text_city_and_pincode}:",
                                if (city == null) {
                                    if (pincode == null) {
                                        ""
                                    } else {
                                        "$pincode"
                                    }
                                } else if (pincode == null) {
                                    "$city"
                                } else {
                                    "$city, $pincode"
                                }
                            ))
                            customerDetailsList.add(CustomerDeliveryAddressDTO("${mOrderDetailStaticData?.text_landmark}:", landmark))
                        }
                        adapter = CustomerDeliveryAddressAdapter(customerDetailsList)
                    }
                }
            }
            if (orderDetailResponse?.instruction?.isNotEmpty() == true) {
                instructionsValue?.visibility = View.VISIBLE
                instructionsValue?.text = orderDetailResponse.instruction
                instructionsLabel?.visibility = View.VISIBLE
            }
            mMobileNumber = orderDetailResponse?.phone ?: ""
            if (orderDetailResponse?.imageLink?.isNotEmpty() == true) {
                viewBillTextView?.visibility = View.VISIBLE
                viewBillTextView.setOnClickListener {
                    showImageDialog(orderDetailResponse.imageLink)
                }
            }
            if (orderDetailMainResponse?.optionMenuList?.isEmpty() == true) {
                ToolBarManager.getInstance().setSideIconVisibility(false)
            }
        }
    }

    private fun setupDeliveryChargeUI(
        displayStatus: String?,
        storeServices: StoreServicesResponse?
    ) {
        when(storeServices?.mDeliveryChargeType) {
            Constants.FREE_DELIVERY -> {
                setFreeDelivery()
            }
            Constants.FIXED_DELIVERY_CHARGE -> {
                if (Constants.DS_SEND_BILL == displayStatus || Constants.DS_NEW == displayStatus) {
                    val amount = getAmountFromEditText()
                    if (storeServices.mFreeDeliveryAbove != 0.0 && storeServices.mFreeDeliveryAbove <= amount) {
                        mDeliveryChargeAmount = 0.0
                        setFreeDelivery()
                        setAmountToEditText()
                    } else {
                        mDeliveryChargeAmount = storeServices.mDeliveryPrice ?: 0.0
                        deliveryChargeLabel?.visibility = View.VISIBLE
                        deliveryChargeValue?.visibility = View.VISIBLE
                        addDeliveryChargesLabel.text = getString(R.string.add_discount_and_other_charges)
                        deliveryChargeValue?.text = "${storeServices.mDeliveryPrice}"
                        setAmountToEditText()
                    }
                } else {
                    deliveryChargeLabel?.visibility = View.GONE
                    deliveryChargeValue?.visibility = View.GONE
                }
            }
            Constants.CUSTOM_DELIVERY_CHARGE -> {
                if (Constants.DS_SEND_BILL == displayStatus || Constants.DS_NEW == displayStatus) {
                    val amount = getAmountFromEditText()
                    if (storeServices.mFreeDeliveryAbove != 0.0 && storeServices.mFreeDeliveryAbove <= amount) {
                        mDeliveryChargeAmount = 0.0
                        setFreeDelivery()
                        setAmountToEditText()
                    } else {
                        deliveryChargeLabel?.visibility = View.VISIBLE
                        deliveryChargeValue?.visibility = View.VISIBLE
                        deliveryChargeValueEditText?.visibility = View.VISIBLE
                    }
                } else {
                    deliveryChargeLabel?.visibility = View.GONE
                    deliveryChargeValue?.visibility = View.GONE
                    deliveryChargeValueEditText?.visibility = View.GONE
                }
                addDeliveryChargesLabel.text = getString(R.string.add_discount_and_other_charges)
            }
            Constants.UNKNOWN_DELIVERY_CHARGE -> {
                addDeliveryChargesLabel.text = getString(R.string.add_discount_and_other_charges)
                if (Constants.DS_SEND_BILL == displayStatus || Constants.DS_NEW == displayStatus) {
                    deliveryChargeLabel?.visibility = View.VISIBLE
                    deliveryChargeValue?.visibility = View.VISIBLE
                    deliveryChargeValueEditText?.visibility = View.VISIBLE
                } else {
                    deliveryChargeLabel?.visibility = View.GONE
                    deliveryChargeValue?.visibility = View.GONE
                    deliveryChargeValueEditText?.visibility = View.GONE
                }
            }
        }
        deliveryChargeValueEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val str = editable?.toString()
                mDeliveryChargeAmount = if (str?.isNotEmpty() == true) {
                    str.toDouble()
                } else 0.0
                setAmountToEditText()

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: do nothing")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: do nothing")
            }
        })
        otherChargesValueEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val str = editable?.toString()
                mOtherChargeAmount = if (str?.isNotEmpty() == true) {
                    str.toDouble()
                } else 0.0
                setAmountToEditText()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: do nothing")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: do nothing")
            }
        })
        discountsValueEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val str = editable?.toString()
                mDiscountAmount = if (str?.isNotEmpty() == true) {
                    str.toDouble()
                } else 0.0
                if (mDiscountAmount <= 0.0) {
                    discountsValueEditText.apply {
                        error = "Invalid Amount"
                        requestFocus()
                    }
                    sendBillTextView.isEnabled = false
                } else {
                    sendBillTextView.isEnabled = true
                    setAmountToEditText()
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: do nothing")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: do nothing")
            }
        })
    }

    private fun setFreeDelivery() {
        deliveryChargeLabel?.visibility = View.VISIBLE
        deliveryChargeValue?.visibility = View.VISIBLE
        val txtSpannable =
            SpannableString(getString(R.string.free).toUpperCase(Locale.getDefault()))
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, 0, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        deliveryChargeValue.text = txtSpannable
        deliveryChargeValue.setTextColor(ContextCompat.getColor(mActivity, R.color.open_green))
        deliveryChargeValue.background =
            ContextCompat.getDrawable(mActivity, R.drawable.order_adapter_new)
        addDeliveryChargesLabel.text = getString(R.string.add_discount_and_other_charges)
    }

    private fun setAmountToEditText() {
        val amount = mTotalDisplayAmount + mDeliveryChargeAmount + mOtherChargeAmount - mDiscountAmount
        amountEditText.setText("$amount")
    }

    private fun getAmountFromEditText() : Double{
        return mTotalDisplayAmount + mDeliveryChargeAmount + mOtherChargeAmount - mDiscountAmount
    }

    private fun setStaticDataToUI(orderDetailResponse: OrderDetailsResponse?) {
        mOrderDetailStaticData?.run {
            sendBillToCustomerTextView?.setHtmlData(heading_send_bill_to_your_customer)
            sendBillTextView?.text = text_send_bill
            amountEditText?.hint = text_rupees_symbol
            otherChargesEditText?.hint = hint_other_charges
            discountsEditText?.hint = hint_discount
            discountsValueEditText?.hint = text_rupees_symbol
            otherChargesValueEditText?.hint = text_rupees_symbol
            customerCanPayUsingTextView?.text = text_customer_can_pay_via
            deliveryChargeLabel?.text = text_delivery_charges
            addDeliveryChargesLabel?.text = heading_add_delivery_and_other_charges
            instructionsLabel?.text = heading_instructions
            customerDetailsLabel?.text = heading_customer_details
            newOrderTextView?.text = text_new_order
            billAmountLabel?.text = "$text_bill_amount:"
            statusLabel?.text = "$text_status:"
            detailTextView?.text = "$text_details:"
            billAmountValue?.text = "$text_rupees_symbol ${orderDetailResponse?.amount}"
            ToolBarManager.getInstance().setHeaderTitle("$text_order #$mOrderId")
            if (orderDetailResponse?.deliveryInfo?.customDeliveryTime?.isEmpty() == true) estimateDeliveryTextView.visibility =
                View.GONE else estimateDeliveryTextView.text =
                "$text_estimate_delivery : ${orderDetailResponse?.deliveryInfo?.customDeliveryTime}"
            statusValue?.text = orderDetailResponse?.orderPaymentStatus?.value
            when (orderDetailResponse?.orderPaymentStatus?.key) {
                Constants.ORDER_STATUS_SUCCESS -> {
                    statusValue.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_tick_green_hollow,
                        0
                    )
                    orderDetailContainer.setBackgroundColor(
                        ContextCompat.getColor(
                            mActivity,
                            R.color.open_green
                        )
                    )
                }
                Constants.ORDER_STATUS_REJECTED -> orderDetailContainer.setBackgroundColor(
                    ContextCompat.getColor(mActivity, R.color.red)
                )
                Constants.ORDER_STATUS_IN_PROGRESS -> orderDetailContainer.setBackgroundColor(
                    ContextCompat.getColor(mActivity, R.color.order_detail_in_progress)
                )
                else -> orderDetailContainer.setBackgroundColor(
                    ContextCompat.getColor(
                        mActivity,
                        R.color.black
                    )
                )
            }
        }
    }

    override fun onDeliveryTimeResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                deliveryTimeResponse = Gson().fromJson<DeliveryTimeResponse>(commonResponse.mCommonDataStr, DeliveryTimeResponse::class.java)
                showDeliveryTimeBottomSheet(deliveryTimeResponse, !mBillSentCameraClicked)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onUpdateOrderResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_green_check_small)
                val response = Gson().fromJson<UpdateOrderResponse>(commonResponse.mCommonDataStr, UpdateOrderResponse::class.java)
                shareDataOnWhatsAppByNumber(mMobileNumber, response?.whatsAppText)
                launchFragment(HomeFragment.newInstance(), true)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onCompleteOrderStatusResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_green_check_small)
                mActivity.onBackPressed()
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onShareBillResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                mShareBillResponseStr = Gson().fromJson<String>(commonResponse.mCommonDataStr, String::class.java)
                shareDataOnWhatsAppByNumber(orderDetailMainResponse?.orders?.phone, mShareBillResponseStr)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onOrderDetailStatusResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val listType = object : TypeToken<List<OrderDetailTransactionItemResponse?>>() {}.type
                val txnItemList = Gson().fromJson<ArrayList<OrderDetailTransactionItemResponse?>>(commonResponse.mCommonDataStr, listType)
                showOrderDetailBottomSheet(txnItemList)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onUpdateStatusResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_green_check_small)
                mActivity.onBackPressed()
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onImageSelectionResultFile(file: File?, mode: String) {
        orderDetailMainResponse?.orders?.run {
            if (otherChargesValueEditText.text?.isNotEmpty() == true) {
                val orderDetailItemResponse = OrderDetailItemResponse(
                    0,
                    0,
                    if (otherChargesEditText.text?.isNotEmpty() == true) otherChargesEditText.text.toString() else "charge",
                    1,
                    if (otherChargesValueEditText.text?.isNotEmpty() == true) otherChargesValueEditText.text.toString().toDouble() else 0.0,
                    1,
                    "charge",
                    Constants.CREATOR_TYPE_MERCHANT
                )
                orderDetailsItemsList?.add(orderDetailItemResponse)
            }
            if (discountsValueEditText.text?.isNotEmpty() == true) {
                val orderDetailItemResponse = OrderDetailItemResponse(
                    0,
                    0,
                    if (discountsEditText.text?.isNotEmpty() == true) discountsEditText.text.toString() else "discount",
                    1,
                    if (discountsValueEditText.text?.isNotEmpty() == true) discountsValueEditText.text.toString().toDouble() else 0.0,
                    1,
                    "charge",
                    Constants.CREATOR_TYPE_MERCHANT
                )
                orderDetailsItemsList?.add(orderDetailItemResponse)
            }
            amount = (if (amountEditText.text?.isNotEmpty() == true) amountEditText.text.toString().toDouble() else amount)
        }
        launchFragment(SendBillPhotoFragment.newInstance(orderDetailMainResponse, file, mDeliveryTimeStr), true)
    }

    override fun onOrderDetailException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onToolbarSideIconClicked() {
        val sideView:View = mActivity.findViewById(R.id.sideIconToolbar)
        val optionsMenu = PopupMenu(mActivity, sideView)
        optionsMenu.inflate(R.menu.menu_product_fragment)
        orderDetailMainResponse?.optionMenuList?.forEachIndexed { position, response ->
            optionsMenu.menu?.add(Menu.NONE, position, Menu.NONE, response.mText)
        }
        optionsMenu.setOnMenuItemClickListener(this)
        optionsMenu.show()
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
                deliveryTimeEditText = findViewById(R.id.deliveryTimeEditText)
                val bottomSheetHeading: TextView = findViewById(R.id.bottomSheetHeading)
                val bottomSheetSendBillText: TextView = findViewById(R.id.bottomSheetSendBillText)
                val deliveryTimeRecyclerView: RecyclerView = findViewById(R.id.deliveryTimeRecyclerView)
                deliveryTimeResponse?.staticText?.run {
                    bottomSheetHeading.text = heading_choose_delivery_time
                    bottomSheetSendBillText.text = text_send_bill
                    deliveryTimeRecyclerView.apply {
                        layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
                        mDeliveryTimeAdapter = DeliveryTimeAdapter(deliveryTimeResponse.deliveryTimeList, object : IChipItemClickListener {
                            override fun onChipItemClickListener(position: Int) {
                                deliveryTimeResponse.deliveryTimeList?.forEachIndexed { _, itemResponse -> itemResponse.isSelected = false }
                                deliveryTimeResponse.deliveryTimeList?.get(position)?.isSelected = true
                                deliveryTimeEditText.visibility = if (CUSTOM == deliveryTimeResponse.deliveryTimeList?.get(position)?.key) View.VISIBLE else View.INVISIBLE
                                if (deliveryTimeEditText.visibility == View.VISIBLE) {
                                    deliveryTimeEditText.requestFocus()
                                    deliveryTimeEditText.showKeyboard()
                                }
                                mDeliveryTimeAdapter.setDeliveryTimeList(deliveryTimeResponse.deliveryTimeList)
                                mDeliveryTimeStr = deliveryTimeResponse.deliveryTimeList?.get(position)?.value
                                bottomSheetSendBillText.isEnabled = true
                            }
                        })
                        adapter = mDeliveryTimeAdapter
                    }
                }
                bottomSheetSendBillText.setOnClickListener {
                    if (mDeliveryTimeStr?.equals(CUSTOM, true) == true) {
                        mDeliveryTimeStr = deliveryTimeEditText.text.toString()
                        if (mDeliveryTimeStr?.isEmpty() == true) {
                            deliveryTimeEditText.apply {
                                error = getString(R.string.mandatory_field_message)
                                requestFocus()
                            }
                            return@setOnClickListener
                        }
                    } else if (isCallSendBillServerCall) {
                        bottomSheetDialog.dismiss()
                        initiateSendBillServerCall()
                    } else {
                        bottomSheetDialog.dismiss()
                        openFullCamera()
                    }
                }
            }
        }.show()
    }

    private fun showOrderDetailBottomSheet(txnItemList: ArrayList<OrderDetailTransactionItemResponse?>) {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_order_detail,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            view.run {
                val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                val billAmountTextView: TextView = findViewById(R.id.billAmountTextView)
                val orderIdTextView: TextView = findViewById(R.id.orderIdTextView)
                val textViewTop: TextView = findViewById(R.id.textViewTop)
                val txnId: TextView = findViewById(R.id.txnId)
                val textViewBottom: TextView = findViewById(R.id.textViewBottom)
                val imageViewTop: ImageView = findViewById(R.id.imageViewTop)
                val imageViewBottom: ImageView = findViewById(R.id.imageViewBottom)
                orderDetailMainResponse?.let {
                    mOrderDetailStaticData?.run {
                        bottomSheetHeadingTextView.text = text_details
                        billAmountTextView.text = "$text_bill_amount: ${it.orders?.amount}"
                        orderIdTextView.text = "$text_order_id: ${it.orders?.orderId}"
                        val firstItem = txnItemList[0]
                        val lastItem = txnItemList[txnItemList.size -1]
                        firstItem?.run {
                            setOrderDetailBottomSheetItem(imageViewTop, textViewTop, this)
                            txnId.text = "Txn ID : ${firstItem.transactionId}"
                        }
                        lastItem?.run {
                            setOrderDetailBottomSheetItem(imageViewBottom, textViewBottom, this)
                        }
                    }
                }
            }
        }.show()
    }

    private fun setOrderDetailBottomSheetItem(imageView:ImageView, textView: TextView, item: OrderDetailTransactionItemResponse) {
        textView.setTextColor(ContextCompat.getColor(mActivity, R.color.black))
        textView.text = item.settlementStatus
        when(item.transactionStatus?.toLowerCase(Locale.getDefault())) {
            "" -> {
                imageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_order_detail_green_tick))
            }
            Constants.ORDER_STATUS_PAYOUT_SUCCESS -> {
                imageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_order_detail_green_tick))
            }
            Constants.ORDER_STATUS_IN_PROGRESS -> {
                imageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_order_detail_yellow_icon))
            }
            Constants.ORDER_STATUS_REFUND_SUCCESS -> {
                imageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_order_detail_black_tick))
            }
            Constants.ORDER_STATUS_REJECTED -> {
                imageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_close_red))
            }
        }

    }

    private fun showOrderRejectBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_order_reject,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            view.run {
                val rejectOrderRadioGroup: RadioGroup = findViewById(R.id.rejectOrderRadioGroup)
                val rejectOrderTextView: TextView = findViewById(R.id.startNowTextView)
                val orderRejectHeadingTextView: TextView = findViewById(R.id.orderRejectHeadingTextView)
                val itemNotAvailableRadioButton: RadioButton = findViewById(R.id.itemNotAvailableRadioButton)
                val deliveryGuyNotAvailableRadioButton: RadioButton = findViewById(R.id.deliveryGuyNotAvailableRadioButton)
                val customerRequestedCancellationRadioButton: RadioButton = findViewById(R.id.customerRequestedCancellationRadioButton)
                mOrderDetailStaticData?.run {
                    orderRejectHeadingTextView.text = bottom_sheet_reject_order_heading
                    itemNotAvailableRadioButton.text = text_items_are_not_available
                    deliveryGuyNotAvailableRadioButton.text = text_delivery_guy_not_available
                    customerRequestedCancellationRadioButton.text = text_customer_request_cancellation
                    rejectOrderTextView.text = text_reject_order
                }
                var reason = ""
                rejectOrderRadioGroup.setOnCheckedChangeListener { _, id ->
                    when(id) {
                        itemNotAvailableRadioButton.id -> reason = itemNotAvailableRadioButton.text.toString()
                        deliveryGuyNotAvailableRadioButton.id -> reason = deliveryGuyNotAvailableRadioButton.text.toString()
                    }
                }
                itemNotAvailableRadioButton.isSelected = true
                itemNotAvailableRadioButton.isChecked = true
                rejectOrderTextView.setOnClickListener {
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return@setOnClickListener
                    }
                    bottomSheetDialog.dismiss()
                    val request = UpdateOrderStatusRequest(orderDetailMainResponse?.orders?.orderId?.toLong(), Constants.StatusRejected.toLong(), reason)
                    showProgressDialog(mActivity)
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_ORDER_REJECTED,
                        isCleverTapEvent = true, isAppFlyerEvent = false, isServerCallEvent = true,
                        data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.ORDER_ID to "${orderDetailMainResponse?.orders?.orderId}",
                            AFInAppEventParameterName.PHONE to PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER),
                            AFInAppEventParameterName.REASON to reason,
                            AFInAppEventParameterName.ORDER_TYPE to "${orderDetailMainResponse?.orders?.orderType}",
                            AFInAppEventParameterName.NUMBER_OF_ITEM to "1"
                        )
                    )
                    mOrderDetailService.updateOrderStatus(request)
                }
            }
        }.show()
    }

    override fun onToolbarSecondIconClicked() {
        val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mMobileNumber, null))
        mActivity.startActivity(intent)
    }

    override fun onMenuItemClick(menu: MenuItem?): Boolean {
        val menuItem = orderDetailMainResponse?.optionMenuList?.get(menu?.itemId ?: 0)
        when(menuItem?.mAction) {
            Constants.ACTION_REJECT_ORDER -> showOrderRejectBottomSheet()
            Constants.ACTION_CASH_COLLECTED -> cashCollected()
            Constants.ACTION_SHARE_BILL -> shareBill()
            Constants.ACTION_DOWNLOAD_BILL -> downloadBill()
        }
        return true
    }

    private fun downloadBill() {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_DOWNLOAD_BILL,
            isCleverTapEvent = true, isAppFlyerEvent = false, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
        val receiptStr = orderDetailMainResponse?.orders?.digitalReceipt
        Log.d(TAG, "downloadBill: $receiptStr")
        if (receiptStr?.isEmpty() == true) {
            showToast(mOrderDetailStaticData?.error_no_bill_available_to_download)
        } else {
            showToast("Start Downloading...")
            Picasso.get().load(receiptStr).into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    bitmap?.let {
                        downloadMediaToStorage(bitmap, mActivity)
                    }
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    Log.d(TAG, "onPrepareLoad: ")
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    Log.d(TAG, "onBitmapFailed: ")
                }
            })
        }
    }

    private fun shareBill() {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_SHARE_BILL,
            isCleverTapEvent = true, isAppFlyerEvent = false, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
        if (mShareBillResponseStr?.isNotEmpty() == true) {
            shareDataOnWhatsAppByNumber(
                orderDetailMainResponse?.orders?.phone,
                mShareBillResponseStr
            )
        } else if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
        } else {
            showProgressDialog(mActivity)
            mOrderDetailService.shareBillResponse(orderDetailMainResponse?.orders?.orderId)
        }
    }

    private fun cashCollected() {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
        } else {
            showProgressDialog(mActivity)
            mOrderDetailService.completeOrder(CompleteOrderRequest(orderDetailMainResponse?.orders?.orderId?.toLong()))
        }
    }

}