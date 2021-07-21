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

    private var percentageDiscountTextView: TextView? = null
    private var pdDiscountOffTextView: TextView? = null
    private var flatDiscountTextView: TextView? = null
    private var pdDiscountUpToTextView: TextView? = null
    private var pdMaxDiscountEditText: EditText? = null
    private var pdPercentageEditText: EditText? = null
    private var pdDiscountPreviewLayout: View? = null
    private var pdMaxDiscountStr = ""
    private var pdPercentageStr = ""

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
        pdDiscountOffTextView = mContentView?.findViewById(R.id.pdDiscountOffTextView)
        pdPercentageEditText = mContentView?.findViewById(R.id.pdPercentageEditText)
        percentageDiscountTextView = mContentView?.findViewById(R.id.percentageDiscountTextView)
        pdDiscountUpToTextView = mContentView?.findViewById(R.id.pdDiscountUpToTextView)
        flatDiscountTextView = mContentView?.findViewById(R.id.flatDiscountTextView)
        pdMaxDiscountEditText = mContentView?.findViewById(R.id.pdMaxDiscountEditText)
        pdDiscountPreviewLayout = mContentView?.findViewById(R.id.pdDiscountPreviewLayout)
        setupTextWatchers()
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
            percentageDiscountTextView?.id -> {
                mActivity?.let { context ->
                    percentageDiscountTextView?.isSelected = true
                    percentageDiscountTextView?.setTextColor(
                        ContextCompat.getColor(context, R.color.black)
                    )
                    flatDiscountTextView?.isSelected = false
                    flatDiscountTextView?.setTextColor(
                        ContextCompat.getColor(context, R.color.white)
                    )
                }
            }
            flatDiscountTextView?.id -> {
                mActivity?.let { context ->
                    percentageDiscountTextView?.isSelected = false
                    percentageDiscountTextView?.setTextColor(
                        ContextCompat.getColor(context, R.color.white)
                    )
                    flatDiscountTextView?.isSelected = true
                    flatDiscountTextView?.setTextColor(
                        ContextCompat.getColor(context, R.color.black)
                    )
                }
            }
        }
    }

}