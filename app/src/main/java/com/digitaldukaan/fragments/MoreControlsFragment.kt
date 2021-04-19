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

    private lateinit var mMoreControlsStaticData: AccountStaticTextResponse
    private lateinit var mMoreControlsService: MoreControlsService
    private lateinit var mAppStoreServicesResponse: StoreServicesResponse
    private var mMinOrderValue = 0.0
    private var mDeliveryPrice = 0.0
    private var mFreeDeliveryAbove = 0.0
    private var mDeliveryChargeType = 0

    companion object {
        fun newInstance(appSettingsResponseStaticData: AccountStaticTextResponse, appStoreServicesResponse: StoreServicesResponse): MoreControlsFragment {
            val fragment = MoreControlsFragment()
            fragment.mMoreControlsStaticData = appSettingsResponseStaticData
            updateStoreServiceInstances(appStoreServicesResponse, fragment)
            return fragment
        }

        private fun updateStoreServiceInstances(appStoreServicesResponse: StoreServicesResponse, fragment: MoreControlsFragment) {
            appStoreServicesResponse.apply {
                fragment.mMinOrderValue = mMinOrderValue ?: 0.0
                fragment.mDeliveryPrice = mDeliveryPrice ?: 0.0
                fragment.mFreeDeliveryAbove = mFreeDeliveryAbove ?: 0.0
                fragment.mDeliveryChargeType = mDeliveryChargeType ?: 0
            }
            fragment.mAppStoreServicesResponse = appStoreServicesResponse
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMoreControlsService = MoreControlsService()
        mMoreControlsService.setServiceInterface(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_more_control_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            setHeaderTitle(mMoreControlsStaticData.page_heading_more_controls)
            onBackPressed(this@MoreControlsFragment)
            hideBackPressFromToolBar(mActivity, false)
        }
        hideBottomNavigationView(true)
        setUIDataFromResponse()
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
    }

    private fun setUIDataFromResponse() {
        minOrderValueHeadingTextView.text = if (0.0 == mMinOrderValue) mMoreControlsStaticData.heading_set_min_order_value_for_delivery else mMoreControlsStaticData.heading_edit_min_order_value
        minOrderValueOptionalTextView.text = if (0.0 == mMinOrderValue) mMoreControlsStaticData.text_optional else
                "${mMoreControlsStaticData.sub_heading_success_set_min_order_value_for_delivery} "
        minOrderValueAmountTextView.text = if (0.0 != mMinOrderValue) "${mMoreControlsStaticData.text_ruppee_symbol}$mMinOrderValue" else ""
        deliveryChargeHeadingTextView.text = mMoreControlsStaticData.heading_set_delivery_charge
        deliveryChargeTypeTextView.text = mMoreControlsStaticData.sub_heading_set_delivery_charge
        if (mDeliveryChargeType != 0) {
            deliveryChargeTypeTextView.text = mMoreControlsStaticData.sub_heading_success_set_delivery_charge
            if (mDeliveryChargeType == 1) {
                deliveryChargeRateTextView.visibility = View.GONE
                deliveryChargeRateValueTextView.visibility = View.GONE
            } else{
                deliveryChargeRateTextView.text = mMoreControlsStaticData.sub_heading_success_set_delivery_charge_amount
                deliveryChargeRateValueTextView.text = " ${mMoreControlsStaticData.text_ruppee_symbol} $mFreeDeliveryAbove"
            }
            deliveryChargeTypeValueTextView.text = when (mDeliveryChargeType) {
                SetDeliveryChargeFragment.FREE_DELIVERY -> mMoreControlsStaticData.heading_free_delivery
                SetDeliveryChargeFragment.FIXED_DELIVERY_CHARGE -> mMoreControlsStaticData.heading_fixed_delivery_charge
                SetDeliveryChargeFragment.CUSTOM_DELIVERY_CHARGE -> mMoreControlsStaticData.heading_custom_delivery_charge
                else -> ""
            }
        }
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            minOrderValueContainer.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SET_MIN_ORDER_VALUE,
                    isCleverTapEvent = true, isAppFlyerEvent = false, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                showMinimumDeliveryOrderBottomSheet()
            }
            deliveryChargeContainer.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SET_DELIVERY_CHARGE,
                    isCleverTapEvent = true, isAppFlyerEvent = false, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                launchFragment(SetDeliveryChargeFragment.newInstance(mMoreControlsStaticData, mAppStoreServicesResponse), true)
            }
        }
    }

    private fun showMinimumDeliveryOrderBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_min_delivery_order,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            view.run {
                val verifyTextView: TextView = findViewById(R.id.verifyTextView)
                val minDeliveryHeadingTextView: TextView = findViewById(R.id.minDeliveryHeadingTextView)
                val minDeliveryAmountContainer: TextInputLayout = findViewById(R.id.minDeliveryAmountContainer)
                val minDeliveryAmountEditText: EditText = findViewById(R.id.minDeliveryAmountEditText)
                minDeliveryAmountContainer.hint = mMoreControlsStaticData.bottom_sheet_heading
                minDeliveryAmountEditText.setText(if (mMinOrderValue != 0.0) mMinOrderValue.toString() else "")
                minDeliveryHeadingTextView.text = mMoreControlsStaticData.bottom_sheet_hint
                verifyTextView.text = mMoreControlsStaticData.save_changes
                verifyTextView.setOnClickListener {
                    val amount = minDeliveryAmountEditText.text.trim().toString()
                    if (amount.isEmpty()) {
                        minDeliveryAmountEditText.error = mMoreControlsStaticData.error_mandatory_field
                        requestFocus()
                        return@setOnClickListener
                    }
                    if (0.0 == amount.toDouble()) {
                        minDeliveryAmountEditText.error = mMoreControlsStaticData.error_amount_must_greater_than_zero
                        requestFocus()
                        return@setOnClickListener
                    }
                    if (0.0 != mFreeDeliveryAbove && amount.toDouble() > mFreeDeliveryAbove) {
                        minDeliveryAmountEditText.error = mMoreControlsStaticData.error_amount_must_greater_than_free_delivery_above
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
                        amount.toDouble()
                    )
                    showProgressDialog(mActivity)
                    bottomSheetDialog.dismiss()
                    mMoreControlsService.updateDeliveryInfo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
                }
            }
        }.show()
    }

    override fun onMoreControlsResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                val moreControlResponse = Gson().fromJson<StoreServicesResponse>(response.mCommonDataStr, StoreServicesResponse::class.java)
                updateStoreServiceInstances(moreControlResponse, this)
                setUIDataFromResponse()
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onMoreControlsServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}