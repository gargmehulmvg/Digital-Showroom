package com.digitaldukaan.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.adapters.CustomerDeliveryAddressAdapter
import com.digitaldukaan.adapters.DeliveryTimeAdapter
import com.digitaldukaan.adapters.OrderDetailsAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IChipItemClickListener
import com.digitaldukaan.interfaces.IOrderDetailListener
import com.digitaldukaan.models.dto.CustomerDeliveryAddressDTO
import com.digitaldukaan.models.request.CompleteOrderRequest
import com.digitaldukaan.models.request.UpdateOrderRequest
import com.digitaldukaan.models.request.UpdateOrderStatusRequest
import com.digitaldukaan.models.request.UpdatePrepaidOrderRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.OrderDetailService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IOrderDetailServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.bottom_layout_send_bill.*
import kotlinx.android.synthetic.main.layout_order_detail_fragment.*
import java.io.File
import java.util.*


class OrderDetailFragment : BaseFragment(), IOrderDetailServiceInterface, PopupMenu.OnMenuItemClickListener {

    private var mOrderId = ""
    private var mMobileNumber = ""
    private var mOrderDetailService: OrderDetailService? = null
    private var mOrderDetailStaticData: OrderDetailsStaticTextResponse? = null
    private var mDeliveryTimeAdapter: DeliveryTimeAdapter? = null
    private var deliveryTimeResponse: DeliveryTimeResponse? = null
    private var mDeliveryTimeStr: String? = ""
    private var orderDetailMainResponse: OrderDetailMainResponse? = null
    private var mBillSentCameraClicked = false
    private var mIsNewOrder = false
    private var deliveryTimeEditText: EditText? = null
    private var mTotalPayAmount = 0.0
    private var mTotalActualAmount = 0.0
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
        mOrderDetailService?.setOrderDetailServiceListener(this)
        mContentView = inflater.inflate(R.layout.layout_order_detail_fragment, container, false)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
        } else {
            showProgressDialog(mActivity)
            mOrderDetailService?.getDeliveryTime()
            mOrderDetailService?.getOrderDetail(mOrderId)
        }
        mShareBillResponseStr = ""
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.apply { hideToolBar(mActivity, true) }
        mActivity?.let {
            sideIcon2Toolbar?.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_call))
            sideIconToolbar?.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_options_menu))
        }
        hideBottomNavigationView(true)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            addDeliveryChargesLabel?.id -> {
                addDeliveryChargesLabel?.visibility = View.GONE
                addDeliveryChargesGroup?.visibility = View.VISIBLE
                minusTextView?.visibility = View.VISIBLE
            }
            billCameraImageView?.id -> {
                mBillSentCameraClicked = true
                handleDeliveryTimeBottomSheet(isCallSendBillServerCall = false, isPrepaidOrder = false)
            }
            sendBillTextView?.id -> {
                orderDetailMainResponse?.orders?.run {
                    var isAllProductsAmountSet = true
                    orderDetailsItemsList?.forEachIndexed { _, itemResponse ->
                        if ((itemResponse.amount ?: 0.0) <= 0.0) {
                            showToast(getString(R.string.please_fill_price_for_each_product))
                            isAllProductsAmountSet = false
                            return@forEachIndexed
                        }
                    }
                    if (!isAllProductsAmountSet) return@run
                    if (mIsPickUpOrder) initiateSendBillServerCall() else handleDeliveryTimeBottomSheet(isCallSendBillServerCall = true, isPrepaidOrder = false)
                }
            }
            markOutForDeliveryTextView?.id -> {
                val displayStatus = orderDetailMainResponse?.orders?.displayStatus
                if (Constants.DS_MARK_READY == displayStatus) {
                    showCancellableProgressDialog(mActivity)
                    mOrderDetailService?.updatePrepaidOrder(orderDetailMainResponse?.orders?.orderHash, null)
                } else handleDeliveryTimeBottomSheet(isCallSendBillServerCall = false, isPrepaidOrder = true)
            }
            detailTextView?.id -> {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                } else {
                    showProgressDialog(mActivity)
                    mOrderDetailService?.getOrderDetailStatus(orderDetailMainResponse?.orders?.orderId)
                }
            }
            sideIconWhatsAppToolbar?.id -> {
                val displayStatus = orderDetailMainResponse?.orders?.displayStatus
                shareDataOnWhatsAppByNumber(mMobileNumber, if (Constants.DS_SEND_BILL == displayStatus || Constants.DS_NEW == displayStatus) "Hi, we are checking your order." else "")
            }
            backButtonToolbar?.id -> mActivity?.onBackPressed()
            sideIconToolbar?.id -> {
                val sideView:View? = mActivity?.findViewById(R.id.sideIconToolbar)
                val optionsMenu = PopupMenu(mActivity, sideView)
                optionsMenu.inflate(R.menu.menu_product_fragment)
                orderDetailMainResponse?.optionMenuList?.forEachIndexed { position, response -> optionsMenu.menu?.add(Menu.NONE, position, Menu.NONE, response.mText) }
                optionsMenu.setOnMenuItemClickListener(this)
                optionsMenu.show()
            }
            sideIcon2Toolbar?.id -> {
                try {
                    mActivity?.startActivity(Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mMobileNumber, null)))
                } catch (e: Exception) {
                    Log.e(TAG, "onClick: ${e.message}", e)
                }
            }
        }
    }

    private fun handleDeliveryTimeBottomSheet(isCallSendBillServerCall: Boolean, isPrepaidOrder: Boolean) {
        if (deliveryTimeResponse != null) {
            showDeliveryTimeBottomSheet(deliveryTimeResponse, isCallSendBillServerCall, isPrepaidOrder)
        } else {
            if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
                return
            }
            showProgressDialog(mActivity)
            mOrderDetailService?.getDeliveryTime()
        }
    }

    private fun initiateSendBillServerCall() {
        orderDetailMainResponse?.orders?.run {
            val payAmount = if (amountEditText.text?.isNotEmpty() == true) amountEditText.text.toString().toDouble() else amount
            if (payAmount ?: 0.0 <= 0.0) {
                amountEditText.apply {
                    error = mActivity?.getString(R.string.bill_value_must_be_greater_than_zero)
                    requestFocus()
                }
                return
            }
            var deliveryChargesAmount = 0.0
            if (!mIsPickUpOrder && orderDetailMainResponse?.storeServices?.mDeliveryChargeType == Constants.CUSTOM_DELIVERY_CHARGE) {
                deliveryChargesAmount = mDeliveryChargeAmount
            } else if (!mIsPickUpOrder && orderDetailMainResponse?.storeServices?.mDeliveryChargeType == Constants.FIXED_DELIVERY_CHARGE && orderDetailMainResponse?.storeServices?.mDeliveryPrice != 0.0 && orderDetailMainResponse?.storeServices?.mMinOrderValue ?: 0.0 >= payAmount ?: 0.0) {
                deliveryChargesAmount = orderDetailMainResponse?.storeServices?.mDeliveryPrice ?: 0.0
            }
            var isAllProductsAmountSet = true
            orderDetailsItemsList?.forEachIndexed { _, itemResponse ->
                if ((itemResponse.amount ?: 0.0) <= 0.0) {
                    showToast(getString(R.string.please_fill_price_for_each_product))
                    isAllProductsAmountSet = false
                    return@forEachIndexed
                }
            }
            if (!isAllProductsAmountSet) return@run
            val orderDetailList = orderDetailsItemsList?.toMutableList()
            if (!isEmpty(orderDetailList)) {
                orderDetailList?.forEachIndexed { _, itemResponse -> if (Constants.ITEM_TYPE_DELIVERY_CHARGE == itemResponse.item_type || Constants.ITEM_TYPE_CHARGE == itemResponse.item_type) orderDetailsItemsList?.remove(itemResponse) }
            }
            val request = UpdateOrderRequest(
                orderId,
                getActualAmountOfOrder(),
                false,
                deliveryInfo?.deliveryTo,
                deliveryInfo?.deliveryFrom,
                mDeliveryTimeStr,
                orderDetailsItemsList,
                "",
                if (otherChargesEditText.text?.isNotEmpty() == true) otherChargesEditText.text.toString() else "" ,
                if (otherChargesValueEditText.text?.isNotEmpty() == true) otherChargesValueEditText.text.toString().toDouble() else 0.0,
                if (discountsValueEditText.text?.isNotEmpty() == true) discountsValueEditText.text.toString().toDouble() else 0.0,
                payAmount,
                deliveryChargesAmount
            )
            Log.d(OrderDetailFragment::class.java.simpleName, "initiateSendBillServerCall: $request")
            showProgressDialog(mActivity)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_BILL_SENT,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
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
            mOrderDetailService?.updateOrder(request)
        }
    }

    override fun onOrderDetailResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            orderDetailMainResponse = Gson().fromJson<OrderDetailMainResponse>(commonResponse.mCommonDataStr, OrderDetailMainResponse::class.java)
            val orderDetailResponse = orderDetailMainResponse?.orders
            mOrderDetailStaticData = orderDetailMainResponse?.staticText
            newOrderTextView?.visibility = if (mIsNewOrder) View.VISIBLE else View.GONE
            val displayStatus = orderDetailResponse?.displayStatus
            Log.d(TAG, "onOrderDetailResponse: display status :: $displayStatus")
            sendBillLayout?.visibility = if (displayStatus == Constants.DS_SEND_BILL || displayStatus == Constants.DS_NEW) View.VISIBLE else View.GONE
            orderDetailContainer?.visibility = if (displayStatus == Constants.DS_SEND_BILL || displayStatus == Constants.DS_NEW) View.GONE else View.VISIBLE
            addDeliveryChargesLabel?.visibility = if (displayStatus == Constants.DS_SEND_BILL) View.VISIBLE else View.GONE
            setupPrepaidOrderUI(displayStatus, orderDetailResponse)
            var isCreateListItemAdded = false
            isCreateListItemAdded = setupOrderDetailItemRecyclerView(orderDetailResponse, isCreateListItemAdded, displayStatus)
            amountEditText?.setText("$mTotalPayAmount")
            setStaticDataToUI(orderDetailResponse)
            appSubTitleTextView?.text = getStringDateTimeFromOrderDate(getCompleteDateFromOrderString(orderDetailMainResponse?.orders?.createdAt))
            if (isCreateListItemAdded) orderDetailMainResponse?.storeServices?.mDeliveryChargeType = Constants.CUSTOM_DELIVERY_CHARGE
            setupDeliveryChargeUI(displayStatus, orderDetailMainResponse?.storeServices)
            when (orderDetailResponse?.orderType) {
                Constants.ORDER_TYPE_SELF_IMAGE -> {
                    blankSpace?.visibility = View.GONE
                    deliveryLabelLayout?.visibility = View.GONE
                    deliveryLabelLayout?.visibility = View.GONE
                    customerDetailsLabel?.visibility = View.GONE
                    billPhotoImageView?.visibility = View.VISIBLE
                    billPhotoImageView?.let {
                        try {
                            Glide.with(this).load(orderDetailResponse.imageLink).into(it)
                        } catch (e: Exception) {
                            Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                        }
                    }
                }
                Constants.ORDER_TYPE_SELF -> {
                    customerDetailsLabel?.visibility = View.GONE
                }
                Constants.ORDER_TYPE_PICK_UP -> {
                    if (orderDetailResponse.imageLink?.isNotEmpty() == true) {
                        viewBillTextView?.visibility = View.VISIBLE
                        viewBillTextView?.setOnClickListener { showImageDialog(orderDetailResponse.imageLink) }
                    }
                    mIsPickUpOrder = true
                    estimateDeliveryTextView?.visibility = View.GONE
                    deliveryChargeLabel?.visibility = View.GONE
                    deliveryChargeValue?.visibility = View.GONE
                    customerDetailsLabel?.visibility = View.GONE
                    deliveryChargeValueEditText?.visibility = View.GONE
                    addDeliveryChargesLabel?.text = getString(R.string.add_discount_and_other_charges)
                    mDeliveryChargeAmount = 0.0
                    setAmountToEditText()
                }
                else -> {
                    if (orderDetailResponse?.imageLink?.isNotEmpty() == true) {
                        viewBillTextView?.visibility = View.VISIBLE
                        viewBillTextView?.setOnClickListener { showImageDialog(orderDetailResponse.imageLink) }
                    }
                    mIsPickUpOrder = false
                    customerDeliveryDetailsRecyclerView?.apply {
                        layoutManager = LinearLayoutManager(mActivity)
                        val customerDetailsList = ArrayList<CustomerDeliveryAddressDTO>()
                        orderDetailResponse?.deliveryInfo?.run {
                            customerDetailsList.add(CustomerDeliveryAddressDTO("${mOrderDetailStaticData?.text_name}:", deliverTo))
                            customerDetailsList.add(CustomerDeliveryAddressDTO("${mOrderDetailStaticData?.text_address}:", "$address1,$address2"))
                            customerDetailsList.add(CustomerDeliveryAddressDTO("${mOrderDetailStaticData?.text_city_and_pincode}:",
                                if (city == null) {
                                    if (pincode == null) "" else "$pincode"
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
            sideIconToolbar?.visibility = if (orderDetailMainResponse?.optionMenuList?.isEmpty() == true) View.GONE else View. VISIBLE
        }
    }

    private fun setupOrderDetailItemRecyclerView(orderDetailResponse: OrderDetailsResponse?, isCreateListItemAdded: Boolean, displayStatus: String?): Boolean {
        var isCreateListItemAdded1 = isCreateListItemAdded
        orderDetailItemRecyclerView?.apply {
            layoutManager = LinearLayoutManager(mActivity)
            val list = orderDetailResponse?.orderDetailsItemsList
            val deliveryStatus = orderDetailResponse?.displayStatus
            val listCopy = list?.toMutableList()
            listCopy?.forEachIndexed { _, itemResponse ->
                if (Constants.ITEM_TYPE_DELIVERY_CHARGE == itemResponse.item_type || Constants.ITEM_TYPE_CHARGE == itemResponse.item_type || Constants.ITEM_TYPE_CHARGE == itemResponse.item_type) {
                    list.remove(itemResponse)
                }
            }
            if (orderDetailResponse?.deliveryCharge != 0.0) {
                list?.add(OrderDetailItemResponse(0, 0, getString(R.string.delivery_charge), 1, "1", orderDetailResponse.deliveryCharge, orderDetailResponse.deliveryCharge, orderDetailResponse.deliveryCharge, orderDetailResponse.deliveryCharge, 1, Constants.ITEM_TYPE_DELIVERY_CHARGE, null, 0, null, false))
            }
            if (orderDetailResponse?.extraCharges != 0.0) {
                list?.add(OrderDetailItemResponse(0, 0, orderDetailResponse.extraChargesName, 1, "1", orderDetailResponse.extraCharges, orderDetailResponse.extraCharges, orderDetailResponse.extraCharges, orderDetailResponse.extraCharges, 1, Constants.ITEM_TYPE_CHARGE, null, 0, null, false))
            }
            if (orderDetailResponse?.discount != 0.0) {
                list?.add(OrderDetailItemResponse(0, 0, getString(R.string.discount), 1, "1", orderDetailResponse.discount, orderDetailResponse.discount, orderDetailResponse.discount, orderDetailResponse.discount, 1, Constants.ITEM_TYPE_DISCOUNT, null, 0, null, false))
            }
            list?.forEachIndexed { _, itemResponse ->
                itemResponse.isItemEditable = (((deliveryStatus == Constants.DS_NEW || deliveryStatus == Constants.DS_SEND_BILL)) && itemResponse.item_price == 0.0)
                if (itemResponse.isItemEditable) isCreateListItemAdded1 = true
                mTotalPayAmount += itemResponse.amount ?: 0.0
                mTotalActualAmount += ((itemResponse.actualAmount ?: 0.0) * itemResponse.quantity)
            }
            var orderDetailAdapter: OrderDetailsAdapter? = null
            orderDetailAdapter = OrderDetailsAdapter(list, displayStatus, mOrderDetailStaticData, object : IOrderDetailListener {

                    override fun onOrderDetailItemClickListener(position: Int) {
                        val item = list?.get(position)
                        item?.item_status = if (list?.get(position)?.item_status == 2) 1 else 2
                        if (item?.item_status == 2) mTotalPayAmount -= item.amount ?: 0.0 else mTotalPayAmount += item?.amount ?: 0.0
                        if (item?.item_status == 2) mTotalActualAmount -= ((item.actualAmount ?: 0.0) * item.quantity) else mTotalActualAmount += ((item?.actualAmount ?: 0.0) * item?.quantity!!)
                        orderDetailAdapter?.setOrderDetailList(list)
                        setAmountToEditText()
                        var isValidOrderAvailable = false
                        list?.forEachIndexed { _, itemResponse -> if (itemResponse.item_status != 2) isValidOrderAvailable = true }
                        sendBillTextView.isEnabled = isValidOrderAvailable
                    }

                    override fun onOrderDetailListUpdateListener() {
                        Log.d(TAG, "onOrderDetailListUpdateListener: called")
                        mTotalPayAmount = 0.0
                        mTotalActualAmount = 0.0
                        list?.forEachIndexed { _, itemResponse ->
                            mTotalPayAmount += itemResponse.amount ?: 0.0
                            mTotalActualAmount += (itemResponse.actualAmount ?: 0.0)
                        }
                        setAmountToEditText()
                    }
                })
            adapter = orderDetailAdapter
        }
        return isCreateListItemAdded1
    }

    private fun setupPrepaidOrderUI(displayStatus: String?, orderDetailResponse: OrderDetailsResponse?) {
        if (orderDetailMainResponse?.orders?.prepaidFlag == Constants.ORDER_TYPE_PREPAID) {
            prepaidOrderLayout?.visibility = View.VISIBLE
            sendBillLayout?.visibility = View.GONE
            when (displayStatus) {
                Constants.DS_MARK_READY -> {
                    markOutForDeliveryTextView?.visibility = View.VISIBLE
                    markOutForDeliveryValueTextView?.visibility = View.GONE
                    markOutForDeliveryTextView?.text = mOrderDetailStaticData?.readyForPickupText
                }
                Constants.DS_OUT_FOR_DELIVERY -> {
                    markOutForDeliveryTextView?.visibility = View.VISIBLE
                    markOutForDeliveryValueTextView?.visibility = View.GONE
                    markOutForDeliveryTextView?.text = mOrderDetailStaticData?.markOutForDeliveryText
                }
                Constants.DS_PREPAID_PICKUP_READY -> {
                    markOutForDeliveryValueTextView?.visibility = View.VISIBLE
                    markOutForDeliveryTextView?.visibility = View.GONE
                    val txtSpannable = SpannableString(mOrderDetailStaticData?.pickUpOrderSuccess)
                    val boldSpan = StyleSpan(Typeface.BOLD)
                    txtSpannable.setSpan(boldSpan, 0, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    markOutForDeliveryValueTextView?.text = txtSpannable
                }
                Constants.DS_PREPAID_DELIVERY_READY -> {
                    markOutForDeliveryValueTextView?.visibility = View.VISIBLE
                    markOutForDeliveryTextView?.visibility = View.GONE
                    val txtSpannable = SpannableString("${mOrderDetailStaticData?.deliveryTimeIsSetAsText}\n${orderDetailResponse?.deliveryInfo?.customDeliveryTime}")
                    val boldSpan = StyleSpan(Typeface.BOLD)
                    txtSpannable.setSpan(boldSpan, mOrderDetailStaticData?.deliveryTimeIsSetAsText?.length ?: 0, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    markOutForDeliveryValueTextView?.text = txtSpannable
                }
                Constants.DS_PREPAID_PICKUP_COMPLETED -> prepaidOrderLayout?.visibility = View.GONE
                Constants.DS_PREPAID_DELIVERY_COMPLETED -> prepaidOrderLayout?.visibility = View.GONE
            }
        } else prepaidOrderLayout?.visibility = View.GONE
    }

    private fun setupDeliveryChargeUI(displayStatus: String?, storeServices: StoreServicesResponse?) {
        when(storeServices?.mDeliveryChargeType) {
            Constants.FREE_DELIVERY -> setFreeDelivery(displayStatus)
            Constants.FIXED_DELIVERY_CHARGE -> setFixedDeliveryChargeUI(displayStatus, storeServices)
            Constants.CUSTOM_DELIVERY_CHARGE -> setCustomDeliveryChargeUI(displayStatus, storeServices)
            Constants.UNKNOWN_DELIVERY_CHARGE -> setUnknownDeliveryChargeUI(displayStatus)
        }
        deliveryChargeValueEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val str = editable?.toString()
                mDeliveryChargeAmount = if (str?.isNotEmpty() == true) { str.toDouble() } else 0.0
                setAmountToEditText()

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: do nothing")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: do nothing")
            }
        })
        otherChargesValueEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val str = editable?.toString()
                mOtherChargeAmount = if (str?.isNotEmpty() == true) { str.toDouble() } else 0.0
                setAmountToEditText()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: do nothing")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: do nothing")
            }
        })
        discountsValueEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val str = editable?.toString()
                mDiscountAmount = if (str?.isNotEmpty() == true) { str.toDouble() } else 0.0
                setAmountToEditText()
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: do nothing")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: do nothing")
            }
        })
    }

    private fun setUnknownDeliveryChargeUI(displayStatus: String?) {
        addDeliveryChargesLabel?.text = getString(R.string.add_discount_and_other_charges)
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

    private fun setCustomDeliveryChargeUI(displayStatus: String?, storeServices: StoreServicesResponse) {
        if (Constants.DS_SEND_BILL == displayStatus || Constants.DS_NEW == displayStatus) {
            val amount = getAmountFromEditText()
            if (storeServices.mFreeDeliveryAbove != 0.0 && storeServices.mFreeDeliveryAbove <= amount) {
                mDeliveryChargeAmount = 0.0
                setFreeDelivery(displayStatus)
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
        addDeliveryChargesLabel?.text = getString(R.string.add_discount_and_other_charges)
    }

    private fun setFixedDeliveryChargeUI(displayStatus: String?, storeServices: StoreServicesResponse) {
        if (Constants.DS_SEND_BILL == displayStatus || Constants.DS_NEW == displayStatus) {
            val amount = getAmountFromEditText()
            if (storeServices.mFreeDeliveryAbove != 0.0 && storeServices.mFreeDeliveryAbove <= amount) {
                mDeliveryChargeAmount = 0.0
                setFreeDelivery(displayStatus)
                setAmountToEditText()
            } else {
                mDeliveryChargeAmount = storeServices.mDeliveryPrice ?: 0.0
                deliveryChargeLabel?.visibility = View.VISIBLE
                deliveryChargeValue?.visibility = View.VISIBLE
                addDeliveryChargesLabel?.text = getString(R.string.add_discount_and_other_charges)
                deliveryChargeValue?.text = "${storeServices.mDeliveryPrice}"
                setAmountToEditText()
            }
        } else {
            deliveryChargeLabel?.visibility = View.GONE
            deliveryChargeValue?.visibility = View.GONE
        }
    }

    private fun setFreeDelivery(displayStatus: String?) {
        try {
            if (Constants.DS_SEND_BILL == displayStatus || Constants.DS_NEW == displayStatus) {
                deliveryChargeLabel?.visibility = View.VISIBLE
                deliveryChargeValue?.visibility = View.VISIBLE
                val txtSpannable = SpannableString(getString(R.string.free).toUpperCase(Locale.getDefault()))
                val boldSpan = StyleSpan(Typeface.BOLD)
                txtSpannable.setSpan(boldSpan, 0, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                deliveryChargeValue?.text = txtSpannable
                mActivity?.run {
                    deliveryChargeValue?.setTextColor(ContextCompat.getColor(this, R.color.open_green))
                    deliveryChargeValue?.background = ContextCompat.getDrawable(this, R.drawable.order_adapter_new)
                }
                addDeliveryChargesLabel?.text = getString(R.string.add_discount_and_other_charges)
            } else {
                deliveryChargeLabel?.visibility = View.GONE
                deliveryChargeValue?.visibility = View.GONE
            }
        } catch (e: Exception) {
            Log.e(TAG, "setFreeDelivery: ${e.message}", e)
        }
    }

    private fun setAmountToEditText() {
        val amount = mTotalPayAmount + mDeliveryChargeAmount + mOtherChargeAmount - mDiscountAmount
        amountEditText?.setText("$amount")
    }

    private fun getAmountFromEditText() : Double {
        return mTotalPayAmount + mDeliveryChargeAmount + mOtherChargeAmount - mDiscountAmount
    }

    private fun getActualAmountOfOrder() : Double {
        return mTotalActualAmount + mDeliveryChargeAmount + mOtherChargeAmount
    }

    private fun setStaticDataToUI(orderDetailResponse: OrderDetailsResponse?) {
        mOrderDetailStaticData?.run {
            sendBillToCustomerTextView?.setHtmlData(heading_send_bill_to_your_customer)
            sendBillTextView?.text = text_send_bill
            amountEditText?.hint = text_rupees_symbol
            otherChargesEditText?.hint = hint_other_charges
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
            billAmountValue?.text = "$text_rupees_symbol ${orderDetailResponse?.payAmount}"
            appTitleTextView?.text = "$text_order #$mOrderId"
            if (Constants.ORDER_TYPE_PREPAID == orderDetailResponse?.prepaidFlag || orderDetailResponse?.deliveryInfo?.customDeliveryTime?.isEmpty() == true) {
                estimateDeliveryTextView?.visibility = View.GONE
            } else {
                val estimatedDeliveryStr = "$text_estimate_delivery : ${orderDetailResponse?.deliveryInfo?.customDeliveryTime}"
                estimateDeliveryTextView?.text = estimatedDeliveryStr
            }
            statusValue?.text = orderDetailResponse?.orderPaymentStatus?.value
            mActivity?.run {
                Log.d(TAG, "setStaticDataToUI: orderDetailResponse?.orderPaymentStatus?.key ${orderDetailResponse?.orderPaymentStatus?.key}")
                when (orderDetailResponse?.orderPaymentStatus?.key) {
                    Constants.ORDER_STATUS_SUCCESS -> {
                        statusValue?.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_tick_green_hollow, 0)
                        orderDetailContainer?.setBackgroundColor(ContextCompat.getColor(this, R.color.open_green))
                    }
                    Constants.ORDER_STATUS_REJECTED -> orderDetailContainer?.setBackgroundColor(ContextCompat.getColor(this, R.color.red))
                    Constants.ORDER_STATUS_IN_PROGRESS -> orderDetailContainer?.setBackgroundColor(ContextCompat.getColor(this, R.color.order_detail_in_progress))
                    else -> orderDetailContainer?.setBackgroundColor(ContextCompat.getColor(this, R.color.black))
                }
            }
        }
    }

    override fun onDeliveryTimeResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                deliveryTimeResponse = Gson().fromJson<DeliveryTimeResponse>(commonResponse.mCommonDataStr, DeliveryTimeResponse::class.java)
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
                mActivity?.onBackPressed()
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
                mActivity?.onBackPressed()
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onPrepaidOrderUpdateStatusResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val response = Gson().fromJson<UpdateOrderResponse>(commonResponse.mCommonDataStr, UpdateOrderResponse::class.java)
                shareDataOnWhatsAppByNumber(mMobileNumber, response?.whatsAppText)
                mOrderDetailService?.getOrderDetail(mOrderId)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onImageSelectionResultFile(file: File?, mode: String) {
        launchFragment(SendBillPhotoFragment.newInstance(orderDetailMainResponse, file, mDeliveryTimeStr), true)
    }

    override fun onOrderDetailException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    private fun showDeliveryTimeBottomSheet(deliveryTimeResponse: DeliveryTimeResponse?, isCallSendBillServerCall: Boolean, isPrepaidOrder: Boolean) {
        mActivity?.run {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(this).inflate(
                R.layout.bottom_sheet_delivery_time,
                findViewById(R.id.bottomSheetContainer)
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
                                    onDeliveryTimeItemClickListener(deliveryTimeResponse, position, bottomSheetSendBillText)
                                }
                            })

                            adapter = mDeliveryTimeAdapter
                        }
                    }
                    bottomSheetSendBillText.setOnClickListener {
                        if (mDeliveryTimeStr?.equals(CUSTOM, true) == true) {
                            mDeliveryTimeStr = deliveryTimeEditText?.text.toString()
                            if (mDeliveryTimeStr?.isEmpty() == true) {
                                deliveryTimeEditText?.apply {
                                    error = getString(R.string.mandatory_field_message)
                                    requestFocus()
                                }
                                return@setOnClickListener
                            }
                        } else if (isPrepaidOrder) {
                            bottomSheetDialog.dismiss()
                            orderDetailMainResponse?.orders?.run {
                                showCancellableProgressDialog(mActivity)
                                val request = UpdatePrepaidOrderRequest(deliveryInfo?.deliveryTo, deliveryInfo?.deliveryFrom, mDeliveryTimeStr)
                                mOrderDetailService?.updatePrepaidOrder(orderHash, request)
                            }
                        } else if (isCallSendBillServerCall) {
                            bottomSheetDialog.dismiss()
                            initiateSendBillServerCall()
                        } else {
                            bottomSheetDialog.dismiss()
                            openCameraWithoutCrop()
                        }
                    }
                }
            }.show()
        }
    }

    private fun onDeliveryTimeItemClickListener(deliveryTimeResponse: DeliveryTimeResponse, position: Int, bottomSheetSendBillText: TextView) {
        deliveryTimeResponse.deliveryTimeList?.forEachIndexed { _, itemResponse ->
            itemResponse.isSelected = false
        }
        deliveryTimeResponse.deliveryTimeList?.get(position)?.isSelected = true
        deliveryTimeEditText?.visibility = if (CUSTOM == deliveryTimeResponse.deliveryTimeList?.get(position)?.key) View.VISIBLE else View.INVISIBLE
        if (deliveryTimeEditText?.visibility == View.VISIBLE) {
            deliveryTimeEditText?.requestFocus()
            deliveryTimeEditText?.showKeyboard()
        }
        mDeliveryTimeAdapter?.setDeliveryTimeList(deliveryTimeResponse.deliveryTimeList)
        mDeliveryTimeStr = deliveryTimeResponse.deliveryTimeList?.get(position)?.value
        bottomSheetSendBillText.isEnabled = true
    }

    private fun showOrderDetailBottomSheet(txnItemList: ArrayList<OrderDetailTransactionItemResponse?>) {
        mActivity?.run {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(this).inflate(
                R.layout.bottom_sheet_order_detail,
                findViewById(R.id.bottomSheetContainer)
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
                            val billAmountStr = "$text_bill_amount: ${it.orders?.amount}"
                            billAmountTextView.text = billAmountStr
                            val orderIdStr = "$text_order_id: ${it.orders?.orderId}"
                            orderIdTextView.text = orderIdStr
                            val firstItem = txnItemList[0]
                            val lastItem = txnItemList[txnItemList.size -1]
                            firstItem?.run {
                                setOrderDetailBottomSheetItem(imageViewTop, textViewTop, this)
                                val txnIdStr = "Txn ID : ${firstItem.transactionId}"
                                txnId.text = txnIdStr
                            }
                            lastItem?.run { setOrderDetailBottomSheetItem(imageViewBottom, textViewBottom, this) }
                        }
                    }
                }
            }.show()
        }
    }

    private fun setOrderDetailBottomSheetItem(imageView:ImageView, textView: TextView, item: OrderDetailTransactionItemResponse) {
        mActivity?.run {
            textView.setTextColor(ContextCompat.getColor(this, R.color.black))
            textView.text = item.settlementStatus
            when(item.transactionStatus?.toLowerCase(Locale.getDefault())) {
                "" -> imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_order_detail_green_tick))
                Constants.ORDER_STATUS_PAYOUT_SUCCESS -> imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_order_detail_green_tick))
                Constants.ORDER_STATUS_IN_PROGRESS -> imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_order_detail_yellow_icon))
                Constants.ORDER_STATUS_REFUND_SUCCESS -> imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_order_detail_black_tick))
                Constants.ORDER_STATUS_REJECTED -> imageView.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_close_red))
            }
        }
    }

    private fun showOrderRejectBottomSheet() {
        mActivity?.run {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(this).inflate(
                R.layout.bottom_sheet_order_reject,
                findViewById(R.id.bottomSheetContainer)
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
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                AFInAppEventParameterName.ORDER_ID to "${orderDetailMainResponse?.orders?.orderId}",
                                AFInAppEventParameterName.PHONE to PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER),
                                AFInAppEventParameterName.REASON to reason,
                                AFInAppEventParameterName.ORDER_TYPE to "${orderDetailMainResponse?.orders?.orderType}",
                                AFInAppEventParameterName.NUMBER_OF_ITEM to "1"
                            )
                        )
                        mOrderDetailService?.updateOrderStatus(request)
                    }
                }
            }.show()
        }
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
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
        val receiptStr = orderDetailMainResponse?.orders?.digitalReceipt
        Log.d(TAG, "downloadBill: $receiptStr")
        if (askStoragePermission()) return
        startDownloadBill(receiptStr)
    }

    private fun startDownloadBill(receiptStr: String?) = if (receiptStr?.isEmpty() == true) {
        showToast(mOrderDetailStaticData?.error_no_bill_available_to_download)
    } else {
        showToast("Start Downloading...")
        try {
            Picasso.get().load(receiptStr).into(object : Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    bitmap?.let {
                        downloadMediaToStorage(bitmap, mActivity)
                        val file = downloadBillInGallery(bitmap, orderDetailMainResponse?.orders?.orderId?.toString())
                        file?.let { showDownloadNotification(it, "Bill-#${orderDetailMainResponse?.orders?.orderId}") }
                    }
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    Log.d(TAG, "onPrepareLoad: ")
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    Log.d(TAG, "onBitmapFailed: ")
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "startDownloadBill: ${e.message}", e)
            showToast("Something went wrong")
        }
    }

    private fun askStoragePermission(): Boolean {
        mActivity?.run {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.EXTERNAL_STORAGE_REQUEST_CODE)
                return true
            }
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == Constants.EXTERNAL_STORAGE_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> startDownloadBill(orderDetailMainResponse?.orders?.digitalReceipt)
            }
        }
    }

    private fun shareBill() {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_SHARE_BILL,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
        if (mShareBillResponseStr?.isNotEmpty() == true) {
            shareDataOnWhatsAppByNumber(orderDetailMainResponse?.orders?.phone, mShareBillResponseStr)
        } else if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
        } else {
            showProgressDialog(mActivity)
            mOrderDetailService?.shareBillResponse(orderDetailMainResponse?.orders?.orderId)
        }
    }

    private fun cashCollected() {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
        } else {
            showProgressDialog(mActivity)
            mOrderDetailService?.completeOrder(CompleteOrderRequest(orderDetailMainResponse?.orders?.orderId?.toLong()))
        }
    }
}
