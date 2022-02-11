package com.digitaldukaan.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.CreateCouponsRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.PromoCodePageInfoResponse
import com.digitaldukaan.models.response.PromoCodePageStaticTextResponse
import com.digitaldukaan.services.CustomCouponsService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ICustomCouponsServiceInterface
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import java.util.*

class CustomCouponsFragment : BaseFragment(), ICustomCouponsServiceInterface {

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
    private var mService = CustomCouponsService()
    private var mCreateCouponsRequest: CreateCouponsRequest? = null
    private var mStaticText: PromoCodePageStaticTextResponse? = null
    private var mStoreName = ""

    companion object {

        fun newInstance(staticText: PromoCodePageStaticTextResponse?): CustomCouponsFragment {
            val fragment = CustomCouponsFragment()
            fragment.mStaticText = staticText
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "CustomCouponsFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_custom_coupons_fragment, container, false)
        if (null == mStaticText) mService.getPromoCodePageInfo()
        initializeUI()
        mService.setCustomCouponsServiceListener(this)
        mStoreName = PrefsManager.getStringDataFromSharedPref(Constants.STORE_NAME)
        return mContentView
    }

    private fun initializeUI() {
        hideBottomNavigationView(true)
        ToolBarManager.getInstance()?.apply {
            setSideIconVisibility(false)
            setSecondSideIconVisibility(false)
            headerTitle = mStaticText?.heading_custom_coupon
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
        setStaticTextToUI()
        percentageDiscountTextView?.callOnClick()
    }

    private fun setStaticTextToUI() {
        mStaticText?.let {text ->
            percentageDiscountTextView?.text = text.text_percent_discount
            flatDiscountTextView?.text = text.text_flat_discount
            createCouponsTextView?.text = text.text_create_coupon
            val selectDiscountTypeTextView: TextView? = mContentView?.findViewById(R.id.selectDiscountTypeTextView)
            val couponSettingHeadingTextView: TextView? = mContentView?.findViewById(R.id.couponSettingHeadingTextView)
            val setting1Heading: TextView? = mContentView?.findViewById(R.id.setting1Heading)
            val setting2Heading: TextView? = mContentView?.findViewById(R.id.setting2Heading)
            val setting1Message: TextView? = mContentView?.findViewById(R.id.setting1Message)
            val setting2Message: TextView? = mContentView?.findViewById(R.id.setting2Message)
            val pdPercentageInputLayout: TextInputLayout? = mContentView?.findViewById(R.id.pdPercentageInputLayout)
            val pdMaxDiscountInputLayout: TextInputLayout? = mContentView?.findViewById(R.id.pdMaxDiscountInputLayout)
            val pdMinOrderAmountInputLayout: TextInputLayout? = mContentView?.findViewById(R.id.pdMinOrderAmountInputLayout)
            val pdCouponCodeInputLayout: TextInputLayout? = mContentView?.findViewById(R.id.pdCouponCodeInputLayout)
            val fdDiscountInputLayout: TextInputLayout? = mContentView?.findViewById(R.id.fdDiscountInputLayout)
            val fdMinOrderAmountInputLayout: TextInputLayout? = mContentView?.findViewById(R.id.fdMinOrderAmountInputLayout)
            val fdCouponCodeInputLayout: TextInputLayout? = mContentView?.findViewById(R.id.fdCouponCodeInputLayout)
            pdPercentageInputLayout?.hint = text.text_enter_percentage
            pdMaxDiscountInputLayout?.hint = text.text_enter_max_discount
            pdMinOrderAmountInputLayout?.hint = text.text_enter_min_discount
            pdCouponCodeInputLayout?.hint = text.text_coupon_code
            fdDiscountInputLayout?.hint = text.text_discount
            fdMinOrderAmountInputLayout?.hint = text.text_enter_min_discount
            fdCouponCodeInputLayout?.hint = text.text_coupon_code
            couponSettingHeadingTextView?.text = text.text_coupon_settings
            setting1Heading?.text = text.heading_applicable_once_per_customer
            setting2Heading?.text = text.heading_show_this_coupon_website
            setting1Message?.text = text.message_select_this_coupon
            setting2Message?.text = text.message_allow_customer_see_coupon
            selectDiscountTypeTextView?.text = text.heading_select_discount_type
        }
    }

    private fun setupTextWatchers() {
        pdMaxDiscountEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                pdMaxDiscountStr = editable?.toString() ?: ""
                pdMaxDiscountStr = pdMaxDiscountStr.trim()
                if (!isEmpty(pdPercentageStr) && !isEmpty(pdMaxDiscountStr)) {
                    pdDiscountPreviewLayout?.visibility = View.VISIBLE
                    pdDiscountUpToTextView?.text = "${mStaticText?.text_upto_capital} ₹$pdMaxDiscountStr"
                    pdDiscountOffTextView?.text = "$pdPercentageStr% ${mStaticText?.text_off_all_caps}"
                } else pdDiscountPreviewLayout?.visibility = View.GONE
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })
        fdDiscountEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                fdDiscountStr = editable?.toString() ?: ""
                fdDiscountStr = fdDiscountStr.trim()
                if (!isEmpty(fdDiscountStr)) {
                    fdDiscountPreviewLayout?.visibility = View.VISIBLE
                    fdDiscountUpToTextView?.text = "₹$fdDiscountStr ${mStaticText?.text_off_all_caps}"
                    fdDiscountOffTextView?.text = mStaticText?.text_flat
                } else fdDiscountPreviewLayout?.visibility = View.GONE
                if (!isEmpty(fdDiscountStr)) {
                    val storeNameStr = getCouponStoreName()
                    val couponCodeStr = "$storeNameStr$fdDiscountStr"
                    fdCouponCodeEditText?.setText(couponCodeStr)
                } else fdCouponCodeEditText?.text = null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })
        pdPercentageEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                pdPercentageStr = editable?.toString() ?: ""
                pdPercentageStr = pdPercentageStr.trim()
                if (!isEmpty(pdPercentageStr) && !isEmpty(pdMaxDiscountStr)) {
                    pdDiscountPreviewLayout?.visibility = View.VISIBLE
                    pdDiscountOffTextView?.text = "$pdPercentageStr% ${mStaticText?.text_off_all_caps}"
                    pdDiscountUpToTextView?.text = "${mStaticText?.text_upto_capital} ₹$pdMaxDiscountStr"
                } else pdDiscountPreviewLayout?.visibility = View.GONE
                if (!isEmpty(pdPercentageStr)) {
                    val storeNameStr = getCouponStoreName()
                    val couponCodeStr = "$storeNameStr$pdPercentageStr"
                    pdCouponCodeEditText?.setText(couponCodeStr)
                } else pdCouponCodeEditText?.text = null
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })
    }

    private fun getCouponStoreName(): String {
        return when {
            isEmpty(mStoreName) -> ""
            mStoreName.length <= 3 -> mStoreName
            else -> mStoreName.trim().substring(0, 3).toUpperCase(Locale.getDefault())
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            createCouponsTextView?.id -> {
                if (mIsFlatDiscountSelected) checkFlatDiscountValidation() else checkPercentageDiscountValidation()
            }
            percentageDiscountTextView?.id -> {
                mIsFlatDiscountSelected = false
                mActivity?.let { context ->
                    percentageDiscountTextView?.apply {
                        isSelected = true
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_percentage_discount_white, 0, 0, 0)
                    }
                    flatDiscountTextView?.apply {
                        isSelected = false
                        setTextColor(ContextCompat.getColor(context, R.color.black))
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flat_discount_black, 0, 0, 0)
                    }
                    pdGroup?.visibility = View.VISIBLE
                    fdGroup?.visibility = View.GONE
                }
            }
            flatDiscountTextView?.id -> {
                mIsFlatDiscountSelected = true
                mActivity?.let { context ->
                    percentageDiscountTextView?.apply {
                        isSelected = false
                        setTextColor(ContextCompat.getColor(context, R.color.black))
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_percentage_discount_black, 0, 0, 0)
                    }
                    flatDiscountTextView?.apply {
                        isSelected = true
                        setTextColor(ContextCompat.getColor(context, R.color.white))
                        setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_flat_discount_white, 0, 0, 0)
                    }
                    pdGroup?.visibility = View.GONE
                    fdGroup?.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun checkPercentageDiscountValidation() {
        val setting1CheckBox: CheckBox? = mContentView?.findViewById(R.id.setting1CheckBox)
        val setting2CheckBox: CheckBox? = mContentView?.findViewById(R.id.setting2CheckBox)
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
        if (0 == percentage.toInt()) {
            pdPercentageEditText?.apply {
                requestFocus()
                error = mActivity?.getString(R.string.please_enter_valid_input)
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
        if (0 == maxAmount.toInt()) {
            pdMaxDiscountEditText?.apply {
                requestFocus()
                error = mActivity?.getString(R.string.please_enter_valid_input)
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
        mCreateCouponsRequest = CreateCouponsRequest(
            discountType =  Constants.MODE_COUPON_TYPE_PERCENTAGE,
            promoCode =     code.toUpperCase(Locale.getDefault()),
            discount =      percentage.toDouble(),
            minOrderPrice = minOrderAmount.toDouble(),
            maxDiscount =   maxAmount.toDouble(),
            isOneTime =     setting1CheckBox?.isChecked ?: false,
            isHidden =      setting2CheckBox?.isChecked ?: false
        )
        showCreateCouponConfirmationBottomSheet()
    }

    private fun checkFlatDiscountValidation() {
        val setting1CheckBox: CheckBox? = mContentView?.findViewById(R.id.setting1CheckBox)
        val setting2CheckBox: CheckBox? = mContentView?.findViewById(R.id.setting2CheckBox)
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
        if (0 == discount.toInt()) {
            fdDiscountEditText?.apply {
                requestFocus()
                error = mActivity?.getString(R.string.please_enter_valid_input)
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
        mCreateCouponsRequest = CreateCouponsRequest(
            discountType =  Constants.MODE_COUPON_TYPE_FLAT,
            promoCode =     code.toUpperCase(Locale.getDefault()),
            discount =      discount.toDouble(),
            minOrderPrice = minOrderAmount.toDouble(),
            maxDiscount =   discount.toDouble(),
            isOneTime =     setting1CheckBox?.isChecked ?: false,
            isHidden =      setting2CheckBox?.isChecked ?: false
        )
        showCreateCouponConfirmationBottomSheet()
    }

    private fun showCreateCouponConfirmationBottomSheet() {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_SHOW_COUPON,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.COUPON_ID to mCreateCouponsRequest?.promoCode,
                AFInAppEventParameterName.PATH to AFInAppEventParameterName.COUPON_ADD_DETAILS_SCREEN
            )
        )
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_CREATE_COUPON,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.PATH to AFInAppEventParameterName.COUPON_ADD_DETAILS_SCREEN
            )
        )
        mActivity?.run {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_create_coupon_confirmation, findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                view.run {
                    val applicablePerCustomerCheckBox: CheckBox = findViewById(R.id.setting1CheckBox)
                    val showOnWebsiteCheckBox: CheckBox = findViewById(R.id.setting2CheckBox)
                    val createCouponsTextView: TextView = findViewById(R.id.createCouponsTextView)
                    val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                    val bottomSheetClose: ImageView = findViewById(R.id.bottomSheetClose)
                    val couponSettingHeadingTextView: TextView? = findViewById(R.id.couponSettingHeadingTextView)
                    val setting1Heading: TextView? = findViewById(R.id.setting1Heading)
                    val setting2Heading: TextView? = findViewById(R.id.setting2Heading)
                    val setting1Message: TextView? = findViewById(R.id.setting1Message)
                    val setting2Message: TextView? = findViewById(R.id.setting2Message)
                    val useCodeTextView: TextView? = findViewById(R.id.useCodeTextView)
                    val descriptionTextView: TextView? = findViewById(R.id.descriptionTextView)
                    val minOrderValueTextView: TextView? = findViewById(R.id.minOrderValueTextView)
                    couponSettingHeadingTextView?.text = mStaticText?.text_coupon_settings
                    createCouponsTextView.text = mStaticText?.text_create_coupon
                    setting1Heading?.text = mStaticText?.heading_applicable_once_per_customer
                    setting2Heading?.text = mStaticText?.heading_show_this_coupon_website
                    setting1Message?.text = mStaticText?.message_select_this_coupon
                    setting2Message?.text = mStaticText?.message_allow_customer_see_coupon
                    useCodeTextView?.text = "${mStaticText?.text_use_code} ${mCreateCouponsRequest?.promoCode}"
                    minOrderValueTextView?.text = "${mStaticText?.text_min_order_amount} ₹${mCreateCouponsRequest?.minOrderPrice?.toInt()}"
                    val discountStr = if (mIsFlatDiscountSelected) {
                        "${mStaticText?.text_flat} ₹${mCreateCouponsRequest?.discount?.toInt()} ${mStaticText?.text_off_all_caps}"
                    } else {
                        "${mCreateCouponsRequest?.discount?.toInt()}% ${mStaticText?.text_off_all_caps} ${mStaticText?.text_upto_capital} ₹${mCreateCouponsRequest?.maxDiscount?.toInt()}"
                    }
                    descriptionTextView?.text = discountStr
                    bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                    bottomSheetHeadingTextView.text = mStaticText?.heading_please_confirm
                    applicablePerCustomerCheckBox.isChecked = mCreateCouponsRequest?.isOneTime ?: false
                    applicablePerCustomerCheckBox.setOnCheckedChangeListener { _, isChecked -> mCreateCouponsRequest?.isOneTime = isChecked }
                    showOnWebsiteCheckBox.isChecked = mCreateCouponsRequest?.isHidden ?: false
                    showOnWebsiteCheckBox.setOnCheckedChangeListener { _, isChecked -> mCreateCouponsRequest?.isHidden = isChecked }
                    createCouponsTextView.setOnClickListener {
                        bottomSheetDialog.dismiss()
                        createCoupon()
                    }
                }
            }.show()
        }
    }

    private fun createCoupon() {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_SHOW_COUPON,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.COUPON_ID to mCreateCouponsRequest?.promoCode,
                AFInAppEventParameterName.PATH to AFInAppEventParameterName.COUPON_CONFIRMS_SCREEN
            )
        )
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_CREATE_COUPON,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.PATH to AFInAppEventParameterName.COUPON_CONFIRMS_SCREEN
            )
        )
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        mCreateCouponsRequest?.isHidden = !(mCreateCouponsRequest?.isHidden ?: false)
        showProgressDialog(mActivity)
        mService.getCreatePromoCode(mCreateCouponsRequest)
    }

    override fun onCustomCouponsErrorResponse(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onCustomCouponsResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showShortSnackBar(response.mMessage, true, if (response.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red)
            if (response.mIsSuccessStatus) mActivity?.onBackPressed()
        }
    }

    override fun onPromoCodePageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val pageInfoResponse = Gson().fromJson(response.mCommonDataStr, PromoCodePageInfoResponse::class.java)
                mStaticText = pageInfoResponse?.mStaticText
                initializeUI()
            }
        }
    }

}