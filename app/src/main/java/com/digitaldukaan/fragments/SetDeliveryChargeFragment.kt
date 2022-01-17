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
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.set_delivery_charge_fragment.*

class SetDeliveryChargeFragment : BaseFragment(), IMoreControlsServiceInterface {

    private var mMoreControlsStaticData: AccountStaticTextResponse? = null
    private var mAppStoreServicesResponse: StoreServicesResponse? = null

    companion object {

        fun newInstance(appSettingsResponseStaticData: AccountStaticTextResponse?): SetDeliveryChargeFragment {
            val fragment = SetDeliveryChargeFragment()
            fragment.mMoreControlsStaticData = appSettingsResponseStaticData
            fragment.mAppStoreServicesResponse = StaticInstances.sAppStoreServicesResponse
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "SetDeliveryChargeFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.set_delivery_charge_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            headerTitle = mMoreControlsStaticData?.page_heading_set_delivery_charge
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
        mMoreControlsStaticData?.let {
            freeDeliveryRadioButton?.text = it.heading_free_delivery
            freeDeliveryTextView?.text = it.free_delivery_description
            fixedDeliveryRadioButton?.text = it.heading_fixed_delivery_charge
            fixedDeliveryChargeLayout?.hint = it.hint_free_delivery_charge
            freeDeliveryAboveLayout?.hint = it.hint_free_delivery_above_optional
            customDeliveryRadioButton?.text = it.heading_custom_delivery_charge
            customDeliveryAboveLayout?.hint = it.hint_custom_delivery_charge
            customDeliveryTextView?.text = it.custom_delivery_charge_description
            continueTextView?.text = it.bottom_sheet_save_changes
        }
        when (mAppStoreServicesResponse?.mDeliveryChargeType) {
            Constants.UNKNOWN_DELIVERY_CHARGE -> {
                showFreeDeliveryContainer(false)
                showFixedDeliveryContainer(false)
                showCustomDeliveryContainer(false)
            }
            Constants.FREE_DELIVERY -> {
                showFreeDeliveryContainer(true)
                showFixedDeliveryContainer(false)
                showCustomDeliveryContainer(false)
            }
            Constants.FIXED_DELIVERY_CHARGE -> {
                showFreeDeliveryContainer(false)
                showFixedDeliveryContainer(true)
                showCustomDeliveryContainer(false)
                fixedDeliveryChargeEditText?.setText(mAppStoreServicesResponse?.mDeliveryPrice.toString())
                if (0.0 != mAppStoreServicesResponse?.mFreeDeliveryAbove) freeDeliveryAboveEditText.setText(mAppStoreServicesResponse?.mFreeDeliveryAbove.toString())
            }
            Constants.CUSTOM_DELIVERY_CHARGE -> {
                showFreeDeliveryContainer(false)
                showFixedDeliveryContainer(false)
                showCustomDeliveryContainer(true)
                if (0.0 != mAppStoreServicesResponse?.mFreeDeliveryAbove) customDeliveryAboveEditText.setText(mAppStoreServicesResponse?.mFreeDeliveryAbove.toString())
            }
        }
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            freeDeliveryRadioButton?.id -> {
                showFreeDeliveryContainer(true)
                showFixedDeliveryContainer(false)
                showCustomDeliveryContainer(false)
            }
            fixedDeliveryRadioButton?.id -> {
                showFixedDeliveryContainer(true)
                showFreeDeliveryContainer(false)
                showCustomDeliveryContainer(false)
            }
            customDeliveryRadioButton?.id -> {
                showCustomDeliveryContainer(true)
                showFreeDeliveryContainer(false)
                showFixedDeliveryContainer(false)
            }
            continueTextView?.id -> {
                var selectionStr = ""
                if (!freeDeliveryRadioButton.isChecked && !fixedDeliveryRadioButton.isChecked && !customDeliveryRadioButton.isChecked) {
                    showShortSnackBar("Please select at least 1 Delivery charge")
                    return
                }
                val mMoreControlRequest = MoreControlsRequest(0, 0.0, 0.0, 0.0)
                if (freeDeliveryRadioButton.isChecked) {
                    selectionStr = "Free_delivery"
                    mMoreControlRequest.apply {
                        deliveryChargeType = Constants.FREE_DELIVERY
                        deliveryPrice = 0.0
                        freeDeliveryAbove = 0.0
                        minOrderValue = mAppStoreServicesResponse?.mMinOrderValue ?: 0.0
                    }
                }
                if (fixedDeliveryRadioButton.isChecked) {
                    selectionStr = "Fixed_delivery"
                    val fixedDeliveryChargeStr = fixedDeliveryChargeEditText.text.trim().toString()
                    val freeDeliveryAboveStr = freeDeliveryAboveEditText.text.trim().toString()
                    if (isEmpty(fixedDeliveryChargeStr) || 0.0 == fixedDeliveryChargeStr.toDouble()) {
                        fixedDeliveryChargeEditText?.apply {
                            requestFocus()
                            error = mMoreControlsStaticData?.error_please_select_free_delivery
                        }
                        return
                    }
                    if (isNotEmpty(freeDeliveryAboveStr) && (freeDeliveryAboveStr.toDouble() < (mAppStoreServicesResponse?.mMinOrderValue ?: 0.0))) {
                        freeDeliveryAboveEditText?.apply {
                            requestFocus()
                            error = mMoreControlsStaticData?.error_amount_must_greater_than_min_order_value
                        }
                        return
                    }
                    if (isNotEmpty(freeDeliveryAboveStr) && (0.0 == freeDeliveryAboveStr.toDouble())) {
                        freeDeliveryAboveEditText?.apply {
                            requestFocus()
                            error = mMoreControlsStaticData?.error_mandatory_field
                        }
                        return
                    }
                    mMoreControlRequest.apply {
                        deliveryChargeType = Constants.FIXED_DELIVERY_CHARGE
                        deliveryPrice = fixedDeliveryChargeStr.toDouble()
                        freeDeliveryAbove = if (isEmpty(freeDeliveryAboveStr)) 0.0 else freeDeliveryAboveStr.toDouble()
                        minOrderValue = mAppStoreServicesResponse?.mMinOrderValue ?: 0.0
                    }
                }
                if (customDeliveryRadioButton.isChecked) {
                    selectionStr = "Custom_delivery"
                    val customDeliveryAboveStr = customDeliveryAboveEditText.text.trim().toString()
                    if (isNotEmpty(customDeliveryAboveStr) && (0.0 == customDeliveryAboveStr.toDouble())) {
                        customDeliveryAboveEditText?.apply {
                            requestFocus()
                            error = mMoreControlsStaticData?.error_mandatory_field
                        }
                        return
                    }
                    if (isNotEmpty(customDeliveryAboveStr) && (customDeliveryAboveStr.toDouble() < (mAppStoreServicesResponse?.mMinOrderValue ?: 0.0))) {
                        customDeliveryAboveEditText?.apply {
                            requestFocus()
                            error = mMoreControlsStaticData?.error_amount_must_greater_than_min_order_value
                        }
                        return
                    }
                    mMoreControlRequest.apply {
                        deliveryChargeType = Constants.CUSTOM_DELIVERY_CHARGE
                        deliveryPrice = 0.0
                        freeDeliveryAbove = if (customDeliveryAboveStr.isEmpty()) 0.0 else customDeliveryAboveStr.toDouble()
                        minOrderValue = mAppStoreServicesResponse?.mMinOrderValue ?: 0.0
                    }
                }
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                showProgressDialog(mActivity)
                continueTextView?.isEnabled = false
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
                service.updateDeliveryInfo(mMoreControlRequest)
            }
        }
    }

    private fun showFreeDeliveryContainer(toShow: Boolean) {
        if (toShow) {
            freeDeliveryRadioButton?.isChecked = true
            freeDeliveryRadioButton?.isSelected = true
            freeDeliverySeparator?.visibility = View.VISIBLE
            freeDeliveryTextView?.visibility = View.VISIBLE
        } else {
            freeDeliveryRadioButton?.isChecked = false
            freeDeliveryRadioButton?.isSelected = false
            freeDeliverySeparator?.visibility = View.GONE
            freeDeliveryTextView?.visibility = View.GONE
        }
    }

    private fun showFixedDeliveryContainer(toShow: Boolean) {
        if (toShow) {
            fixedDeliveryRadioButton?.isChecked = true
            fixedDeliveryRadioButton?.isSelected = true
            fixedDeliverySeparator?.visibility = View.VISIBLE
            fixedDeliveryChargeLayout?.visibility = View.VISIBLE
            freeDeliveryAboveLayout?.visibility = View.VISIBLE
        } else {
            fixedDeliveryRadioButton?.isChecked = false
            fixedDeliveryRadioButton?.isSelected = false
            fixedDeliverySeparator?.visibility = View.GONE
            fixedDeliveryChargeLayout?.visibility = View.GONE
            freeDeliveryAboveLayout?.visibility = View.GONE
        }
    }

    private fun showCustomDeliveryContainer(toShow: Boolean) {
        if (toShow) {
            customDeliveryRadioButton?.isChecked = true
            customDeliveryRadioButton?.isSelected = true
            customDeliverySeparator?.visibility = View.VISIBLE
            customDeliveryAboveLayout?.visibility = View.VISIBLE
            customDeliveryTextView?.visibility = View.VISIBLE
            customDeliveryImageView?.visibility = View.VISIBLE
        } else {
            customDeliveryRadioButton?.isChecked = false
            customDeliveryRadioButton?.isSelected = false
            customDeliverySeparator?.visibility = View.GONE
            customDeliveryAboveLayout?.visibility = View.GONE
            customDeliveryTextView?.visibility = View.GONE
            customDeliveryImageView?.visibility = View.GONE
        }
    }

    override fun onMoreControlsResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            continueTextView?.isEnabled = true
            stopProgress()
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                val servicesObj = Gson().fromJson<StoreServicesResponse>(response.mCommonDataStr, StoreServicesResponse::class.java)
                StaticInstances.sAppStoreServicesResponse = servicesObj
                mActivity?.onBackPressed()
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onMoreControlsPageInfoResponse(response: CommonApiResponse) = Unit

    override fun onChangeStoreAndDeliveryStatusResponse(response: CommonApiResponse) = Unit

    override fun onMoreControlsServerException(e: Exception) {
        CoroutineScopeUtils().runTaskOnCoroutineMain { continueTextView?.isEnabled = true }
        exceptionHandlingForAPIResponse(e)
    }

}