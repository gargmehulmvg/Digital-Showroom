package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.MoreControlsRequest
import com.digitaldukaan.models.response.AccountStaticTextResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.StoreServicesResponse
import com.digitaldukaan.services.MoreControlsService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IMoreControlsServiceInterface
import kotlinx.android.synthetic.main.set_delivery_charge_fragment.*

class SetDeliveryChargeFragment : BaseFragment(), IMoreControlsServiceInterface {

    private lateinit var mMoreControlsStaticData: AccountStaticTextResponse
    private lateinit var mAppStoreServicesResponse: StoreServicesResponse

    companion object {
        fun newInstance(appSettingsResponseStaticData: AccountStaticTextResponse, appStoreServicesResponse: StoreServicesResponse): SetDeliveryChargeFragment {
            val fragment = SetDeliveryChargeFragment()
            fragment.mMoreControlsStaticData = appSettingsResponseStaticData
            fragment.mAppStoreServicesResponse = appStoreServicesResponse
            return fragment
        }
        const val UNKNOWN_DELIVERY_CHARGE = 0
        const val FREE_DELIVERY = 1
        const val FIXED_DELIVERY_CHARGE = 2
        const val CUSTOM_DELIVERY_CHARGE = 3
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.set_delivery_charge_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            setHeaderTitle(mMoreControlsStaticData.page_heading_set_delivery_charge)
            onBackPressed(this@SetDeliveryChargeFragment)
            hideBackPressFromToolBar(mActivity, false)
        }
        hideBottomNavigationView(true)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        setupTextFromResponse()
    }

    private fun setupTextFromResponse() {
        freeDeliveryRadioButton.text = mMoreControlsStaticData.heading_free_delivery
        freeDeliveryTextView.text = mMoreControlsStaticData.free_delivery_description
        fixedDeliveryRadioButton.text = mMoreControlsStaticData.heading_fixed_delivery_charge
        fixedDeliveryChargeLayout.hint = mMoreControlsStaticData.hint_free_delivery_charge
        freeDeliveryAboveLayout.hint = mMoreControlsStaticData.hint_free_delivery_above_optional
        customDeliveryRadioButton.text = mMoreControlsStaticData.heading_custom_delivery_charge
        customDeliveryAboveLayout.hint = mMoreControlsStaticData.hint_custom_delivery_charge
        customDeliveryTextView.text = mMoreControlsStaticData.custom_delivery_charge_description
        continueTextView.text = mMoreControlsStaticData.bottom_sheet_save_changes
        when (mAppStoreServicesResponse.mDeliveryChargeType) {
            UNKNOWN_DELIVERY_CHARGE -> {
                showFreeDeliveryContainer(false)
                showFixedDeliveryContainer(false)
                showCustomDeliveryContainer(false)
            }
            FREE_DELIVERY -> {
                showFreeDeliveryContainer(true)
                showFixedDeliveryContainer(false)
                showCustomDeliveryContainer(false)
            }
            FIXED_DELIVERY_CHARGE -> {
                showFreeDeliveryContainer(false)
                showFixedDeliveryContainer(true)
                showCustomDeliveryContainer(false)
                fixedDeliveryChargeEditText.setText(mAppStoreServicesResponse.mDeliveryPrice.toString())
                if (mAppStoreServicesResponse.mFreeDeliveryAbove != 0.0) freeDeliveryAboveEditText.setText(mAppStoreServicesResponse.mFreeDeliveryAbove.toString())
            }
            CUSTOM_DELIVERY_CHARGE -> {
                showFreeDeliveryContainer(false)
                showFixedDeliveryContainer(false)
                showCustomDeliveryContainer(true)
                if (mAppStoreServicesResponse.mFreeDeliveryAbove != 0.0) customDeliveryAboveEditText.setText(mAppStoreServicesResponse.mFreeDeliveryAbove.toString())
            }
        }
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            freeDeliveryRadioButton.id -> {
                showFreeDeliveryContainer(true)
                showFixedDeliveryContainer(false)
                showCustomDeliveryContainer(false)
            }
            fixedDeliveryRadioButton.id -> {
                showFixedDeliveryContainer(true)
                showFreeDeliveryContainer(false)
                showCustomDeliveryContainer(false)
            }
            customDeliveryRadioButton.id -> {
                showCustomDeliveryContainer(true)
                showFreeDeliveryContainer(false)
                showFixedDeliveryContainer(false)
            }
            continueTextView.id -> {
                var selectionStr = ""
                if (!freeDeliveryRadioButton.isChecked && !fixedDeliveryRadioButton.isChecked && !customDeliveryRadioButton.isChecked) {
                    showShortSnackBar("Please select at least 1 Delivery charge")
                    return
                }
                val mMoreControlRequest = MoreControlsRequest(0, 0.0, 0.0, 0.0)
                if (freeDeliveryRadioButton.isChecked) {
                    selectionStr = "Free_delivery"
                    mMoreControlRequest.deliveryChargeType = FREE_DELIVERY
                    mMoreControlRequest.deliveryPrice = 0.0
                    mMoreControlRequest.freeDeliveryAbove = 0.0
                    mMoreControlRequest.minOrderValue = mAppStoreServicesResponse.mMinOrderValue ?: 0.0
                }
                if (fixedDeliveryRadioButton.isChecked) {
                    selectionStr = "Fixed_delivery"
                    val fixedDeliveryChargeStr = fixedDeliveryChargeEditText.text.trim().toString()
                    val freeDeliveryAboveStr = freeDeliveryAboveEditText.text.trim().toString()
                    if (fixedDeliveryChargeStr.isEmpty() || fixedDeliveryChargeStr.toDouble() == 0.0) {
                        fixedDeliveryChargeEditText.requestFocus()
                        fixedDeliveryChargeEditText.error = mMoreControlsStaticData.error_mandatory_field
                        return
                    }
                    if (freeDeliveryAboveStr.isNotEmpty() && (freeDeliveryAboveStr.toDouble() < (mAppStoreServicesResponse.mMinOrderValue ?: 0.0))) {
                        freeDeliveryAboveEditText.requestFocus()
                        freeDeliveryAboveEditText.error = mMoreControlsStaticData.error_amount_must_greater_than_min_order_value
                        return
                    }
                    if (freeDeliveryAboveStr.isNotEmpty() && (freeDeliveryAboveStr.toDouble() == 0.0)) {
                        freeDeliveryAboveEditText.requestFocus()
                        freeDeliveryAboveEditText.error = mMoreControlsStaticData.error_mandatory_field
                        return
                    }
                    mMoreControlRequest.deliveryChargeType = FIXED_DELIVERY_CHARGE
                    mMoreControlRequest.deliveryPrice = fixedDeliveryChargeStr.toDouble()
                    mMoreControlRequest.freeDeliveryAbove = if (freeDeliveryAboveStr.isEmpty()) 0.0 else freeDeliveryAboveStr.toDouble()
                    mMoreControlRequest.minOrderValue = mAppStoreServicesResponse.mMinOrderValue ?: 0.0
                }
                if (customDeliveryRadioButton.isChecked) {
                    selectionStr = "Custom_delivery"
                    val customDeliveryAboveStr = customDeliveryAboveEditText.text.trim().toString()
                    if (customDeliveryAboveStr.isNotEmpty() && (customDeliveryAboveStr.toDouble() == 0.0)) {
                        customDeliveryAboveEditText.requestFocus()
                        customDeliveryAboveEditText.error = mMoreControlsStaticData.error_mandatory_field
                        return
                    }
                    if (customDeliveryAboveStr.isNotEmpty() && (customDeliveryAboveStr.toDouble() < (mAppStoreServicesResponse.mMinOrderValue ?: 0.0))) {
                        customDeliveryAboveEditText.requestFocus()
                        customDeliveryAboveEditText.error = mMoreControlsStaticData.error_amount_must_greater_than_min_order_value
                        return
                    }
                    mMoreControlRequest.deliveryChargeType = CUSTOM_DELIVERY_CHARGE
                    mMoreControlRequest.deliveryPrice = mAppStoreServicesResponse.mDeliveryPrice ?: 0.0
                    mMoreControlRequest.freeDeliveryAbove = if (customDeliveryAboveStr.isEmpty()) 0.0 else customDeliveryAboveStr.toDouble()
                    mMoreControlRequest.minOrderValue = mAppStoreServicesResponse.mMinOrderValue ?: 0.0
                }
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                showProgressDialog(mActivity)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_DELIVERY_MODEL_SELECT,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.SELECTION to selectionStr
                    )
                )
                val service = MoreControlsService()
                service.setServiceInterface(this)
                service.updateDeliveryInfo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), mMoreControlRequest)
            }
        }
    }

    private fun showFreeDeliveryContainer(toShow: Boolean) {
        if (toShow) {
            freeDeliveryRadioButton.isChecked = true
            freeDeliveryRadioButton.isSelected = true
            freeDeliverySeparator.visibility = View.VISIBLE
            freeDeliveryTextView.visibility = View.VISIBLE
        } else {
            freeDeliveryRadioButton.isChecked = false
            freeDeliveryRadioButton.isSelected = false
            freeDeliverySeparator.visibility = View.GONE
            freeDeliveryTextView.visibility = View.GONE
        }
    }

    private fun showFixedDeliveryContainer(toShow: Boolean) {
        if (toShow) {
            fixedDeliveryRadioButton.isChecked = true
            fixedDeliveryRadioButton.isSelected = true
            fixedDeliverySeparator.visibility = View.VISIBLE
            fixedDeliveryChargeLayout.visibility = View.VISIBLE
            freeDeliveryAboveLayout.visibility = View.VISIBLE
        } else {
            fixedDeliveryRadioButton.isChecked = false
            fixedDeliveryRadioButton.isSelected = false
            fixedDeliverySeparator.visibility = View.GONE
            fixedDeliveryChargeLayout.visibility = View.GONE
            freeDeliveryAboveLayout.visibility = View.GONE
        }
    }

    private fun showCustomDeliveryContainer(toShow: Boolean) {
        if (toShow) {
            customDeliveryRadioButton.isChecked = true
            customDeliveryRadioButton.isSelected = true
            customDeliverySeparator.visibility = View.VISIBLE
            customDeliveryAboveLayout.visibility = View.VISIBLE
            customDeliveryTextView.visibility = View.VISIBLE
            customDeliveryImageView.visibility = View.VISIBLE
        } else {
            customDeliveryRadioButton.isChecked = false
            customDeliveryRadioButton.isSelected = false
            customDeliverySeparator.visibility = View.GONE
            customDeliveryAboveLayout.visibility = View.GONE
            customDeliveryTextView.visibility = View.GONE
            customDeliveryImageView.visibility = View.GONE
        }
    }

    override fun onMoreControlsResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                clearFragmentBackStack()
                launchFragment(HomeFragment.newInstance(), true)
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onMoreControlsServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}