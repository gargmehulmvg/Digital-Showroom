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
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.SetOrderTypePageInfoResponse
import com.digitaldukaan.models.response.SetOrderTypePageStaticTextResponse
import com.digitaldukaan.services.SetOrderTypeService
import com.digitaldukaan.services.serviceinterface.ISetOrderTypeServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_set_order_type_fragment.*

class SetOrderTypeFragment: BaseFragment(), ISetOrderTypeServiceInterface {

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
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            setSideIconVisibility(false)
            setSecondSideIconVisibility(false)
        }
        hideBottomNavigationView(true)
        setupUI()
        mService = SetOrderTypeService()
        mService?.setServiceInterface(this)
        showProgressDialog(mActivity)
        mService?.getOrderTypePageInfo()
        return mContentView
    }

    private fun setupUI() {
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
            if (it.id == mPaymentMethod) postpaidRadioButton?.isChecked = true
            postpaidRadioButton?.text = it.text
            postpaidRadioButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, if (it.isCompleted) 0 else R.drawable.ic_info_black_small, 0)
        }
        mSetOrderTypePageInfoResponse?.mPrePaidResponse?.let {
            if (it.id == mPaymentMethod) prepaidOrderRadioButton?.isChecked = true
            prepaidOrderRadioButton?.text = it.text
            mIsPrepaidCompleted = it.isCompleted
            prepaidOrderRadioButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, if (it.isCompleted) 0 else R.drawable.ic_info_black_small, 0)
            val prepaidOrderTypeRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.prepaidOrderTypeRecyclerView)
            prepaidOrderTypeRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity)
                adapter = SetOrderTypeAdapter(mActivity, it.setOrderTypeItemList)
            }
        }
        mSetOrderTypePageInfoResponse?.mBothPaidResponse?.let {
            if (it.id == mPaymentMethod) payBothRadioButton?.isChecked = true
            payBothRadioButton?.text = it.text
            mIsBothCompleted = it.isCompleted
            payBothRadioButton?.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, if (it.isCompleted) 0 else R.drawable.ic_pending_small_black, 0)
            val bothOrderTypeRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.bothOrderTypeRecyclerView)
            bothOrderTypeRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity)
                adapter = SetOrderTypeAdapter(mActivity, it.setOrderTypeItemList)
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

    override fun onSetOrderTypeException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            howDoesPrepaidWorkTextView?.id -> showPrepaidOrderWorkFlowBottomSheet()
            prepaidOrderContainer?.id -> {
                clearRadioButtonSelection()
                prepaidOrderRadioButton?.isChecked = true
                if (mIsPrepaidCompleted) showConfirmationDialog() else showUnlockOptionBottomSheet()
            }
            payBothContainer?.id -> {
                clearRadioButtonSelection()
                payBothRadioButton?.isChecked = true
                if (mIsBothCompleted) showConfirmationDialog() else showUnlockOptionBottomSheet()
            }
            postpaidContainer?.id -> {
                clearRadioButtonSelection()
                postpaidRadioButton?.isChecked = true
            }
        }
    }

    private fun clearRadioButtonSelection() {
        prepaidOrderRadioButton?.isChecked = false
        postpaidRadioButton?.isChecked = false
        payBothRadioButton?.isChecked = false
    }

    private fun showPrepaidOrderWorkFlowBottomSheet() {
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
                        adapter = PrepaidOrderWorkFlowAdapter(mSetOrderTypePageInfoResponse?.mHowItWorkList)
                    }
                }
            }.show()
        }
    }

    private fun showUnlockOptionBottomSheet() {
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
                        adapter = PrepaidOrderUnlockOptionsAdapter(mSetOrderTypePageInfoResponse?.mUnlockOptionList, mActivity)
                    }
                }
            }.show()
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
                        activateTextView.setOnClickListener { cancelWarningDialog.dismiss() }
                        iAcceptCheckBox.setOnCheckedChangeListener { _, isChecked -> activateTextView.isEnabled = isChecked }
                    }
                }.show()
            }
        }
    }

}