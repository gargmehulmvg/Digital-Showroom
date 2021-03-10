package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.request.MoreControlsRequest
import com.digitaldukaan.models.response.AccountStaticTextResponse
import com.digitaldukaan.models.response.MoreControlsResponse
import com.digitaldukaan.models.response.StoreServicesResponse
import com.digitaldukaan.services.MoreControlsService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IMoreControlsServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.more_control_fragment.*

class MoreControlsFragment : BaseFragment(), IMoreControlsServiceInterface {

    private lateinit var mMoreControlsStaticData: AccountStaticTextResponse
    private lateinit var mMoreControlsService: MoreControlsService
    private var mMinOrderValue = 0.0
    private var mDeliveryPrice = 0.0
    private var mFreeDeliveryAbove = 0.0
    private var mDeliveryChargeType = 0

    companion object {
        fun newInstance(appSettingsResponseStaticData: AccountStaticTextResponse, appStoreServicesResponse: StoreServicesResponse): MoreControlsFragment {
            val fragment = MoreControlsFragment()
            fragment.mMoreControlsStaticData = appSettingsResponseStaticData
            appStoreServicesResponse.apply {
                fragment.mMinOrderValue = mMinOrderValue ?: 0.0
                fragment.mDeliveryPrice = mDeliveryPrice ?: 0.0
                fragment.mFreeDeliveryAbove = mFreeDeliveryAbove ?: 0.0
                fragment.mDeliveryChargeType = mDeliveryChargeType ?: 0
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMoreControlsService = MoreControlsService()
        mMoreControlsService.setServiceInterface(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.more_control_fragment, container, false)
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
        setUIDataFromResponse()
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
    }

    private fun setUIDataFromResponse() {
        minOrderValueHeadingTextView.text = if (0.0 == mMinOrderValue) mMoreControlsStaticData.heading_set_min_order_value_for_delivery else mMoreControlsStaticData.heading_edit_min_order_value
        minOrderValueOptionalTextView.text = if (0.0 == mMinOrderValue) mMoreControlsStaticData.text_optional else
                "${mMoreControlsStaticData.sub_heading_success_set_min_order_value_for_delivery} ${mMoreControlsStaticData.text_ruppee_symbol}"
        minOrderValueAmountTextView.text = if (0.0 != mMinOrderValue) mMinOrderValue.toString() else ""
        deliveryChargeHeadingTextView.text = mMoreControlsStaticData.heading_set_delivery_charge
        deliveryChargeTypeTextView.text = mMoreControlsStaticData.sub_heading_set_delivery_charge
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            minOrderValueContainer.id -> showMinimumDeliveryOrderBottomSheet()
            deliveryChargeContainer.id -> launchFragment(SetDeliveryChargeFragment.newInstance(mMoreControlsStaticData), true)
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
                    if (amount.toDouble() == 0.0) {
                        minDeliveryAmountEditText.error = mMoreControlsStaticData.error_amount_must_greater_than_zero
                        requestFocus()
                        return@setOnClickListener
                    }
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return@setOnClickListener
                    }
                    val request = MoreControlsRequest(
                        getStringDataFromSharedPref(Constants.STORE_ID).toInt(),
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

    override fun onMoreControlsResponse(response: MoreControlsResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showShortSnackBar(response.mMessage)
            mMinOrderValue = response.mServices.mMinOrderValue ?: 0.0
            mDeliveryPrice = response.mServices.mDeliveryPrice ?: 0.0
            mFreeDeliveryAbove = response.mServices.mFreeDeliveryAbove ?: 0.0
            mDeliveryChargeType = response.mServices.mDeliveryChargeType ?: 0
            setUIDataFromResponse()
        }
    }

    override fun onMoreControlsServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}