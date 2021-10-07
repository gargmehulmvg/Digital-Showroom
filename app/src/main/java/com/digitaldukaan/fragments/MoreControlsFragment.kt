package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.MoreControlsRequest
import com.digitaldukaan.models.request.StoreDeliveryStatusChangeRequest
import com.digitaldukaan.models.response.AccountInfoResponse
import com.digitaldukaan.models.response.AccountStaticTextResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.StoreServicesResponse
import com.digitaldukaan.services.MoreControlsService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IMoreControlsServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_more_control_fragment.*


class MoreControlsFragment : BaseFragment(), IMoreControlsServiceInterface {

    private var mMoreControlsStaticData: AccountStaticTextResponse? = null
    private var mMoreControlsService: MoreControlsService? = null
    private var mMinOrderValue = 0.0
    private var mDeliveryPrice = 0.0
    private var mFreeDeliveryAbove = 0.0
    private var mDeliveryChargeType = 0
    private var mPaymentPaymentMethod: String? = ""
    private var mIsOrderNotificationOn: Boolean = false
    private var mIsOnlinePaymentModeLocked: Boolean = false
    private var mIsPrepaidOrdersLocked: Boolean = false
    private var mIsDeliveryOn: Boolean = false
    private var mIsPickupOn: Boolean = false
    private var mIsStoreOn: Boolean = false
    private var mAccountInfoResponse: AccountInfoResponse? = null

    companion object {

        fun newInstance(appSettingsResponseStaticData: AccountStaticTextResponse?, accountInfoResponse: AccountInfoResponse?): MoreControlsFragment {
            val fragment = MoreControlsFragment()
            fragment.mMoreControlsStaticData = appSettingsResponseStaticData
            fragment.mAccountInfoResponse = accountInfoResponse
            fragment.mIsOrderNotificationOn = accountInfoResponse?.mIsOrderNotificationOn ?: false
            fragment.mIsPrepaidOrdersLocked = accountInfoResponse?.mPrepaidOrdersLocked?.mIsActive ?: false
            fragment.mIsOnlinePaymentModeLocked = accountInfoResponse?.mOnlinePaymentModesOn?.mIsActive ?: false
            return fragment
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_more_control_fragment, container, false)
        mMoreControlsService = MoreControlsService()
        mMoreControlsService?.setServiceInterface(this)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            setHeaderTitle(mMoreControlsStaticData?.mTextStoreControls)
            onBackPressed(this@MoreControlsFragment)
            hideBackPressFromToolBar(mActivity, false)
        }
        hideBottomNavigationView(true)
        updateStoreServiceInstances()
        setUIDataFromResponse()
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
    }

    private fun updateStoreServiceInstances() {
        StaticInstances.sAppStoreServicesResponse?.apply {
            this@MoreControlsFragment.mMinOrderValue = mMinOrderValue ?: 0.0
            this@MoreControlsFragment.mDeliveryPrice = mDeliveryPrice ?: 0.0
            this@MoreControlsFragment.mFreeDeliveryAbove = mFreeDeliveryAbove
            this@MoreControlsFragment.mDeliveryChargeType = mDeliveryChargeType ?: 0
            this@MoreControlsFragment.mPaymentPaymentMethod = StaticInstances.sPaymentMethodStr ?: ""
            mActivity?.let { context ->
                if (1 == mDeliveryFlag) {
                    mIsDeliveryOn = true
                    deliveryStatusValueTextView?.text = mMoreControlsStaticData?.mOnText
                    deliveryStatusValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.open_green))
                    deliveryImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_delivery_on))
                } else {
                    mIsDeliveryOn = false
                    deliveryStatusValueTextView?.text = mMoreControlsStaticData?.mOffText
                    deliveryStatusValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.red))
                    deliveryImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_delivery_off))
                }
                if (1 == mPickupFlag) {
                    mIsPickupOn = true
                    pickupStatusValueTextView?.text = mMoreControlsStaticData?.mOnText
                    pickupStatusValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.open_green))
                    pickupImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pickup_green))
                } else {
                    mIsPickupOn = false
                    pickupStatusValueTextView?.text = mMoreControlsStaticData?.mOffText
                    pickupStatusValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.red))
                    pickupImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pickup_red))
                }
                val storeStatus = "${mMoreControlsStaticData?.mStoreText} :"
                storeStatusTextView?.text = storeStatus
                if (1 == mStoreFlag) {
                    mIsStoreOn = true
                    storeStatusTextView2?.text = mMoreControlsStaticData?.mOpenText
                    storeStatusTextView2?.setTextColor(ContextCompat.getColor(context, R.color.open_green))
                    storeImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_open))
                } else {
                    mIsStoreOn = false
                    storeStatusTextView2?.text = mMoreControlsStaticData?.mClosedText
                    storeStatusTextView2?.setTextColor(ContextCompat.getColor(context, R.color.red))
                    storeImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_closed))
                }
                val deliveryStatus = "${mMoreControlsStaticData?.mDeliveryText} :"
                val pickUpStr = "${mMoreControlsStaticData?.text_pickup} :"
                deliveryStatusTextView?.text = deliveryStatus
                pickupStatusTextView?.text = pickUpStr
                deliveryStatusValueTextView?.text = if (1 == mDeliveryFlag) mMoreControlsStaticData?.mOnText else mMoreControlsStaticData?.mOffText
                deliveryStatusValueTextView?.setTextColor(ContextCompat.getColor(context, if (mDeliveryFlag == 1) R.color.open_green else R.color.red))
                storeStatusTextView2?.setTextColor(ContextCompat.getColor(context, if (mStoreFlag == 1) R.color.open_green else R.color.red))
            }
        }
    }

    private fun setUIDataFromResponse() {
        val orderNotificationGroup: View? = mContentView?.findViewById(R.id.orderNotificationGroup)
        orderNotificationGroup?.visibility = if (mIsOrderNotificationOn) View.VISIBLE else View.GONE
        onlinePaymentsLockGroup?.visibility = if (mIsPrepaidOrdersLocked) {
            onlinePaymentsUnlockNowTextView?.text = mMoreControlsStaticData?.text_unlock_now
            View.VISIBLE
        } else View.GONE
        paymentModeLockGroup?.visibility = if (mIsOnlinePaymentModeLocked) {
            paymentModesUnlockNowTextView?.text = mMoreControlsStaticData?.text_unlock_now
            View.VISIBLE
        } else View.GONE
        minOrderValueHeadingTextView?.text = if (0.0 == mMinOrderValue) mMoreControlsStaticData?.heading_set_min_order_value_for_delivery else mMoreControlsStaticData?.heading_edit_min_order_value
        minOrderValueOptionalTextView?.text = if (0.0 == mMinOrderValue) mMoreControlsStaticData?.text_optional else "${mMoreControlsStaticData?.sub_heading_success_set_min_order_value_for_delivery} "
        minOrderValueAmountTextView?.text = if (0.0 != mMinOrderValue) "${mMoreControlsStaticData?.text_ruppee_symbol} $mMinOrderValue" else ""
        deliveryChargeHeadingTextView?.text = mMoreControlsStaticData?.heading_set_delivery_charge
        deliveryChargeTypeTextView?.text = mMoreControlsStaticData?.sub_heading_set_delivery_charge
        onlinePaymentsTextView?.text = mMoreControlsStaticData?.text_online_payments
        deliveryHeadingTextView?.text = mMoreControlsStaticData?.mDeliveryText
        onlinePaymentsHeadingTextView?.text = mMoreControlsStaticData?.heading_set_orders_to_online_payments
        paymentModesHeadingTextView?.text = mMoreControlsStaticData?.heading_set_online_payment_modes
        paymentModesOptionalTextView?.text = mMoreControlsStaticData?.message_set_online_payment_modes
        onlinePaymentsOptionalTextView?.text = mMoreControlsStaticData?.text_type_colon
        onlinePaymentsValueAmountTextView?.text = mPaymentPaymentMethod
        notificationsTextView?.text = mMoreControlsStaticData?.mNotificationText
        notificationsHeadingTextView?.text = mMoreControlsStaticData?.mHeadingSetOrderNotifications
        notificationsOptionalTextView?.text = mMoreControlsStaticData?.mMessageSetOrderNotifications
        paymentModesNewTextView?.text = mMoreControlsStaticData?.mNewText
        notificationsNewTextView?.text = mMoreControlsStaticData?.mNewText
        storeServiceContainerHeading?.text = mMoreControlsStaticData?.heading_tap_the_icon
        if (0 != mDeliveryChargeType) {
            deliveryChargeTypeTextView?.text = mMoreControlsStaticData?.sub_heading_success_set_delivery_charge
            if (1 == mDeliveryChargeType) {
                deliveryChargeRateTextView?.visibility = View.GONE
                deliveryChargeRateValueTextView?.visibility = View.GONE
            } else{
                deliveryChargeRateTextView?.text = mMoreControlsStaticData?.sub_heading_success_set_delivery_charge_amount
                val deliveryChargeStr = " ${mMoreControlsStaticData?.text_ruppee_symbol} $mFreeDeliveryAbove"
                deliveryChargeRateValueTextView?.text = deliveryChargeStr
            }
            deliveryChargeTypeValueTextView?.text = when (mDeliveryChargeType) {
                Constants.FREE_DELIVERY -> mMoreControlsStaticData?.heading_free_delivery
                Constants.FIXED_DELIVERY_CHARGE -> mMoreControlsStaticData?.heading_fixed_delivery_charge
                Constants.CUSTOM_DELIVERY_CHARGE -> mMoreControlsStaticData?.heading_custom_delivery_charge
                else -> ""
            }
        }
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            minOrderValueContainer?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SET_MIN_ORDER_VALUE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                showMinimumDeliveryOrderBottomSheet()
            }
            deliveryChargeContainer?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SET_DELIVERY_CHARGE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                mMoreControlsStaticData?.let { staticData -> launchFragment(SetDeliveryChargeFragment.newInstance(staticData), true) }
            }
            onlinePaymentsContainer?.id -> {
                if (mIsPrepaidOrdersLocked) {
                    openSubscriptionLockedUrlInBrowser(mAccountInfoResponse?.mPrepaidOrdersLocked?.mUrl ?: "")
                    return
                }
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SET_PREPAID_ORDER,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.PATH to AFInAppEventParameterName.MORE_CONTROLS)
                )
                launchFragment(SetOrderTypeFragment.newInstance(), true)
            }
            paymentModesContainer?.id -> {
                if (mIsOnlinePaymentModeLocked) {
                    openSubscriptionLockedUrlInBrowser(mAccountInfoResponse?.mOnlinePaymentModesOn?.mUrl ?: "")
                    return
                }
                launchFragment(PaymentModesFragment.newInstance(), true)
            }
            notificationsContainer?.id -> getOrderNotificationBottomSheet(AFInAppEventParameterName.STORE_CONTROLS)
            storeImageView?.id -> {
                startViewAnimation(storeImageView)
                changeStoreDeliveryPickUpStatus(isStoreClicked = true)
            }
            storeStatusTextView?.id -> {
                startViewAnimation(storeImageView)
                changeStoreDeliveryPickUpStatus(isStoreClicked = true)
            }
            storeStatusTextView2?.id -> {
                startViewAnimation(storeImageView)
                changeStoreDeliveryPickUpStatus(isStoreClicked = true)
            }
            deliveryImageView?.id -> {
                startViewAnimation(deliveryImageView)
                changeStoreDeliveryPickUpStatus(isStoreClicked = false, isDeliveryClicked = true)
            }
            deliveryStatusTextView?.id -> {
                startViewAnimation(deliveryImageView)
                changeStoreDeliveryPickUpStatus(isStoreClicked = false, isDeliveryClicked = true)
            }
            deliveryStatusValueTextView?.id -> {
                startViewAnimation(deliveryImageView)
                changeStoreDeliveryPickUpStatus(isStoreClicked = false, isDeliveryClicked = true)
            }
            pickupImageView?.id -> {
                startViewAnimation(pickupImageView)
                changeStoreDeliveryPickUpStatus(isStoreClicked = false, isDeliveryClicked = false, isPickUpClicked = true)
            }
            pickupStatusTextView?.id -> {
                startViewAnimation(pickupImageView)
                changeStoreDeliveryPickUpStatus(isStoreClicked = false, isDeliveryClicked = false, isPickUpClicked = true)
            }
            pickupStatusValueTextView?.id -> {
                startViewAnimation(pickupImageView)
                changeStoreDeliveryPickUpStatus(isStoreClicked = false, isDeliveryClicked = false, isPickUpClicked = true)
            }
        }
    }

    private fun showMinimumDeliveryOrderBottomSheet() {
        mActivity?.run {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(mActivity).inflate(
                R.layout.bottom_sheet_min_delivery_order,
                findViewById(R.id.bottomSheetContainer)
            )
            bottomSheetDialog.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
                view.run {
                    val verifyTextView: TextView = findViewById(R.id.verifyTextView)
                    val minDeliveryHeadingTextView: TextView = findViewById(R.id.minDeliveryHeadingTextView)
                    val minDeliveryAmountContainer: TextInputLayout = findViewById(R.id.minDeliveryAmountContainer)
                    val minDeliveryAmountEditText: EditText = findViewById(R.id.minDeliveryAmountEditText)
                    minDeliveryAmountContainer.hint = mMoreControlsStaticData?.bottom_sheet_heading
                    minDeliveryAmountEditText.setText(if (mMinOrderValue != 0.0) mMinOrderValue.toString() else "")
                    minDeliveryHeadingTextView.text = mMoreControlsStaticData?.bottom_sheet_hint
                    verifyTextView.text = mMoreControlsStaticData?.save_changes
                    verifyTextView.setOnClickListener {
                        val amount = minDeliveryAmountEditText.text.trim().toString()
                        if (amount.isNotEmpty() && 0.0 != mFreeDeliveryAbove && amount.toDoubleOrNull() ?: 0.0 > mFreeDeliveryAbove) {
                            minDeliveryAmountEditText.error = mMoreControlsStaticData?.error_amount_must_greater_than_free_delivery_above
                            minDeliveryAmountEditText.requestFocus()
                            return@setOnClickListener
                        }
                        if (!isInternetConnectionAvailable(mActivity)) {
                            showNoInternetConnectionDialog()
                            return@setOnClickListener
                        }
                        val request = MoreControlsRequest(
                            mDeliveryChargeType,
                            mFreeDeliveryAbove,
                            mDeliveryPrice,
                            if (amount.isEmpty()) 0.0 else if (amount.startsWith(".")) "0$amount".toDoubleOrNull() else amount.toDoubleOrNull()
                        )
                        showProgressDialog(mActivity)
                        bottomSheetDialog.dismiss()
                        mMoreControlsService?.updateDeliveryInfo(request)
                    }
                }
            }.show()
        }
    }

    override fun onMoreControlsResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val moreControlResponse = Gson().fromJson<StoreServicesResponse>(response.mCommonDataStr, StoreServicesResponse::class.java)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SET_MIN_ORDER_VALUE_SAVE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID  to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.AMOUNT    to "${moreControlResponse?.mMinOrderValue}"
                    )
                )
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                StaticInstances.sAppStoreServicesResponse = moreControlResponse
                updateStoreServiceInstances()
                setUIDataFromResponse()
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onMoreControlsServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

    private fun changeStoreDeliveryPickUpStatus(isStoreClicked: Boolean = false, isDeliveryClicked: Boolean = false, isPickUpClicked: Boolean = false) {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showCancellableProgressDialog(mActivity)
        val isStoreOpen = if (isStoreClicked) { StaticInstances.sAppStoreServicesResponse?.mStoreFlag != 1 } else StaticInstances.sAppStoreServicesResponse?.mStoreFlag == 1
        val isDeliveryOpen = if (isDeliveryClicked) { StaticInstances.sAppStoreServicesResponse?.mDeliveryFlag != 1 } else StaticInstances.sAppStoreServicesResponse?.mDeliveryFlag == 1
        val isPickUpOpen = if (isPickUpClicked) { StaticInstances.sAppStoreServicesResponse?.mPickupFlag != 1 } else StaticInstances.sAppStoreServicesResponse?.mPickupFlag == 1
        val request = StoreDeliveryStatusChangeRequest(
            if (isStoreOpen) 1 else 0,
            if (isDeliveryOpen) 1 else 0,
            if (isPickUpOpen) 1 else 0
        )
        mMoreControlsService?.changeStoreAndDeliveryStatus(request)
    }

    override fun onChangeStoreAndDeliveryStatusResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val storeDeliveryService = Gson().fromJson<StoreServicesResponse>(response.mCommonDataStr, StoreServicesResponse::class.java)
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                StaticInstances.sAppStoreServicesResponse = storeDeliveryService
                updateStoreServiceInstances()
                setUIDataFromResponse()
                setupPickupDeliveryUI()
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    private fun setupPickupDeliveryUI() {
        mActivity?.let { context ->
            if (mIsDeliveryOn) {
                deliveryStatusValueTextView?.text = mMoreControlsStaticData?.mOnText
                deliveryStatusValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.open_green))
                deliveryImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_delivery_on))
            } else {
                deliveryStatusValueTextView?.text = mMoreControlsStaticData?.mOffText
                deliveryStatusValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.red))
                deliveryImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_delivery_off))
            }
            if (mIsPickupOn) {
                pickupStatusValueTextView?.text = mMoreControlsStaticData?.mOnText
                pickupStatusValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.open_green))
                pickupImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pickup_green))
            } else {
                pickupStatusValueTextView?.text = mMoreControlsStaticData?.mOffText
                pickupStatusValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.red))
                pickupImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pickup_red))
            }
            val storeStatus = "${mMoreControlsStaticData?.mStoreText} :"
            storeStatusTextView?.text = storeStatus
            if (mIsStoreOn) {
                storeStatusTextView2?.text = mMoreControlsStaticData?.mOpenText
                storeStatusTextView2?.setTextColor(ContextCompat.getColor(context, R.color.open_green))
                storeImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_open))
            } else {
                storeStatusTextView2?.text = mMoreControlsStaticData?.mClosedText
                storeStatusTextView2?.setTextColor(ContextCompat.getColor(context, R.color.red))
                storeImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_closed))
            }
        }
    }

}