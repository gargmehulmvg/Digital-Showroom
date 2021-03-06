package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.digitaldukaan.R
import com.digitaldukaan.adapters.MoreControlsItemAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.MoreControlsRequest
import com.digitaldukaan.models.request.StoreDeliveryStatusChangeRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.MoreControlsService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IMoreControlsItemClickListener
import com.digitaldukaan.services.serviceinterface.IMoreControlsServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_more_control_fragment_v2.*

class MoreControlsFragment : BaseFragment(), IMoreControlsServiceInterface, IMoreControlsItemClickListener,
    SwipeRefreshLayout.OnRefreshListener {

    private var mMoreControlsService: MoreControlsService? = null
    private var mMinOrderValue = 0.0
    private var mDeliveryPrice = 0.0
    private var mFreeDeliveryAbove = 0.0
    private var mDeliveryChargeType = 0
    private var mPaymentPaymentMethod: String? = ""
    private var mIsDeliveryOn: Boolean = false
    private var mIsPickupOn: Boolean = false
    private var mIsStoreOn: Boolean = false
    private var mMoreControlsPageInfoResponse: MoreControlsPageInfoResponse? = null
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAccountStaticText: AccountStaticTextResponse? = null

    companion object {
        fun newInstance(accountStaticText: AccountStaticTextResponse?): MoreControlsFragment {
            val fragment = MoreControlsFragment()
            fragment.mAccountStaticText = accountStaticText
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "MoreControlsFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_more_control_fragment_v2, container, false)
        mMoreControlsService = MoreControlsService()
        mMoreControlsService?.setServiceInterface(this)
        showProgressDialog(mActivity)
        mMoreControlsService?.getMoreControlsPageInfo()
        mSwipeRefreshLayout = mContentView?.findViewById(R.id.swipeRefreshLayout)
        mSwipeRefreshLayout?.setOnRefreshListener(this)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            headerTitle = ""
            onBackPressed(this@MoreControlsFragment)
            hideBackPressFromToolBar(mActivity, false)
        }
        hideBottomNavigationView(true)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
    }

    private fun updateStoreServiceInstances() {
        mMoreControlsPageInfoResponse?.store?.storeServices?.apply {
            this@MoreControlsFragment.mMinOrderValue = mMinOrderValue ?: 0.0
            this@MoreControlsFragment.mDeliveryPrice = mDeliveryPrice ?: 0.0
            this@MoreControlsFragment.mFreeDeliveryAbove = mFreeDeliveryAbove
            this@MoreControlsFragment.mDeliveryChargeType = mDeliveryChargeType ?: 0
            this@MoreControlsFragment.mPaymentPaymentMethod = StaticInstances.sPaymentMethodStr ?: ""
            mActivity?.let { context ->
                val staticText = mMoreControlsPageInfoResponse?.staticText
                storeServiceContainerHeading?.text = staticText?.heading_tap_the_icon
                if (1 == mDeliveryFlag) {
                    mIsDeliveryOn = true
                    deliveryStatusValueTextView?.text = staticText?.text_on
                    deliveryStatusValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.open_green))
                    deliveryImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_delivery_on))
                } else {
                    mIsDeliveryOn = false
                    deliveryStatusValueTextView?.text = staticText?.text_off
                    deliveryStatusValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.red))
                    deliveryImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_delivery_off))
                }
                if (1 == mPickupFlag) {
                    mIsPickupOn = true
                    pickupStatusValueTextView?.text = staticText?.text_on
                    pickupStatusValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.open_green))
                    pickupImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pickup_green))
                } else {
                    mIsPickupOn = false
                    pickupStatusValueTextView?.text = staticText?.text_off
                    pickupStatusValueTextView?.setTextColor(ContextCompat.getColor(context, R.color.red))
                    pickupImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_pickup_red))
                }
                val storeStatus = "${staticText?.text_store} :"
                storeStatusTextView?.text = storeStatus
                if (1 == mStoreFlag) {
                    mIsStoreOn = true
                    storeStatusTextView2?.text = staticText?.text_open
                    storeStatusTextView2?.setTextColor(ContextCompat.getColor(context, R.color.open_green))
                    storeImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_open))
                } else {
                    mIsStoreOn = false
                    storeStatusTextView2?.text = staticText?.text_closed
                    storeStatusTextView2?.setTextColor(ContextCompat.getColor(context, R.color.red))
                    storeImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_closed))
                }
                val deliveryStatus = "${staticText?.text_delivery} :"
                val pickUpStr = "${staticText?.text_pickup} :"
                deliveryStatusTextView?.text = deliveryStatus
                pickupStatusTextView?.text = pickUpStr
                deliveryStatusValueTextView?.text = if (1 == mDeliveryFlag) staticText?.text_on else staticText?.text_off
                deliveryStatusValueTextView?.setTextColor(ContextCompat.getColor(context, if (mDeliveryFlag == 1) R.color.open_green else R.color.red))
                storeStatusTextView2?.setTextColor(ContextCompat.getColor(context, if (mStoreFlag == 1) R.color.open_green else R.color.red))
            }
        }
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
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
                    val staticText = mMoreControlsPageInfoResponse?.staticText
                    val verifyTextView: TextView = findViewById(R.id.verifyTextView)
                    val minDeliveryHeadingTextView: TextView = findViewById(R.id.minDeliveryHeadingTextView)
                    val minDeliveryAmountContainer: TextInputLayout = findViewById(R.id.minDeliveryAmountContainer)
                    val minDeliveryAmountEditText: EditText = findViewById(R.id.minDeliveryAmountEditText)
                    minDeliveryAmountContainer.hint = staticText?.bottom_sheet_hint
                    minDeliveryAmountEditText.setText(if (0.0 != mMinOrderValue) mMinOrderValue.toString() else "")
                    minDeliveryHeadingTextView.text = staticText?.heading_set_min_order_value_for_delivery
                    verifyTextView.text = staticText?.bottom_sheet_save_changes
                    verifyTextView.setOnClickListener {
                        val amount = minDeliveryAmountEditText.text.trim().toString()
                        if (isNotEmpty(amount) && 0.0 != mFreeDeliveryAbove && amount.toDoubleOrNull() ?: 0.0 > mFreeDeliveryAbove) {
                            minDeliveryAmountEditText.error = staticText?.error_amount_must_greater_than_free_delivery_above
                            minDeliveryAmountEditText.requestFocus()
                            return@setOnClickListener
                        }
                        if (!isInternetConnectionAvailable(mActivity)) {
                            showNoInternetConnectionDialog()
                            return@setOnClickListener
                        }
                        val request = MoreControlsRequest(
                            deliveryChargeType = mDeliveryChargeType,
                            freeDeliveryAbove = mFreeDeliveryAbove,
                            deliveryPrice = mDeliveryPrice,
                            minOrderValue = if (isEmpty(amount)) 0.0 else if (amount.startsWith(".")) "0$amount".toDoubleOrNull() else amount.toDoubleOrNull()
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
                onRefresh()
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onMoreControlsPageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                mMoreControlsPageInfoResponse = Gson().fromJson<MoreControlsPageInfoResponse>(response.mCommonDataStr, MoreControlsPageInfoResponse::class.java)
                val recyclerView: RecyclerView? = mContentView?.findViewById(R.id.recyclerView)
                recyclerView?.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    isNestedScrollingEnabled = false
                    adapter = MoreControlsItemAdapter(mActivity, mMoreControlsPageInfoResponse?.storeControlItemsList, mMoreControlsPageInfoResponse?.staticText, this@MoreControlsFragment)
                }
                updateStoreServiceInstances()
                ToolBarManager.getInstance()?.apply {
                    headerTitle = mMoreControlsPageInfoResponse?.staticText?.heading_page
                }
            }
            if (true == mSwipeRefreshLayout?.isRefreshing) mSwipeRefreshLayout?.isRefreshing = false
        }
    }

    override fun onMoreControlsServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

    private fun changeStoreDeliveryPickUpStatus(isStoreClicked: Boolean = false, isDeliveryClicked: Boolean = false, isPickUpClicked: Boolean = false) {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showCancellableProgressDialog(mActivity)
        val isStoreOpen = if (isStoreClicked) { 1 != mMoreControlsPageInfoResponse?.store?.storeServices?.mStoreFlag } else 1 == mMoreControlsPageInfoResponse?.store?.storeServices?.mStoreFlag
        val isDeliveryOpen = if (isDeliveryClicked) { 1 != mMoreControlsPageInfoResponse?.store?.storeServices?.mDeliveryFlag } else 1 == mMoreControlsPageInfoResponse?.store?.storeServices?.mDeliveryFlag
        val isPickUpOpen = if (isPickUpClicked) { 1 != mMoreControlsPageInfoResponse?.store?.storeServices?.mPickupFlag } else 1 == mMoreControlsPageInfoResponse?.store?.storeServices?.mPickupFlag
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
            try {
                if (response.mIsSuccessStatus) {
                    showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                    onRefresh()
                } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
            } catch (e: Exception) {
                Log.e(TAG, "onChangeStoreAndDeliveryStatusResponse: ${e.message}", e)
            }
        }
    }

    override fun onMoreControlsEditMinOrderValueClicked(item: MoreControlsInnerItemResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SET_MIN_ORDER_VALUE,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
            )
            showMinimumDeliveryOrderBottomSheet()
        }
    }

    override fun onMoreControlsSetDeliveryChargeClicked(item: MoreControlsInnerItemResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SET_DELIVERY_CHARGE,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
            )
            launchFragment(SetDeliveryChargeFragment.newInstance(mAccountStaticText), true)
        }
    }

    override fun onMoreControlsPrepaidOrderClicked(item: MoreControlsInnerItemResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (item.isLocked) {
                openSubscriptionLockedUrlInBrowser(mMoreControlsPageInfoResponse?.mPrepaidOrdersLocked?.mUrl ?: "")
                return@runTaskOnCoroutineMain
            }
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SET_PREPAID_ORDER,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.PATH to AFInAppEventParameterName.MORE_CONTROLS)
            )
            launchFragment(SetOrderTypeFragment.newInstance(), true)
        }
    }

    override fun onMoreControlsEditCustomerAddressFieldClicked(item: MoreControlsInnerItemResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            launchFragment(AddressFieldsFragment.newInstance(), true)
        }
    }

    override fun onMoreControlsOrderNotificationClicked(item: MoreControlsInnerItemResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            getOrderNotificationBottomSheet(AFInAppEventParameterName.STORE_CONTROLS)
        }
    }

    override fun onMoreControlsPaymentModesClicked(item: MoreControlsInnerItemResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (item.isLocked) {
                openSubscriptionLockedUrlInBrowser(mMoreControlsPageInfoResponse?.mPrepaidOrdersLocked?.mUrl ?: "")
                return@runTaskOnCoroutineMain
            }
            launchFragment(PaymentModesFragment.newInstance(), true)
        }
    }

    override fun onRefresh() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            showCancellableProgressDialog(mActivity)
            mMoreControlsService?.getMoreControlsPageInfo()
        }
    }

}