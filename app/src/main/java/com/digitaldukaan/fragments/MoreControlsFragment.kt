package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.MoreControlsRequest
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

    companion object {
        private const val TAG = "MoreControlsFragment"
        fun newInstance(appSettingsResponseStaticData: AccountStaticTextResponse?): MoreControlsFragment {
            val fragment = MoreControlsFragment()
            fragment.mMoreControlsStaticData = appSettingsResponseStaticData
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
            setHeaderTitle(mMoreControlsStaticData?.page_heading_more_controls)
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
        }
    }

    private fun setUIDataFromResponse() {
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
                mMoreControlsStaticData?.let { launchFragment(SetDeliveryChargeFragment.newInstance(it), true) }
            }
            onlinePaymentsContainer?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SET_PREPAID_ORDER,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PATH to AFInAppEventParameterName.MORE_CONTROLS
                    )
                )
                launchFragment(SetOrderTypeFragment.newInstance(), true)
            }
            paymentModesContainer?.id -> {
                launchFragment(PaymentModesFragment.newInstance(), true)
                /*val GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user"
                val GOOGLE_PAY_PACKAGE_NAME = "com.phonepe.app"
                val GOOGLE_PAY_REQUEST_CODE = 123

                val uri: Uri = Uri.Builder()
                    .scheme("upi")
                    .authority("pay")
                    .appendQueryParameter("pa", "dotpe.payu@indus")
                    .appendQueryParameter("pn", "DOTPE PRIVATE LIMITED")
                    //.appendQueryParameter("mc", "your-merchant-code")
                    .appendQueryParameter("tr", "13230050241")
                    .appendQueryParameter("tn", "OrderId-CWzMK1PmJLao5VkA")
                    .appendQueryParameter("am", "3600.00")
                    .appendQueryParameter("cu", "INR")
                    //.appendQueryParameter("url", "your-transaction-url")
                    .build()
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = uri
                intent.setPackage(GOOGLE_PAY_PACKAGE_NAME)
                mActivity?.startActivityForResult(intent, GOOGLE_PAY_REQUEST_CODE)*/
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
                        if (amount.isNotEmpty() && 0.0 != mFreeDeliveryAbove && amount.toDouble() > mFreeDeliveryAbove) {
                            minDeliveryAmountEditText.error = mMoreControlsStaticData?.error_amount_must_greater_than_free_delivery_above
                            requestFocus()
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
                            if (amount.isEmpty()) 0.0 else if (amount.startsWith(".")) "0$amount".toDouble() else amount.toDouble()
                        )
                        showProgressDialog(mActivity)
                        bottomSheetDialog.dismiss()
                        mMoreControlsService?.updateDeliveryInfo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
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
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.AMOUNT to "${moreControlResponse?.mMinOrderValue}"
                    )
                )
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                StaticInstances.sAppStoreServicesResponse = moreControlResponse
                updateStoreServiceInstances()
                setUIDataFromResponse()
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onMoreControlsServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}