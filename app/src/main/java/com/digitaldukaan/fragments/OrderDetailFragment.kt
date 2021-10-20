package com.digitaldukaan.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.*
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.skydoves.balloon.showAlignTop
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.bottom_layout_send_bill.*
import kotlinx.android.synthetic.main.layout_order_detail_fragment.*
import java.io.File
import java.util.*


class OrderDetailFragment : BaseFragment(), IOrderDetailServiceInterface, PopupMenu.OnMenuItemClickListener {

    private var mOrderHash = ""
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
    private var mPromoDiscount = 0.0
    private var mIsPickUpOrder = false

    companion object {
        private const val TAG = "OrderDetailFragment"
        private const val CUSTOM = "custom"
        private var mShareBillResponseStr: String? = ""

        fun newInstance(orderHash: String, isNewOrder: Boolean): OrderDetailFragment {
            val fragment = OrderDetailFragment()
            fragment.mOrderHash = orderHash
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
            mOrderDetailService?.getOrderDetail(mOrderHash)
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
                when (orderDetailMainResponse?.sendBillAction) {
                    Constants.ACTION_HOW_TO_SHIP -> showShipmentConfirmationBottomSheet(mOrderDetailStaticData, orderDetailMainResponse?.orders?.orderId)
                    Constants.ACTION_SEND_BILL -> initiateSendBillServerCall()
                    else -> if (mIsPickUpOrder) initiateSendBillServerCall() else handleDeliveryTimeBottomSheet(isCallSendBillServerCall = true, isPrepaidOrder = false)
                }
            }
            markOutForDeliveryTextView?.id -> {
                if (Constants.DS_MARK_READY == orderDetailMainResponse?.orders?.displayStatus) {
                    showCancellableProgressDialog(mActivity)
                    mOrderDetailService?.updatePrepaidOrder(orderDetailMainResponse?.orders?.orderHash, null)
                } else handleDeliveryTimeBottomSheet(isCallSendBillServerCall = false, isPrepaidOrder = true)
            }
            detailTextView?.id -> {
                getTransactionDetailBottomSheet(orderDetailMainResponse?.orders?.transactionId, AFInAppEventParameterName.ORDER_DETAILS)
            }
            shareProductContainer?.id -> {
                showProgressDialog(mActivity)
                mOrderDetailService?.sharePaymentLink(mOrderHash)
            }
            sideIconWhatsAppToolbar?.id -> {
                val displayStatus = orderDetailMainResponse?.orders?.displayStatus
                if (getString(R.string.default_mobile) == mMobileNumber) {
                    shareOnWhatsApp("Hi, we are checking your order." , null)
                } else {
                    shareDataOnWhatsAppByNumber(mMobileNumber, if (Constants.DS_SEND_BILL == displayStatus || Constants.DS_NEW == displayStatus) "Hi, we are checking your order." else "")
                }
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
            val payAmount = if (true == amountEditText.text?.isNotEmpty()) amountEditText.text.toString().toDouble() else amount
            if (payAmount ?: 0.0 <= 0.0) {
                amountEditText.apply {
                    error = mActivity?.getString(R.string.bill_value_must_be_greater_than_zero)
                    requestFocus()
                }
                return
            }
            val deliveryChargesAmount = calculateDeliveryCharge(payAmount)
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
                if (true == otherChargesEditText.text?.isNotEmpty()) otherChargesEditText.text.toString() else "" ,
                if (true == otherChargesValueEditText.text?.isNotEmpty()) otherChargesValueEditText.text.toString().toDouble() else 0.0,
                if (true == discountsValueEditText.text?.isNotEmpty()) discountsValueEditText.text.toString().toDouble() else 0.0,
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

    private fun calculateDeliveryCharge(payAmount: Double?): Double {
        var deliveryChargesAmount = 0.0
        val storeServices = orderDetailMainResponse?.storeServices
        if (!mIsPickUpOrder) {
            if (Constants.CUSTOM_DELIVERY_CHARGE == storeServices?.mDeliveryChargeType && 0.0 == storeServices.mFreeDeliveryAbove) {
                deliveryChargesAmount = mDeliveryChargeAmount
            } else if ((Constants.CUSTOM_DELIVERY_CHARGE == storeServices?.mDeliveryChargeType && payAmount ?: 0.0 <= storeServices.mFreeDeliveryAbove) || (Constants.UNKNOWN_DELIVERY_CHARGE == storeServices?.mDeliveryChargeType)) {
                deliveryChargesAmount = mDeliveryChargeAmount
            } else if (Constants.FIXED_DELIVERY_CHARGE == storeServices?.mDeliveryChargeType) {
                deliveryChargesAmount = storeServices.mDeliveryPrice ?: 0.0
            }
        }
        return deliveryChargesAmount
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
            setupFooterLayout()
            val isUpperLayoutVisible = (true == orderDetailMainResponse?.isHeaderLayoutVisible)
            orderDetailContainer?.visibility = if (isUpperLayoutVisible) View.VISIBLE else View.GONE
            addDeliveryChargesLabel?.visibility = if (Constants.ACTION_SHARE_BILL != orderDetailMainResponse?.footerLayout && (Constants.DS_NEW == displayStatus || Constants.DS_SEND_BILL == displayStatus)) View.VISIBLE else View.GONE
            var isCreateListItemAdded = false
            isCreateListItemAdded = setupOrderDetailItemRecyclerView(orderDetailResponse, isCreateListItemAdded, displayStatus)
            amountEditText?.setText("$mTotalPayAmount")
            setStaticDataToUI(orderDetailResponse)
            setUpOrderDeliveryPartnerUI(orderDetailMainResponse)
            appSubTitleTextView?.text = getStringDateTimeFromOrderDate(getCompleteDateFromOrderString(orderDetailMainResponse?.orders?.createdAt))
            if (isCreateListItemAdded) orderDetailMainResponse?.storeServices?.mDeliveryChargeType = Constants.CUSTOM_DELIVERY_CHARGE
            setupDeliveryChargeUI(displayStatus, orderDetailMainResponse?.orders?.deliveryInfo, orderDetailMainResponse?.orders?.deliveryCharge)
            setupOrderTypeUI(orderDetailResponse, displayStatus)
            if (isNotEmpty(orderDetailResponse?.instruction)) {
                instructionsValue?.visibility = View.VISIBLE
                instructionsValue?.text = orderDetailResponse?.instruction
                instructionsLabel?.visibility = View.VISIBLE
            }
            mMobileNumber = orderDetailResponse?.phone ?: ""
            sideIcon2Toolbar?.visibility = if (getString(R.string.default_mobile) == mMobileNumber) View.GONE else View.VISIBLE
            sideIconToolbar?.visibility = if (true == orderDetailMainResponse?.optionMenuList?.isEmpty()) View.GONE else View.VISIBLE
            if (null == orderDetailMainResponse?.promoCodeDetails) {
                promoLayout?.visibility = View.GONE
            } else {
                mPromoDiscount = orderDetailMainResponse?.orders?.promoDiscount ?: 0.0
                promoLayout?.visibility = View.VISIBLE
                promoCodeTextView?.text = orderDetailMainResponse?.promoCodeDetails?.coupon?.promoCode
                val descriptionStr = if (Constants.MODE_COUPON_TYPE_FLAT == orderDetailMainResponse?.promoCodeDetails?.coupon?.discountType) {
                    "Flat ₹${orderDetailMainResponse?.promoCodeDetails?.coupon?.discount?.toInt()} OFF"
                } else {
                    "${orderDetailMainResponse?.promoCodeDetails?.coupon?.discount?.toInt()}% OFF Upto ₹${orderDetailMainResponse?.promoCodeDetails?.coupon?.maxDiscount?.toInt()}"
                }
                promoCodeMessageTextView?.text = descriptionStr
                val amount = "- ₹${mPromoDiscount}"
                promoCodeAmountTextView?.text = amount
                mActivity?.let { context ->
                    promoPercentageImageView?.let { it -> Glide.with(context).load(orderDetailMainResponse?.promoCodeDetails?.percentageCdn).into(it) }
                }
                setAmountToEditText()
            }
        }
    }

    private fun setupFooterLayout() {
        Log.d(TAG, "setupFooterLayout: ${orderDetailMainResponse?.footerLayout}")
        when (orderDetailMainResponse?.footerLayout) {
            Constants.ACTION_SHARE_BILL -> {
                deliveryPartnerShareLinkContainer?.apply {
                    visibility = View.VISIBLE
                    deliveryPartnerShareLinkHeadingTextView?.apply {
                        text = mOrderDetailStaticData?.text_delivery_person
                        setOnClickListener { showCodConfirmationBottomSheet() }
                    }
                    deliveryPartnerShareLinkCtaTextView?.apply {
                        text = mOrderDetailStaticData?.text_share_bill
                        setOnClickListener { shareBill() }
                    }
                }
            }
            Constants.ACTION_SEND_BILL -> {
                sendBillLayout?.visibility = View.VISIBLE
            }
            Constants.ACTION_SET_PREPAID_ORDER -> {
                setupPrepaidOrderUI(orderDetailMainResponse?.orders?.displayStatus, orderDetailMainResponse?.orders)
            }
            Constants.ACTION_PREPAID_ORDER -> {
                setupPrepaidOrderUI(orderDetailMainResponse?.orders?.displayStatus, orderDetailMainResponse?.orders)
            }
        }

    }

    private fun setUpOrderDeliveryPartnerUI(orderMainResponse: OrderDetailMainResponse?) {
        mActivity?.let { context ->
            orderMainResponse?.let { response ->
                deliveryPartnerLayout?.visibility = View.GONE
                prepaidDeliveryPartnerLayout?.visibility = View.GONE
                visitDotPeLayout?.visibility = View.GONE
                when {
                    true == response.deliveryDetails?.isEnabled -> {
                        Log.d(TAG, "setUpOrderDeliveryPartnerUI: response.deliveryDetails :: ${response.deliveryDetails}")
                        deliveryPartnerLayout?.visibility = View.VISIBLE
                        deliveryPartnerStatusTextView?.text = response.deliveryDetails?.deliveryDisplayStatus
                        deliveryPartnerMessageTextView?.text = response.deliveryDetails?.deliveryStatusSubHeading
                        deliveryPartnerShareTextView?.apply {
                            visibility = if (true == response.deliveryDetails?.cta?.isEnabled) View.VISIBLE else View.GONE
                            text = response.deliveryDetails?.textShare
                            setOnClickListener { shareOnWhatsApp(response.deliveryDetails?.shareWaMessage) }
                        }
                        deliveryPartnerCtaTextView?.apply {
                            alpha = if (true == response.deliveryDetails?.cta?.isEnabled) 1f else 0.3f
                            text = response.deliveryDetails?.cta?.text
                            setTextColor(Color.parseColor(if (isEmpty(response.deliveryDetails?.cta?.textColor)) "#000000" else response.deliveryDetails?.cta?.textColor))
                            background = ContextCompat.getDrawable(context, if (Constants.CTA_TYPE_BORDER == response.deliveryDetails?.cta?.type) R.drawable.curve_black_border else R.drawable.curve_black_background)
                            setOnClickListener {
                                when {
                                    false == response.deliveryDetails?.cta?.isEnabled -> {
                                        val toolTipBalloon = getToolTipBalloon(context, response.deliveryDetails?.cta?.messageTooltip, 0.75f)
                                        toolTipBalloon?.let { b -> this.showAlignTop(b) }
                                    }
                                    Constants.NEW_RELEASE_TYPE_EXTERNAL == response.deliveryDetails?.cta?.action -> {
                                        openUrlInBrowser(response.deliveryDetails?.cta?.url)
                                    }
                                    else -> {

                                    }
                                }
                            }
                        }
                        deliveryPartnerContainer?.setBackgroundColor(Color.parseColor(if (isEmpty(response.deliveryDetails?.outerBgColor)) "#000000" else response.deliveryDetails?.outerBgColor))
                        deliveryPartnerInnerLayout?.setBackgroundColor(Color.parseColor(if (isEmpty(response.deliveryDetails?.innerBgColor)) "#FFFFFF" else response.deliveryDetails?.innerBgColor))
                        deliveryPartnerImageView?.let { view -> Glide.with(context).load(response.deliveryDetails?.deliveryPartnerImageUrl).into(view)}
                    }
                    true == response.prepaidDeliveryDetails?.isEnabled -> {
                        Log.d(TAG, "setUpOrderDeliveryPartnerUI: response.prepaidDeliveryDetails :: ${response.prepaidDeliveryDetails}")
                        prepaidDeliveryPartnerLayout?.visibility = View.VISIBLE
                        prepaidDeliveryPartnerStatusTextView?.text = response.prepaidDeliveryDetails?.deliveryDisplayStatus
                        prepaidDeliveryPartnerMessageTextView?.text = response.prepaidDeliveryDetails?.deliveryStatusSubHeading
                        response.prepaidDeliveryDetails?.cta1?.let { cta ->
                            prepaidDeliveryPartnerCta1TextView?.apply {
                                text = cta.text
                                setTextColor(Color.parseColor(if (isEmpty(cta.textColor)) "#000000" else cta.textColor))
                                background = ContextCompat.getDrawable(context, if (Constants.CTA_TYPE_BORDER == cta.type) R.drawable.curve_black_border else R.drawable.curve_black_background)
                                setOnClickListener { showTrendingOffersBottomSheet(response.prepaidDeliveryDetails) }
                            }
                        }
                        response.prepaidDeliveryDetails?.cta2?.let { cta ->
                            prepaidDeliveryPartnerCta2TextView?.apply {
                                text = cta.text
                                setTextColor(Color.parseColor(if (isEmpty(cta.textColor)) "#000000" else cta.textColor))
                                background = ContextCompat.getDrawable(context, if (Constants.CTA_TYPE_BORDER == cta.type) R.drawable.curve_black_border else R.drawable.curve_black_background)
                                setOnClickListener {
                                    if (Constants.ACTION_MARK_OUT_FOR_DELIVERY == cta.action) handleDeliveryTimeBottomSheet(isCallSendBillServerCall = false, isPrepaidOrder = true)
                                }
                            }
                        }
                        deliveryPartnerContainer?.setBackgroundColor(Color.parseColor(if (isEmpty(response.prepaidDeliveryDetails?.outerBgColor)) "#000000" else response.prepaidDeliveryDetails?.outerBgColor))
                        prepaidDeliveryPartnerInnerLayout?.setBackgroundColor(Color.parseColor(if (isEmpty(response.prepaidDeliveryDetails?.innerBgColor)) "#FFFFFF" else response.prepaidDeliveryDetails?.innerBgColor))
                        prepaidDeliveryPartnerImageView?.let { view -> Glide.with(context).load(response.prepaidDeliveryDetails?.deliveryPartnerImageUrl).into(view)}
                    }
                    true == response.deliveryPartnerDetails?.isEnabled-> {
                        Log.d(TAG, "setUpOrderDeliveryPartnerUI: response.deliveryPartnerDetails :: ${response.deliveryPartnerDetails}")
                        visitDotPeLayout?.visibility = View.VISIBLE
                        visitDotPeTextView?.setHtmlData(response.deliveryPartnerDetails?.deliveryDisplayStatus)
                        deliveryPartnerContainer?.setBackgroundColor(Color.parseColor(if (isEmpty(response.deliveryPartnerDetails?.outerBgColor)) "#000000" else response.deliveryPartnerDetails?.outerBgColor))
                        visitDotPeInnerLayout?.setBackgroundColor(Color.parseColor(if (isEmpty(response.deliveryPartnerDetails?.innerBgColor)) "#FFFFFF" else response.deliveryPartnerDetails?.innerBgColor))
                        response.deliveryPartnerDetails?.cta?.let { cta ->
                            visitDotPeCtaTextView?.apply {
                                text = cta.text
                                setTextColor(Color.parseColor(if (isEmpty(cta.textColor)) "#000000" else cta.textColor))
                                background = ContextCompat.getDrawable(context, if (Constants.CTA_TYPE_BORDER == cta.type) R.drawable.curve_black_border else R.drawable.curve_black_background)
                                setOnClickListener {
                                    copyDataToClipboard(Constants.DOTPE_OFFICIAL_URL_CLIPBOARD)
                                }
                            }
                        }
                    }
                    else -> {

                    }
                }
            }
        }
    }

    private fun showTrendingOffersBottomSheet(prepaidDeliveryDetails: OrderDetailPrepaidDeliveryResponse?) {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_DELIVERY_SHIP_USING_PARTNER,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.ORDER_ID to "${orderDetailMainResponse?.orders?.orderId}"
            )
        )
        mActivity?.let { context ->
            val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_trending_offers, context.findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                val bottomSheetHeadingTextView: TextView = view.findViewById(R.id.bottomSheetHeadingTextView)
                val bottomSheetUrl: TextView = view.findViewById(R.id.bottomSheetUrl)
                val bottomSheetClose: View = view.findViewById(R.id.bottomSheetClose)
                bottomSheetHeadingTextView.setHtmlData(prepaidDeliveryDetails?.headingBottomSheet)
                bottomSheetUrl.setHtmlData(prepaidDeliveryDetails?.messageGotoWebsite)
                bottomSheetUrl.setOnClickListener { copyDataToClipboard(Constants.DOTPE_OFFICIAL_URL_CLIPBOARD) }
                bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
            }.show()
        }
    }

    private fun showCodConfirmationBottomSheet() {
        mActivity?.let { context ->
            val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_cod_confirmation, context.findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                val textView: TextView = view.findViewById(R.id.textView)
                val okayTextView: TextView = view.findViewById(R.id.okayTextView)
                textView.setHtmlData(mOrderDetailStaticData?.message_cod)
                okayTextView.setHtmlData(mOrderDetailStaticData?.text_okay)
                okayTextView.setOnClickListener { bottomSheetDialog.dismiss() }
            }.show()
        }
    }

    private fun setupOrderTypeUI(orderDetailResponse: OrderDetailsResponse?, displayStatus: String?) {
        when (orderDetailResponse?.orderType) {
            Constants.ORDER_TYPE_SELF_IMAGE -> {
                blankSpace?.visibility = View.GONE
                deliveryLabelLayout?.visibility = View.GONE
                customerDetailsLabel?.visibility = View.GONE
                if (Constants.DS_PENDING_PAYMENT_LINK == displayStatus && isEmpty(orderDetailResponse.imageLink)) {
                    reminderContainer?.visibility = View.VISIBLE
                } else {
                    if (isEmpty(orderDetailResponse.imageLink)) {
                        getTransactionDetailBottomSheet(orderDetailMainResponse?.orders?.transactionId)
                    } else {
                        billPhotoImageView?.let { view ->
                            view.visibility = View.VISIBLE
                            Glide.with(this).load(orderDetailResponse.imageLink).into(view)
                        }
                    }
                }
            }
            Constants.ORDER_TYPE_SELF -> customerDetailsLabel?.visibility = View.GONE
            Constants.ORDER_TYPE_PICK_UP -> {
                if (isNotEmpty(orderDetailResponse.imageLink)) {
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
                if (isNotEmpty(orderDetailResponse?.imageLink)) {
                    viewBillTextView?.visibility = View.VISIBLE
                    viewBillTextView?.setOnClickListener { showImageDialog(orderDetailResponse?.imageLink) }
                }
                mIsPickUpOrder = false
                customerDeliveryDetailsRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    val customerDetailsList = ArrayList<CustomerDeliveryAddressDTO>()
                    orderDetailResponse?.deliveryInfo?.run {
                        customerDetailsList.add(CustomerDeliveryAddressDTO("${mOrderDetailStaticData?.text_name}:", deliverTo))
                        customerDetailsList.add(CustomerDeliveryAddressDTO("${mOrderDetailStaticData?.text_address}:", "$address1,$address2"))
                        customerDetailsList.add(
                            CustomerDeliveryAddressDTO("${mOrderDetailStaticData?.text_city_and_pincode}:",
                                if (null == city) {
                                    if (null == pincode) "" else "$pincode"
                                } else if (null == pincode) {
                                    "$city"
                                } else {
                                    "$city, $pincode"
                                }
                            )
                        )
                        customerDetailsList.add(CustomerDeliveryAddressDTO("${mOrderDetailStaticData?.text_landmark}:", landmark))
                    }
                    adapter = CustomerDeliveryAddressAdapter(customerDetailsList)
                }
            }
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
            if (orderDetailResponse?.deliveryCharge != 0.0 && !(Constants.DS_NEW == deliveryStatus || Constants.DS_SEND_BILL == deliveryStatus)) {
                list?.add(OrderDetailItemResponse(0, 0, getString(R.string.delivery_charge), 1, "1", orderDetailResponse.deliveryCharge, orderDetailResponse.deliveryCharge, orderDetailResponse.deliveryCharge, orderDetailResponse.deliveryCharge, 1, Constants.ITEM_TYPE_DELIVERY_CHARGE, null, 0, null, false))
            }
            if (orderDetailResponse?.extraCharges != 0.0 && !(Constants.DS_NEW == deliveryStatus || Constants.DS_SEND_BILL == deliveryStatus)) {
                list?.add(OrderDetailItemResponse(0, 0, orderDetailResponse.extraChargesName, 1, "1", orderDetailResponse.extraCharges, orderDetailResponse.extraCharges, orderDetailResponse.extraCharges, orderDetailResponse.extraCharges, 1, Constants.ITEM_TYPE_CHARGE, null, 0, null, false))
            }
            if (orderDetailResponse?.discount != 0.0 && !(Constants.DS_NEW == deliveryStatus || Constants.DS_SEND_BILL == deliveryStatus)) {
                list?.add(OrderDetailItemResponse(0, 0, getString(R.string.discount), 1, "1", orderDetailResponse.discount, orderDetailResponse.discount, orderDetailResponse.discount, orderDetailResponse.discount, 1, Constants.ITEM_TYPE_DISCOUNT, null, 0, null, false))
            }
            list?.forEachIndexed { _, itemResponse ->
                itemResponse.isItemEditable = (((Constants.DS_NEW == deliveryStatus || Constants.DS_SEND_BILL == deliveryStatus)) && 0.0 == itemResponse.amount)
                if (itemResponse.isItemEditable) isCreateListItemAdded1 = true
                mTotalPayAmount += itemResponse.amount ?: 0.0
                mTotalActualAmount += ((itemResponse.actualAmount ?: 0.0) * itemResponse.quantity)
            }
            var orderDetailAdapter: OrderDetailsAdapter? = null
            orderDetailAdapter = OrderDetailsAdapter(list, orderDetailResponse, displayStatus, mOrderDetailStaticData, object : IOrderDetailListener {

                    override fun onOrderDetailItemClickListener(position: Int) {
                        if (position < 0) return
                        val item = list?.get(position)
                        item?.item_status = if (2 == list?.get(position)?.item_status) 1 else 2
                        if (2 == item?.item_status) mTotalPayAmount -= item.amount ?: 0.0 else mTotalPayAmount += item?.amount ?: 0.0
                        if (2 == item?.item_status) mTotalActualAmount -= ((item.actualAmount ?: 0.0) * item.quantity) else mTotalActualAmount += ((item?.actualAmount ?: 0.0) * item?.quantity!!)
                        orderDetailAdapter?.setOrderDetailList(list)
                        setAmountToEditText()
                        var isValidOrderAvailable = false
                        list?.forEachIndexed { _, itemResponse -> if (2 != itemResponse.item_status) isValidOrderAvailable = true }
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
        if (Constants.ORDER_TYPE_PREPAID == orderDetailMainResponse?.orders?.prepaidFlag) {
            sendBillLayout?.visibility = View.GONE
            prepaidOrderLayout?.visibility = View.VISIBLE
            Log.d(TAG, "setupPrepaidOrderUI: displayStatus :: $displayStatus")
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
                    prepaidOrderLayout?.visibility = View.VISIBLE
                    markOutForDeliveryValueTextView?.visibility = View.VISIBLE
                    markOutForDeliveryTextView?.visibility = View.GONE
                    val txtSpannable = SpannableString(mOrderDetailStaticData?.pickUpOrderSuccess)
                    val boldSpan = StyleSpan(Typeface.BOLD)
                    txtSpannable.setSpan(boldSpan, 0, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    markOutForDeliveryValueTextView?.text = txtSpannable
                }
                Constants.DS_PREPAID_DELIVERY_READY -> {
                    prepaidOrderLayout?.visibility = View.VISIBLE
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

    private fun setupDeliveryChargeUI(displayStatus: String?, deliveryInfo: DeliveryInfoItemResponse?, deliveryCharge: Double?) {
        Log.d(TAG, "setupDeliveryChargeUI: deliveryInfo?.type :: ${deliveryInfo?.type}")
        when(deliveryInfo?.type) {
            Constants.FREE_DELIVERY -> setFreeDelivery()
            Constants.FIXED_DELIVERY_CHARGE -> setFixedDeliveryChargeUI(displayStatus, deliveryCharge)
            Constants.CUSTOM_DELIVERY_CHARGE -> setCustomDeliveryChargeUI(displayStatus)
            Constants.UNKNOWN_DELIVERY_CHARGE -> setCustomDeliveryChargeUI(displayStatus)
            else -> setCustomDeliveryChargeUI(displayStatus)
        }
        deliveryChargeValueEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val str = editable?.toString()
                mDeliveryChargeAmount = if (true == str?.isNotEmpty()) { str.toDouble() } else 0.0
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
                mOtherChargeAmount = if (true == str?.isNotEmpty()) { str.toDouble() } else 0.0
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
                mDiscountAmount = if (true == str?.isNotEmpty()) { str.toDouble() } else 0.0
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

    private fun setCustomDeliveryChargeUI(displayStatus: String?) {
        if (Constants.DS_SEND_BILL == displayStatus || Constants.DS_NEW == displayStatus) {
            deliveryChargeLabel?.visibility = View.VISIBLE
            deliveryChargeValue?.visibility = View.VISIBLE
            deliveryChargeValueEditText?.visibility = View.VISIBLE
        } else {
            deliveryChargeLabel?.visibility = View.GONE
            deliveryChargeValue?.visibility = View.GONE
            deliveryChargeValueEditText?.visibility = View.GONE
        }
        addDeliveryChargesLabel?.text = getString(R.string.add_discount_and_other_charges)
    }

    private fun setFixedDeliveryChargeUI(displayStatus: String?, deliveryCharge: Double?) {
        if (Constants.DS_SEND_BILL == displayStatus || Constants.DS_NEW == displayStatus) {
            mDeliveryChargeAmount = deliveryCharge ?: 0.0
            deliveryChargeLabel?.visibility = View.VISIBLE
            deliveryChargeValue?.visibility = View.VISIBLE
            addDeliveryChargesLabel?.text = getString(R.string.add_discount_and_other_charges)
            deliveryChargeValue?.text = "$deliveryCharge"
            setAmountToEditText()
        } else {
            deliveryChargeLabel?.visibility = View.GONE
            deliveryChargeValue?.visibility = View.GONE
        }
    }

    private fun setFreeDelivery() {
        try {
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
        } catch (e: Exception) {
            Log.e(TAG, "setFreeDelivery: ${e.message}", e)
        }
    }

    private fun setAmountToEditText() {
        val amount = mTotalPayAmount + mDeliveryChargeAmount + mOtherChargeAmount - mDiscountAmount - mPromoDiscount
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
            if (isEmpty(orderDetailResponse?.transactionId))
                detailTextView?.visibility = View.INVISIBLE
            else {
                detailTextView?.visibility = View.VISIBLE
                detailTextView?.text = text_details
            }
            billAmountValue?.text = "$text_rupees_symbol ${orderDetailResponse?.payAmount}"
            appTitleTextView?.text = "$text_order #${orderDetailResponse?.orderId}"
            if (Constants.ORDER_TYPE_PREPAID == orderDetailResponse?.prepaidFlag || true == orderDetailResponse?.deliveryInfo?.customDeliveryTime?.isEmpty()) {
                estimateDeliveryTextView?.visibility = View.GONE
            } else {
                val estimatedDeliveryStr = "$text_estimate_delivery : ${orderDetailResponse?.deliveryInfo?.customDeliveryTime}"
                estimateDeliveryTextView?.text = estimatedDeliveryStr
            }
            statusValue?.text = orderDetailResponse?.orderPaymentStatus?.value
            mActivity?.let {context ->
                Log.d(TAG, "setStaticDataToUI: orderDetailResponse?.orderPaymentStatus?.key ${orderDetailResponse?.orderPaymentStatus?.key}")
                when (orderDetailResponse?.orderPaymentStatus?.key) {
                    Constants.ORDER_STATUS_REJECTED -> orderDetailContainer?.setBackgroundColor(ContextCompat.getColor(context, R.color.red))
                    Constants.ORDER_STATUS_IN_PROGRESS -> orderDetailContainer?.setBackgroundColor(ContextCompat.getColor(context, R.color.order_detail_in_progress))
                    Constants.ORDER_STATUS_SUCCESS -> orderDetailContainer?.setBackgroundColor(ContextCompat.getColor(context, R.color.open_green))
                    else -> orderDetailContainer?.setBackgroundColor(ContextCompat.getColor(context, R.color.black))
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
                if (!PrefsManager.getBoolDataFromSharedPref(PrefsManager.KEY_FIRST_ITEM_COMPLETED)) {
                    PrefsManager.storeBoolDataInSharedPref(PrefsManager.KEY_FIRST_ITEM_COMPLETED, true)
                    mActivity?.launchInAppReviewDialog()
                }
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

    override fun onSharePaymentLinkResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            shareOnWhatsApp(Gson().fromJson<String>(commonResponse.mCommonDataStr, String::class.java))
        }
    }

    override fun onOrderDetailStatusResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val listType = object : TypeToken<List<OrderDetailTransactionItemResponse?>>() {}.type
                val txnItemList = Gson().fromJson<ArrayList<OrderDetailTransactionItemResponse?>>(commonResponse.mCommonDataStr, listType)
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
                mOrderDetailService?.getOrderDetail(mOrderHash)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onImageSelectionResultFile(file: File?, mode: String) {
        val extraChargeName = if (true == otherChargesEditText.text?.isNotEmpty()) otherChargesEditText.text.toString() else ""
        val extraCharge = if (true == otherChargesValueEditText.text?.isNotEmpty()) otherChargesValueEditText.text.toString().toDouble() else 0.0
        val discount = if (true == discountsValueEditText.text?.isNotEmpty()) discountsValueEditText.text.toString().toDouble() else 0.0
        val payAmount = if (true == amountEditText.text?.isNotEmpty()) amountEditText.text.toString().toDouble() else orderDetailMainResponse?.orders?.amount
        val deliveryChargesAmount = calculateDeliveryCharge(payAmount)
        launchFragment(SendBillPhotoFragment.newInstance(orderDetailMainResponse, file, mDeliveryTimeStr, extraChargeName, extraCharge, discount, payAmount, deliveryChargesAmount, deliveryTimeResponse), true)
    }

    override fun onOrderDetailException(e: Exception) = exceptionHandlingForAPIResponse(e)

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

                                override fun onChipItemClickListener(position: Int) = onDeliveryTimeItemClickListener(deliveryTimeResponse, position, bottomSheetSendBillText)

                            })
                            adapter = mDeliveryTimeAdapter
                        }
                    }
                    bottomSheetSendBillText.setOnClickListener {
                        if (true == mDeliveryTimeStr?.equals(CUSTOM, true)) {
                            mDeliveryTimeStr = deliveryTimeEditText?.text.toString()
                            if (true == mDeliveryTimeStr?.isEmpty()) {
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
        if (View.VISIBLE == deliveryTimeEditText?.visibility) {
            deliveryTimeEditText?.requestFocus()
            deliveryTimeEditText?.showKeyboard()
        }
        mDeliveryTimeAdapter?.setDeliveryTimeList(deliveryTimeResponse.deliveryTimeList)
        mDeliveryTimeStr = deliveryTimeResponse.deliveryTimeList?.get(position)?.value
        bottomSheetSendBillText.isEnabled = true
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

    private fun startDownloadBill(receiptStr: String?) = if (true == receiptStr?.isEmpty()) {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (Constants.EXTERNAL_STORAGE_REQUEST_CODE == requestCode) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                PackageManager.PERMISSION_GRANTED == grantResults[0] -> startDownloadBill(orderDetailMainResponse?.orders?.digitalReceipt)
            }
        }
    }

    private fun shareBill() {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_SHARE_BILL,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
        if (isNotEmpty(mShareBillResponseStr)) {
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

    override fun onTransactionDetailResponse(response: TransactionDetailResponse?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            detailTextView?.visibility = View.INVISIBLE
            transactionDetailLayout?.visibility = View.VISIBLE
            mContentView?.run {
                val billAmountValueTextView: TextView = findViewById(R.id.billAmountValueTextView)
                val txnChargeValueTextView: TextView = findViewById(R.id.txnChargeValueTextView)
                val textViewTop: TextView = findViewById(R.id.textViewTop)
                val textViewBottom: TextView = findViewById(R.id.textViewBottom)
                val billAmountTextView: TextView = findViewById(R.id.billAmountTextView)
                val txnChargeTextView: TextView = findViewById(R.id.txnChargeTextView)
                val paymentModeTextView: TextView = findViewById(R.id.paymentModeTextView)
                val amountSettleTextView: TextView = findViewById(R.id.amountSettleTextView)
                val amountSettleValueTextView: TextView = findViewById(R.id.amountSettleValueTextView)
                val txnId: TextView = findViewById(R.id.txnId)
                val bottomDate: TextView = findViewById(R.id.bottomDate)
                val displayMessage: TextView = findViewById(R.id.displayMessage)
                val ctaTextView: TextView = findViewById(R.id.ctaTextView)
                val imageViewBottom: ImageView = findViewById(R.id.imageViewBottom)
                val paymentModeImageView: ImageView = findViewById(R.id.paymentModeImageView)

                val staticText = response?.staticText
                textViewTop.text = response?.transactionMessage
                textViewBottom.text = response?.settlementMessage
                billAmountTextView.text = staticText?.bill_amount
                amountSettleTextView.text = staticText?.amount_to_settled
                paymentModeTextView.text = staticText?.payment_mode
                txnId.text = getStringDateTimeFromTransactionDetailDate(getCompleteDateFromOrderString(response?.transactionTimestamp))
                when (Constants.ORDER_STATUS_PAYOUT_SUCCESS) {
                    response?.settlementState -> {
                        bottomDate.visibility = View.VISIBLE
                        val bottomDisplayStr = "${getStringDateTimeFromTransactionDetailDate(getCompleteDateFromOrderString(response.settlementTimestamp))} ${if (!isEmpty(response.utr)) "| UTR : ${response.utr}" else ""}"
                        bottomDate.text = bottomDisplayStr
                    }
                    else -> bottomDate.visibility = View.GONE
                }
                if (null != response?.ctaItem) {
                    displayMessage.text = response.ctaItem?.displayMessage
                    ctaTextView.visibility = View.VISIBLE
                    displayMessage.visibility = View.VISIBLE
                    ctaTextView.setOnClickListener {
                        when(response.ctaItem?.action) {
                            Constants.ACTION_ADD_BANK -> {
                                launchFragment(BankAccountFragment.newInstance(null, 0, false, null), false)
                            }
                        }
                    }
                } else {
                    ctaTextView.visibility = View.INVISIBLE
                    displayMessage.visibility = View.GONE
                }
                txnChargeTextView.text = "${staticText?.transaction_charges} (${response?.transactionCharges}%)"
                billAmountValueTextView.text = "${getString(R.string.rupee_symbol)} ${response?.amount}"
                txnChargeValueTextView.text = "${getString(R.string.rupee_symbol)} ${response?.transactionChargeAmount}"
                amountSettleValueTextView.text = "${getString(R.string.rupee_symbol)} ${response?.settlementAmount}"
                if (!isEmpty(response?.paymentImage)) mActivity?.let { context -> Glide.with(context).load(response?.paymentImage).into(paymentModeImageView) }
                if (!isEmpty(response?.settlementCdn)) mActivity?.let { context -> Glide.with(context).load(response?.settlementCdn).into(imageViewBottom) }
            }
        }
    }

    override fun onShipmentCtaClicked(initiateServerCall: Boolean) = if (initiateServerCall) initiateSendBillServerCall() else handleDeliveryTimeBottomSheet(isCallSendBillServerCall = true, isPrepaidOrder = false)
}
