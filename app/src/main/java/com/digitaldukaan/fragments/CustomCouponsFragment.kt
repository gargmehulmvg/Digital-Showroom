package com.digitaldukaan.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.digitaldukaan.R
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.constants.isEmpty

class CustomCouponsFragment : BaseFragment() {

    companion object {
        private const val TAG = "CustomCouponsFragment"
        fun newInstance(): CustomCouponsFragment = CustomCouponsFragment()
    }

    private var createCouponsTextView: TextView? = null
    private var percentageDiscountTextView: TextView? = null
    private var fdDiscountOffTextView: TextView? = null
    private var pdDiscountOffTextView: TextView? = null
    private var flatDiscountTextView: TextView? = null
    private var pdDiscountUpToTextView: TextView? = null
    private var fdDiscountUpToTextView: TextView? = null
    private var pdMaxDiscountEditText: EditText? = null
    private var pdMinOrderAmountEditText: EditText? = null
    private var fdCouponCodeEditText: EditText? = null
    private var pdCouponCodeEditText: EditText? = null
    private var pdPercentageEditText: EditText? = null
    private var fdDiscountEditText: EditText? = null
    private var pdDiscountPreviewLayout: View? = null
    private var fdDiscountPreviewLayout: View? = null
    private var pdGroup: View? = null
    private var fdGroup: View? = null
    private var fdDiscountStr = ""
    private var pdMaxDiscountStr = ""
    private var pdPercentageStr = ""
    private var mIsFlatDiscountSelected = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_custom_coupons_fragment, container, false)
        initializeUI()
        return mContentView
    }

    private fun initializeUI() {
        hideBottomNavigationView(true)
        ToolBarManager.getInstance()?.apply {
            setHeaderTitle("Custom Coupons")
            hideToolBar(mActivity, false)
            onBackPressed(this@CustomCouponsFragment)
        }
        createCouponsTextView = mContentView?.findViewById(R.id.createCouponsTextView)
        pdDiscountOffTextView = mContentView?.findViewById(R.id.pdDiscountOffTextView)
        pdPercentageEditText = mContentView?.findViewById(R.id.pdPercentageEditText)
        fdCouponCodeEditText = mContentView?.findViewById(R.id.fdCouponCodeEditText)
        pdCouponCodeEditText = mContentView?.findViewById(R.id.pdCouponCodeEditText)
        percentageDiscountTextView = mContentView?.findViewById(R.id.percentageDiscountTextView)
        pdDiscountUpToTextView = mContentView?.findViewById(R.id.pdDiscountUpToTextView)
        flatDiscountTextView = mContentView?.findViewById(R.id.flatDiscountTextView)
        pdMaxDiscountEditText = mContentView?.findViewById(R.id.pdMaxDiscountEditText)
        pdMinOrderAmountEditText = mContentView?.findViewById(R.id.pdMinOrderAmountEditText)
        pdDiscountPreviewLayout = mContentView?.findViewById(R.id.pdDiscountPreviewLayout)
        fdDiscountPreviewLayout = mContentView?.findViewById(R.id.fdDiscountPreviewLayout)
        fdDiscountUpToTextView = mContentView?.findViewById(R.id.fdDiscountUpToTextView)
        fdDiscountEditText = mContentView?.findViewById(R.id.fdDiscountEditText)
        fdDiscountOffTextView = mContentView?.findViewById(R.id.fdDiscountOffTextView)
        fdGroup = mContentView?.findViewById(R.id.fdGroup)
        pdGroup = mContentView?.findViewById(R.id.pdGroup)
        setupTextWatchers()
        percentageDiscountTextView?.callOnClick()
    }

    private fun setupTextWatchers() {
        pdMaxDiscountEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                pdMaxDiscountStr = editable?.toString() ?: ""
                pdMaxDiscountStr = pdMaxDiscountStr.trim()
                if (!isEmpty(pdPercentageStr) && !isEmpty(pdMaxDiscountStr)) {
                    pdDiscountPreviewLayout?.visibility = View.VISIBLE
                    pdDiscountUpToTextView?.text = "Upto ${getString(R.string.rupee_symbol)}$pdMaxDiscountStr"
                    pdDiscountOffTextView?.text = "$pdPercentageStr% OFF"
                } else pdDiscountPreviewLayout?.visibility = View.GONE
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: do nothing")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: do nothing")
            }

        })
        fdDiscountEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                fdDiscountStr = editable?.toString() ?: ""
                fdDiscountStr = fdDiscountStr.trim()
                if (!isEmpty(fdDiscountStr)) {
                    fdDiscountPreviewLayout?.visibility = View.VISIBLE
                    fdDiscountUpToTextView?.text = "$fdDiscountStr OFF"
                    fdDiscountOffTextView?.text = mActivity?.getString(R.string.flat)
                } else fdDiscountPreviewLayout?.visibility = View.GONE
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: do nothing")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: do nothing")
            }

        })
        pdPercentageEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                pdPercentageStr = editable?.toString() ?: ""
                pdPercentageStr = pdPercentageStr.trim()
                if (!isEmpty(pdPercentageStr) && !isEmpty(pdMaxDiscountStr)) {
                    pdDiscountPreviewLayout?.visibility = View.VISIBLE
                    pdDiscountOffTextView?.text = "$pdPercentageStr% OFF"
                    pdDiscountUpToTextView?.text = "Upto ${getString(R.string.rupee_symbol)}$pdMaxDiscountStr"
                } else pdDiscountPreviewLayout?.visibility = View.GONE
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: do nothing")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: do nothing")
            }

        })
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            createCouponsTextView?.id -> {
                if (mIsFlatDiscountSelected) {
                    checkFlatDiscountValidation()
                } else checkPercentageDiscountValidation()
            }
            percentageDiscountTextView?.id -> {
                mIsFlatDiscountSelected = false
                mActivity?.let { context ->
                    percentageDiscountTextView?.isSelected = true
                    percentageDiscountTextView?.setTextColor(ContextCompat.getColor(context, R.color.black))
                    flatDiscountTextView?.isSelected = false
                    flatDiscountTextView?.setTextColor(ContextCompat.getColor(context, R.color.white))
                    pdGroup?.visibility = View.VISIBLE
                    fdGroup?.visibility = View.GONE
                }
            }
            flatDiscountTextView?.id -> {
                mIsFlatDiscountSelected = true
                mActivity?.let { context ->
                    percentageDiscountTextView?.isSelected = false
                    percentageDiscountTextView?.setTextColor(ContextCompat.getColor(context, R.color.white))
                    flatDiscountTextView?.isSelected = true
                    flatDiscountTextView?.setTextColor(ContextCompat.getColor(context, R.color.black))
                    pdGroup?.visibility = View.GONE
                    fdGroup?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkPercentageDiscountValidation() {
        val percentage = pdPercentageEditText?.text?.toString() ?: ""
        val maxAmount = pdMaxDiscountEditText?.text?.toString() ?: ""
        val minOrderAmount = pdMinOrderAmountEditText?.text?.toString() ?: ""
        val code = pdCouponCodeEditText?.text?.toString() ?: ""
        if (isEmpty(percentage.trim())) {
            pdPercentageEditText?.apply {
                requestFocus()
                error = mActivity?.getString(R.string.mandatory_field_message)
            }
            return
        }
        if (isEmpty(maxAmount.trim())) {
            pdMaxDiscountEditText?.apply {
                requestFocus()
                error = mActivity?.getString(R.string.mandatory_field_message)
            }
            return
        }
        if (isEmpty(minOrderAmount.trim())) {
            pdMinOrderAmountEditText?.apply {
                requestFocus()
                error = mActivity?.getString(R.string.mandatory_field_message)
            }
            return
        }
        if (isEmpty(code.trim())) {
            pdCouponCodeEditText?.apply {
                requestFocus()
                error = mActivity?.getString(R.string.mandatory_field_message)
            }
            return
        }
    }

    private fun checkFlatDiscountValidation() {
        val discount = fdDiscountEditText?.text?.toString() ?: ""
        val code = fdCouponCodeEditText?.text?.toString() ?: ""
        val fdMinOrderAmountEditText: EditText? = mContentView?.findViewById(R.id.fdMinOrderAmountEditText)
        val minOrderAmount = fdMinOrderAmountEditText?.text?.toString() ?: ""
        if (isEmpty(discount.trim())) {
            fdDiscountEditText?.apply {
                requestFocus()
                error = mActivity?.getString(R.string.mandatory_field_message)
            }
            return
        }
        if (isEmpty(minOrderAmount.trim())) {
            fdMinOrderAmountEditText?.apply {
                requestFocus()
                error = mActivity?.getString(R.string.mandatory_field_message)
            }
            return
        }
        if (isEmpty(code.trim())) {
            fdCouponCodeEditText?.apply {
                requestFocus()
                error = mActivity?.getString(R.string.mandatory_field_message)
            }
            return
        }
    }

}