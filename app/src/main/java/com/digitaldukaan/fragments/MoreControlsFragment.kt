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
import com.digitaldukaan.models.response.MoreControlsResponse
import com.digitaldukaan.services.MoreControlsService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IMoreControlsServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.more_control_fragment.*

class MoreControlsFragment : BaseFragment(), IMoreControlsServiceInterface {

    companion object {
        private lateinit var mMoreControlsService: MoreControlsService
        private val mMoreControlsStaticData = mStaticData.mStaticData.mSettingsStaticData
        private var mMinOrderValue = 0.0
        private var mDeliveryPrice = 0.0
        private var mFreeDeliveryAbove = 0.0
        private var mDeliveryChargeType = 0

        fun newInstance(): MoreControlsFragment {
            return MoreControlsFragment()
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
            setHeaderTitle("")
            onBackPressed(this@MoreControlsFragment)
            hideBackPressFromToolBar(mActivity, false)
        }
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            minOrderValueContainer.id -> showMinimumDeliveryOrderBottomSheet()
            deliveryChargeContainer.id -> launchFragment(SetDeliveryChargeFragment.newInstance(), true)
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
                minDeliveryAmountContainer.hint = mMoreControlsStaticData.mMinOrderValueOptional
                minDeliveryHeadingTextView.text = mMoreControlsStaticData.mMinOrderValue
                verifyTextView.text = mMoreControlsStaticData.mSaveChanges
                verifyTextView.setOnClickListener {
                    val amount = minDeliveryAmountEditText.text.trim().toString()
                    if (amount.isEmpty()) {
                        minDeliveryAmountEditText.error = getString(R.string.mandatory_field_message)
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
            showToast(response.toString())
            mMinOrderValue = response.mServices.mMinOrderValue ?: 0.0
            mDeliveryPrice = response.mServices.mDeliveryPrice ?: 0.0
            mFreeDeliveryAbove = response.mServices.mFreeDeliveryAbove ?: 0.0
            mDeliveryChargeType = response.mServices.mDeliveryChargeType ?: 0
        }
    }

    override fun onMoreControlsServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}