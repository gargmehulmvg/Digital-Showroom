package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import kotlinx.android.synthetic.main.more_control_fragment.*

class MoreControlsFragment : BaseFragment() {

    companion object {
        fun newInstance(): MoreControlsFragment {
            val fragment = MoreControlsFragment()
            return fragment
        }
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
            }
        }.show()
        bottomSheetDialog.show()
    }

}