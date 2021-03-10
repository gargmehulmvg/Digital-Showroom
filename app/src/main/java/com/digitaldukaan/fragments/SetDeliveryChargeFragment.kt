package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.AccountStaticTextResponse
import com.digitaldukaan.services.isInternetConnectionAvailable
import kotlinx.android.synthetic.main.set_delivery_charge_fragment.*

class SetDeliveryChargeFragment : BaseFragment() {

    private lateinit var mMoreControlsStaticData: AccountStaticTextResponse

    companion object {
        fun newInstance(appSettingsResponseStaticData: AccountStaticTextResponse): SetDeliveryChargeFragment {
            val fragment = SetDeliveryChargeFragment()
            fragment.mMoreControlsStaticData = appSettingsResponseStaticData
            return fragment
        }
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
        showBottomNavigationView(true)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showFreeDeliveryContainer(true)
        showFixedDeliveryContainer(false)
        showCustomDeliveryContainer(false)
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

}