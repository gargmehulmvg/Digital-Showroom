package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.adapters.PaymentModeAdapter
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.constants.isEmpty
import com.digitaldukaan.interfaces.ISwitchCheckChangeListener
import com.digitaldukaan.models.dto.PaymentModelDTO
import com.digitaldukaan.models.request.PaymentModeRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.PaymentModesResponse
import com.digitaldukaan.services.PaymentModesService
import com.digitaldukaan.services.serviceinterface.IPaymentModesServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_payment_modes.*

class PaymentModesFragment: BaseFragment(), IPaymentModesServiceInterface,
    CompoundButton.OnCheckedChangeListener {

    private var paymentModesRecyclerView: RecyclerView? = null
    private val mService: PaymentModesService = PaymentModesService()
    private var mPaymentModesResponse: PaymentModesResponse? = null
    private var mPaymentType: String? = ""

    companion object {
        private const val TAG = "PaymentModesFragment"

        fun newInstance(): PaymentModesFragment = PaymentModesFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_payment_modes, container, false)
        initializeUI()
        mService.setServiceInterface(this)
        showProgressDialog(mActivity)
        mService.getPaymentModesPageInfo()
        return mContentView
    }

    private fun initializeUI() {
        ToolBarManager.getInstance()?.apply { hideToolBar(mActivity, true) }
        hideBottomNavigationView(true)
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            backButtonToolbar?.id -> mActivity?.onBackPressed()
            completeKycNowTextView?.id -> showCompleteYourKYCBottomSheet()
        }
    }

    private fun showCompleteYourKYCBottomSheet() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mActivity?.let {
                    val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                    val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_complete_your_kyc, it.findViewById(R.id.bottomSheetContainer))
                    bottomSheetDialog.apply {
                        setContentView(view)
                        setCancelable(true)
                        view.run {
                            val paymentModesKYCStatusResponse = mPaymentModesResponse?.kycStatus
                            val bottomSheetHeading: TextView = findViewById(R.id.bottomSheetHeading)
                            val bottomSheetSubHeading: TextView = findViewById(R.id.bottomSheetSubHeading)
                            val completeKycTextView: TextView = findViewById(R.id.completeKycTextView)
                            val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                            bottomSheetHeading.text = paymentModesKYCStatusResponse?.heading_complete_your_kyc
                            bottomSheetSubHeading.text = paymentModesKYCStatusResponse?.message_complete_your_kyc
                            completeKycTextView.text = paymentModesKYCStatusResponse?.text_complete_kyc_now
                            completeKycTextView.setOnClickListener {
                                this@apply.dismiss()
                                launchFragment(ProfilePreviewFragment(), true)
                            }
                            bottomSheetClose.setOnClickListener {
                                this@apply.dismiss()
                            }
                        }
                    }.show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "showCompleteYourKYCBottomSheet: ${e.message}", e)
            }
        }
    }

    private fun showActivationBottomSheet() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mActivity?.let {
                    val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                    val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_payment_mode_activation, it.findViewById(R.id.bottomSheetContainer))
                    bottomSheetDialog.apply {
                        setContentView(view)
                        setCancelable(true)
                        view.run {
                            val paymentModesStaticText = mPaymentModesResponse?.staticText
                            val bottomSheetHeading: TextView = findViewById(R.id.bottomSheetHeading)
                            val bottomSheetSubHeading: TextView = findViewById(R.id.bottomSheetSubHeading)
                            val conditionOne: TextView = findViewById(R.id.conditionOne)
                            val conditionTwo: TextView = findViewById(R.id.conditiontwo)
                            val completeKycTextView: TextView = findViewById(R.id.completeKycTextView)
                            val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                            bottomSheetHeading.text = paymentModesStaticText?.message_activate_and_start_payment
                            bottomSheetSubHeading.text = paymentModesStaticText?.text_please_note
                            conditionOne.text = paymentModesStaticText?.message_transaction_charges_will_be_applied
                            conditionTwo.text = paymentModesStaticText?.message_receive_the_amount_in_your_bank
                            completeKycTextView.text = paymentModesStaticText?.text_activate
                            completeKycTextView.setOnClickListener {
                                this@apply.dismiss()
                            }
                            bottomSheetClose.setOnClickListener { this@apply.dismiss() }
                        }
                    }.show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "showCompleteYourKYCBottomSheet: ${e.message}", e)
            }
        }
    }

    private fun showConfirmationBottomSheet() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mActivity?.let {
                    val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                    val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_payment_mode_confirmation, it.findViewById(R.id.bottomSheetContainer))
                    bottomSheetDialog.apply {
                        setContentView(view)
                        setCancelable(true)
                        view.run {
                            val staticText = mPaymentModesResponse?.staticText
                            val bottomSheetHeading: TextView = findViewById(R.id.bottomSheetHeading)
                            val bottomSheetSubHeading: TextView = findViewById(R.id.bottomSheetSubHeading)
                            val yesTextView: TextView = findViewById(R.id.yesTextView)
                            val noTextView: TextView = findViewById(R.id.noTextView)
                            val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                            bottomSheetHeading.text = staticText?.text_confirmation
                            yesTextView.text = staticText?.text_yes
                            bottomSheetSubHeading.text = staticText?.message_confirm_payment_mode
                            noTextView.text = staticText?.text_no
                            yesTextView.setOnClickListener {
                                this@apply.dismiss()
                            }
                            noTextView.setOnClickListener {
                                this@apply.dismiss()
                            }
                            bottomSheetClose.setOnClickListener { this@apply.dismiss() }
                        }
                    }.show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "showCompleteYourKYCBottomSheet: ${e.message}", e)
            }
        }
    }

    override fun onPaymentModesResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mPaymentModesResponse = Gson().fromJson<PaymentModesResponse>(response.mCommonDataStr, PaymentModesResponse::class.java)
            setupUIFromResponse()
        }
    }

    override fun onSetPaymentModesResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                showProgressDialog(mActivity)
                mService.getPaymentModesPageInfo()
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    private fun setupUIFromResponse() {
        val paymentModesStaticData = mPaymentModesResponse?.staticText
        mContentView?.let {
            val paymentModeHeadingTextView: TextView = it.findViewById(R.id.paymentModeHeadingTextView)
            val paymentModeSubHeadingTextView: TextView = it.findViewById(R.id.paymentModeSubHeadingTextView)
            val completeKycTextView: TextView = it.findViewById(R.id.completeKycTextView)
            val completeKycNowTextView: TextView = it.findViewById(R.id.completeKycNowTextView)
            val codSwitch: SwitchMaterial = it.findViewById(R.id.codSwitch)
            val upiSwitch: SwitchMaterial = it.findViewById(R.id.upiSwitch)
            val codTextView: TextView = it.findViewById(R.id.codTextView)
            val upiTextView: TextView = it.findViewById(R.id.headingTextView)
            val paymentSettlementTextView: TextView = it.findViewById(R.id.paymentSettlementTextView)
            val paymentSettlementViewTextView: TextView = it.findViewById(R.id.paymentSettlementViewTextView)
            val upiImageView: ImageView = it.findViewById(R.id.upiImageView)
            val codImageView: ImageView = it.findViewById(R.id.codImageView)
            val kycContainer: View = it.findViewById(R.id.kycContainer)
            val paymentSettlementContainer: View = it.findViewById(R.id.paymentSettlementContainer)
            paymentModeHeadingTextView.text = paymentModesStaticData?.page_heading_payment_mode
            paymentModeSubHeadingTextView.text = paymentModesStaticData?.page_sub_heading_payment_mode
            completeKycTextView.text = paymentModesStaticData?.message_complete_kyc_to_unlock
            completeKycNowTextView.text = paymentModesStaticData?.text_complete_kyc_now
            mPaymentModesResponse?.upi?.let { upiResponse ->
                upiTextView.text = upiResponse.name
                upiSwitch.isChecked = (1 == upiResponse.status)
                upiSwitch.setOnCheckedChangeListener(this)
                if (!isEmpty(upiResponse.imageUrl)) {
                    mActivity?.let { context -> Glide.with(context).load(upiResponse.imageUrl).into(upiImageView) }
                }
            }
            mPaymentModesResponse?.cod?.let { codResponse ->
                codTextView.text = codResponse.name
                codSwitch.isChecked = (1 == codResponse.status)
                codSwitch.setOnCheckedChangeListener(this)
                if (!isEmpty(codResponse.imageUrl)) {
                    mActivity?.let { context -> Glide.with(context).load(codResponse.imageUrl).into(codImageView) }
                }
            }
            val isKycActive = !mPaymentModesResponse?.kycStatus?.isKycActive!!
            kycContainer.visibility = if (true == isKycActive) View.GONE else View.VISIBLE
            paymentSettlementContainer.visibility = if (true == isKycActive) View.VISIBLE else View.GONE
            paymentSettlementTextView.text = paymentModesStaticData?.heading_view_your_payments_and_settlements
            paymentSettlementViewTextView.text = paymentModesStaticData?.text_view_your_payment_report
            paymentModesRecyclerView = it.findViewById(R.id.paymentModesRecyclerView)
            paymentModesRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity)
                val list: ArrayList<PaymentModelDTO> = ArrayList()
                mPaymentModesResponse?.paymentOptionsMap?.forEach { key, arrayList ->
                    val item = PaymentModelDTO(key, arrayList)
                    list.add(item)
                }
                adapter = PaymentModeAdapter(mActivity, isKycActive, list, object : ISwitchCheckChangeListener {

                    override fun onSwitchCheckChangeListener(switch: CompoundButton?, isChecked: Boolean, paymentType: String?) {
                        mPaymentType = paymentType
                        onCheckedChanged(switch, isChecked)
                        switch?.isChecked = !isChecked
                    }
                })
            }
        }
    }

    override fun onPaymentModesServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onCheckedChanged(switch: CompoundButton?, isChecked: Boolean) {
        when (switch?.id) {
            upiSwitch?.id -> {
                val request: PaymentModeRequest
                if (isChecked) {
                    upiSwitch?.isChecked = false
                    request = PaymentModeRequest(1, mPaymentModesResponse?.upi?.paymentType)
                } else {
                    upiSwitch?.isChecked = true
                    request = PaymentModeRequest(0, mPaymentModesResponse?.upi?.paymentType)
                }
                initiateSetPaymentModeRequest(request)
            }
            codSwitch?.id -> {
                val request: PaymentModeRequest
                if (isChecked) {
                    codSwitch?.isChecked = false
                    request = PaymentModeRequest(1, mPaymentModesResponse?.cod?.paymentType)
                } else {
                    codSwitch?.isChecked = true
                    request = PaymentModeRequest(0, mPaymentModesResponse?.cod?.paymentType)
                }
                initiateSetPaymentModeRequest(request)
            }
            else -> {
                val request: PaymentModeRequest = if (isChecked) {
                    PaymentModeRequest(1, mPaymentType)
                } else {
                    PaymentModeRequest(0, mPaymentType)
                }
                initiateSetPaymentModeRequest(request)
            }
        }
    }

    private fun initiateSetPaymentModeRequest(request: PaymentModeRequest) {
        showProgressDialog(mActivity)
        mService.setPaymentOptions(request)
    }

}