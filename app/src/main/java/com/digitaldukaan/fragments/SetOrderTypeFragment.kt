package com.digitaldukaan.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.PrepaidOrderUnlockOptionsAdapter
import com.digitaldukaan.adapters.PrepaidOrderWorkFlowAdapter
import com.digitaldukaan.adapters.SetOrderTypeAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.interfaces.IRecyclerViewClickListener
import com.digitaldukaan.models.request.UpdatePaymentMethodRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.SetOrderTypePageInfoResponse
import com.digitaldukaan.models.response.SetOrderTypePageStaticTextResponse
import com.digitaldukaan.services.SetOrderTypeService
import com.digitaldukaan.services.serviceinterface.ISetOrderTypeServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_set_order_type_fragment.*

class SetOrderTypeFragment: BaseFragment(), ISetOrderTypeServiceInterface, IRecyclerViewClickListener {

    private var prepaidOrderContainer: View? = null
    private var postpaidContainer: View? = null
    private var payBothContainer: View? = null
    private var prepaidOrderRadioButton: RadioButton? = null
    private var postpaidRadioButton: RadioButton? = null
    private var payBothRadioButton: RadioButton? = null
    private var mService: SetOrderTypeService? = null
    private var mSetOrderTypePageInfoResponse: SetOrderTypePageInfoResponse? = null
    private var mStaticText: SetOrderTypePageStaticTextResponse? = null
    private var mPaymentMethod = 0
    private var mPaymentMethodStr = ""
    private var mIsPrepaidCompleted = false
    private var mIsBothCompleted = false

    companion object {
        private const val TAG = "SetOrderTypeFragment"
        fun newInstance(): SetOrderTypeFragment{
            return SetOrderTypeFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_set_order_type_fragment, container, false)
        initializeUI()
        mService = SetOrderTypeService()
        mService?.setServiceInterface(this)
        showProgressDialog(mActivity)
        mService?.getOrderTypePageInfo()
        return mContentView
    }

    private fun initializeUI() {
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            onBackPressed(this@SetOrderTypeFragment)
            setSideIconVisibility(false)
            setSecondSideIconVisibility(false)
        }
        hideBottomNavigationView(true)
        payBothRadioButton = mContentView?.findViewById(R.id.payBothRadioButton)
        postpaidRadioButton = mContentView?.findViewById(R.id.postpaidRadioButton)
        prepaidOrderRadioButton = mContentView?.findViewById(R.id.prepaidOrderRadioButton)
        prepaidOrderContainer = mContentView?.findViewById(R.id.prepaidOrderContainer)
        postpaidContainer = mContentView?.findViewById(R.id.payOnPickUpDeliveryContainer)
        payBothContainer = mContentView?.findViewById(R.id.payBothContainer)
    }

    private fun setupUIWithStaticData() {
        clearRadioButtonSelection()
        val howDoesPrepaidWorkTextView: TextView? = mContentView?.findViewById(R.id.howDoesPrepaidWorkTextView)
        howDoesPrepaidWorkTextView?.text = mStaticText?.heading_how_does_prepaid_orders_works
        ToolBarManager.getInstance()?.setHeaderTitle(mStaticText?.heading_set_orders_type)
        mSetOrderTypePageInfoResponse?.mPostPaidResponse?.let {
            if (it.id == mPaymentMethod) {
                postpaidRadioButton?.isChecked = true
                mPaymentMethodStr = postpaidRadioButton?.text?.toString() ?: ""
            }
            postpaidRadioButton?.text = it.text
            postpaidRadioButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, if (it.isCompleted) 0 else R.drawable.ic_info_black_small, 0)
        }
        mIsPrepaidCompleted = false
        mIsBothCompleted = false
        mSetOrderTypePageInfoResponse?.mPrePaidResponse?.let {
            if (it.id == mPaymentMethod) {
                prepaidOrderRadioButton?.isChecked = true
                mPaymentMethodStr = prepaidOrderRadioButton?.text?.toString() ?: ""
            }
            prepaidOrderRadioButton?.text = it.text
            mIsPrepaidCompleted = it.isCompleted
            prepaidOrderRadioButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, if (it.isCompleted) 0 else R.drawable.ic_info_black_small, 0)
            val prepaidOrderTypeRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.prepaidOrderTypeRecyclerView)
            if (isEmpty(it.setOrderTypeItemList)) {
                postpaidSeparator?.visibility = View.GONE
                prepaidOrderTypeRecyclerView?.visibility = View.GONE
            } else {
                prepaidOrderTypeRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    adapter = SetOrderTypeAdapter(mActivity, it.setOrderTypeItemList, Constants.MODE_PREPAID, this@SetOrderTypeFragment)
                }
            }
        }
        mSetOrderTypePageInfoResponse?.mBothPaidResponse?.let {
            if (it.id == mPaymentMethod) {
                payBothRadioButton?.isChecked = true
                mPaymentMethodStr = payBothRadioButton?.text?.toString() ?: ""
            }
            payBothRadioButton?.text = it.text
            mIsBothCompleted = it.isCompleted
            payBothRadioButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, if (it.isCompleted) 0 else R.drawable.ic_info_black_small, 0)
            val bothOrderTypeRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.bothOrderTypeRecyclerView)
            if (isEmpty(it.setOrderTypeItemList)) {
                prepaidSeparator?.visibility = View.GONE
                bothOrderTypeRecyclerView?.visibility = View.GONE
            } else {
                bothOrderTypeRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    adapter = SetOrderTypeAdapter(mActivity, it.setOrderTypeItemList, Constants.MODE_POSTPAID, this@SetOrderTypeFragment)
                }
            }

        }
    }

    override fun onSetOrderTypeResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                stopProgress()
                if (response.mIsSuccessStatus) {
                    mSetOrderTypePageInfoResponse = Gson().fromJson(response.mCommonDataStr, SetOrderTypePageInfoResponse::class.java)
                    mSetOrderTypePageInfoResponse?.let {
                        mPaymentMethod = it.mPaymentMethod
                        mStaticText = it.mStaticText
                        setupUIWithStaticData()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "onSetOrderTypeResponse: ${e.message}", e)
            }
        }
    }

    override fun onUpdatePaymentMethodResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                stopProgress()
                response.let {
                    if (it.mIsSuccessStatus) {
                        StaticInstances.sPaymentMethodStr = mPaymentMethodStr
                        showShortSnackBar(it.mMessage, true, R.drawable.ic_check_circle)
                        mService?.getOrderTypePageInfo()
                    } else showShortSnackBar(it.mMessage, true, R.drawable.ic_close_red_small)
                }
            } catch (e: Exception) {
                Log.e(TAG, "onSetOrderTypeResponse: ${e.message}", e)
            }
        }
    }

    override fun onSetOrderTypeException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            howDoesPrepaidWorkTextView?.id -> showPrepaidOrderWorkFlowBottomSheet()
            prepaidOrderContainer?.id -> prepaidContainerClicked()
            payBothContainer?.id -> payBothContainerClicked()
            postpaidContainer?.id -> postpaidContainerClicked()
        }
    }

    private fun payBothContainerClicked() {
        mPaymentMethod = mSetOrderTypePageInfoResponse?.mBothPaidResponse?.id ?: 0
        if (payBothRadioButton?.isChecked == true) return
        if (mIsBothCompleted) showConfirmationDialog() else showUnlockOptionBottomSheet()
        mPaymentMethodStr = payBothRadioButton?.text?.toString() ?: ""
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_CHECK_PREPAID_ORDERS,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.TYPE to AFInAppEventParameterName.BOTH)
        )
    }

    private fun postpaidContainerClicked() {
        mPaymentMethod = mSetOrderTypePageInfoResponse?.mPostPaidResponse?.id ?: 0
        if (postpaidRadioButton?.isChecked == true) return
        mPaymentMethodStr = postpaidRadioButton?.text?.toString() ?: ""
        showProgressDialog(mActivity)
        mService?.updatePaymentMethod(UpdatePaymentMethodRequest(mPaymentMethod))
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_CHECK_PREPAID_ORDERS,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.TYPE to AFInAppEventParameterName.POSTPAID)
        )
    }

    private fun prepaidContainerClicked() {
        mPaymentMethod = mSetOrderTypePageInfoResponse?.mPrePaidResponse?.id ?: 0
        if (prepaidOrderRadioButton?.isChecked == true) return
        if (mIsPrepaidCompleted) showConfirmationDialog() else showUnlockOptionBottomSheet()
        mPaymentMethodStr = prepaidOrderRadioButton?.text?.toString() ?: ""
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_CHECK_PREPAID_ORDERS,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.TYPE to AFInAppEventParameterName.PREPAID)
        )
    }

    private fun clearRadioButtonSelection() {
        prepaidOrderRadioButton?.isChecked = false
        postpaidRadioButton?.isChecked = false
        payBothRadioButton?.isChecked = false
    }

    private fun showPrepaidOrderWorkFlowBottomSheet() {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_HOW_PREPAID_ORDER_WORK,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mActivity?.let {
                    val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                    val view = LayoutInflater.from(it).inflate(
                        R.layout.bottom_sheet_prepaid_work_flow,
                        it.findViewById(R.id.bottomSheetContainer)
                    )
                    bottomSheetDialog.apply {
                        setContentView(view)
                        setBottomSheetCommonProperty()
                        view.run {
                            val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                            val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                            val prepaidOrderWorkFlowRecyclerView: RecyclerView = findViewById(R.id.prepaidOrderWorkFlowRecyclerView)
                            bottomSheetHeadingTextView.text = mStaticText?.heading_how_does_prepaid_orders_works
                            bottomSheetClose.setOnClickListener {
                                bottomSheetDialog.dismiss()
                            }
                            prepaidOrderWorkFlowRecyclerView.apply {
                                layoutManager = LinearLayoutManager(mActivity)
                                adapter =
                                    PrepaidOrderWorkFlowAdapter(mSetOrderTypePageInfoResponse?.mHowItWorkList)
                            }
                        }
                    }.show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "showPrepaidOrderWorkFlowBottomSheet: ${e.message}", e)
            }
        }
    }

    private fun showUnlockOptionBottomSheet() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mActivity?.let {
                    val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                    val view = LayoutInflater.from(it).inflate(
                        R.layout.bottom_sheet_unlock_options,
                        it.findViewById(R.id.bottomSheetContainer)
                    )
                    bottomSheetDialog.apply {
                        setContentView(view)
                        setBottomSheetCommonProperty()
                        view.run {
                            val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                            val optionsRecyclerView: RecyclerView = findViewById(R.id.optionsRecyclerView)
                            bottomSheetHeadingTextView.setHtmlData(mStaticText?.heading_complete_below_steps)
                            optionsRecyclerView.apply {
                                layoutManager = LinearLayoutManager(mActivity)
                                adapter = PrepaidOrderUnlockOptionsAdapter(mSetOrderTypePageInfoResponse?.mUnlockOptionList, mActivity, object : IAdapterItemClickListener {
                                    override fun onAdapterItemClickListener(position: Int) {
                                        val item = mSetOrderTypePageInfoResponse?.mUnlockOptionList?.get(position)
                                        bottomSheetDialog.dismiss()
                                        when(item?.action) {
                                            Constants.ACTION_KYC -> {
                                                AppEventsManager.pushAppEvents(
                                                    eventName = AFInAppEventType.EVENT_COMPLETE_PREPAID_STEPS,
                                                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                                        AFInAppEventParameterName.STEP to AFInAppEventParameterName.KYC)
                                                )
                                                launchFragment(ProfilePreviewFragment().newInstance(""), true)
                                            }
                                            Constants.ACTION_DELIVERY_CHARGES -> {
                                                AppEventsManager.pushAppEvents(
                                                    eventName = AFInAppEventType.EVENT_COMPLETE_PREPAID_STEPS,
                                                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                                        AFInAppEventParameterName.STEP to AFInAppEventParameterName.DELIVERY_CHARGE)
                                                )
                                                launchFragment(SetDeliveryChargeFragment.newInstance(StaticInstances.sAccountPageSettingsStaticData), true)
                                            }
                                        }
                                    }
                                })
                            }
                        }
                    }.show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "showUnlockOptionBottomSheet: ${e.message}", e)
            }
        }
    }

    private fun showConfirmationDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.let {
                val cancelWarningDialog = Dialog(it)
                val view = LayoutInflater.from(it).inflate(R.layout.dialog_set_order_type_confirmation, null)
                cancelWarningDialog.apply {
                    setContentView(view)
                    setCancelable(true)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    view?.run {
                        val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                        val headingTextView: TextView = findViewById(R.id.headingTextView)
                        val subHeadingTextView: TextView = findViewById(R.id.subHeadingTextView)
                        val messageTextView: TextView = findViewById(R.id.messageTextView)
                        val activateTextView: TextView = findViewById(R.id.activateTextView)
                        val iAcceptCheckBox: CheckBox = findViewById(R.id.iAcceptCheckBox)
                        headingTextView.setHtmlData(mStaticText?.dialog_heading_activate_prepaid_orders)
                        subHeadingTextView.text = mStaticText?.dialog_heading_note_for_prepaid_orders
                        messageTextView.text = mStaticText?.dialog_message_note_for_prepaid_orders
                        iAcceptCheckBox.text = mStaticText?.text_i_accept
                        activateTextView.text = mStaticText?.text_activate
                        bottomSheetClose.setOnClickListener { cancelWarningDialog.dismiss() }
                        activateTextView.setOnClickListener {
                            AppEventsManager.pushAppEvents(
                                eventName = AFInAppEventType.EVENT_ACTIVATE_PREPAID_ORDER,
                                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                    AFInAppEventParameterName.TYPE to if (mPaymentMethod == 1) "Prepaid" else "both")
                            )
                            showProgressDialog(mActivity)
                            mService?.updatePaymentMethod(UpdatePaymentMethodRequest(mPaymentMethod))
                            cancelWarningDialog.dismiss()
                        }
                        iAcceptCheckBox.setOnCheckedChangeListener { _, isChecked -> activateTextView.isEnabled = isChecked }
                    }
                }.show()
            }
        }
    }

    override fun onRecyclerViewClickListener(mode: String) {
        when(mode) {
            Constants.MODE_PREPAID -> prepaidOrderContainer?.callOnClick()
            Constants.MODE_POSTPAID -> payBothContainer?.callOnClick()
        }
    }

}