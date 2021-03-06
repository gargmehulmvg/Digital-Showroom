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
import com.digitaldukaan.adapters.PaymentOffersBottomSheetAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IActiveOfferDetailsListener
import com.digitaldukaan.interfaces.ISwitchCheckChangeListener
import com.digitaldukaan.models.dto.PaymentModelDTO
import com.digitaldukaan.models.request.PaymentModeRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.OfferInfoArray
import com.digitaldukaan.models.response.PaymentModesResponse
import com.digitaldukaan.services.PaymentModesService
import com.digitaldukaan.services.serviceinterface.IPaymentModesServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_payment_modes.*

class PaymentModesFragment: BaseFragment(), IPaymentModesServiceInterface,
    CompoundButton.OnCheckedChangeListener {

    private var paymentModesRecyclerView: RecyclerView? = null
    private val mService: PaymentModesService = PaymentModesService()
    private var mPaymentModesPageInfoResponse: PaymentModesResponse? = null
    private var mPaymentType: String? = ""

    companion object {

        fun newInstance(): PaymentModesFragment = PaymentModesFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_SET_ONLINE_PAYMENT_MODE,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "PaymentModesFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
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
            completeKycNowTextView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_COMPLETE_KYC_FOR_PAYMENTS,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PATH to AFInAppEventParameterName.ACTIVATION_SCREEN)
                )
                launchFragment(ProfilePreviewFragment(), true)
            }
            paymentSettlementViewTextView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_VIEW_MY_PAYMENTS,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PATH to AFInAppEventParameterName.ACTIVATION_SCREEN
                    )
                )
                launchFragment(MyPaymentsFragment.newInstance(), true)
            }
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
                            val paymentModesKYCStatusResponse = mPaymentModesPageInfoResponse?.kycStatus
                            val bottomSheetHeading: TextView = findViewById(R.id.bottomSheetHeading)
                            val bottomSheetSubHeading: TextView = findViewById(R.id.bottomSheetSubHeading)
                            val completeKycTextView: TextView = findViewById(R.id.completeKycTextView)
                            val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                            val kycImageView: ImageView = findViewById(R.id.kycImageView)
                            bottomSheetHeading.text = paymentModesKYCStatusResponse?.heading_complete_your_kyc
                            bottomSheetSubHeading.text = paymentModesKYCStatusResponse?.message_complete_your_kyc
                            completeKycTextView.text = paymentModesKYCStatusResponse?.text_complete_kyc_now
                            if (!isEmpty(paymentModesKYCStatusResponse?.kycCdn)) mActivity?.let { context -> Glide.with(context).load(paymentModesKYCStatusResponse?.kycCdn).into(kycImageView) }
                            completeKycTextView.setOnClickListener {
                                this@apply.dismiss()
                                launchFragment(ProfilePreviewFragment(), true)
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
                            val paymentModesStaticText = mPaymentModesPageInfoResponse?.staticText
                            val bottomSheetHeading: TextView = findViewById(R.id.bottomSheetHeading)
                            val bottomSheetHeading2: TextView = findViewById(R.id.bottomSheetHeading2)
                            val bottomSheetSubHeading: TextView = findViewById(R.id.bottomSheetSubHeading)
                            val conditionOne: TextView = findViewById(R.id.conditionOne)
                            val conditionTwo: TextView = findViewById(R.id.conditiontwo)
                            val completeKycTextView: TextView = findViewById(R.id.completeKycTextView)
                            val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                            bottomSheetHeading.setHtmlData(paymentModesStaticText?.message_activate_and_start_payment)
                            bottomSheetHeading2.setHtmlData(paymentModesStaticText?.message_activate_and_start_payment2)
                            bottomSheetSubHeading.text = paymentModesStaticText?.text_please_note
                            conditionOne.text = paymentModesStaticText?.message_transaction_charges_will_be_applied
                            conditionTwo.text = paymentModesStaticText?.message_receive_the_amount_in_your_bank
                            completeKycTextView.text = paymentModesStaticText?.text_activate
                            completeKycTextView.setOnClickListener {
                                this@apply.dismiss()
                                AppEventsManager.pushAppEvents(
                                    eventName = AFInAppEventType.EVENT_CONFIRM_PAYMENTS_ACTIVATION,
                                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                                )
                                initiateSetPaymentModeRequest(PaymentModeRequest(1, mPaymentType))
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
                            val staticText = mPaymentModesPageInfoResponse?.staticText
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
                                initiateSetPaymentModeRequest(PaymentModeRequest(0, mPaymentType))
                            }
                            noTextView.setOnClickListener { this@apply.dismiss() }
                            bottomSheetClose.setOnClickListener { this@apply.dismiss() }
                        }
                    }.show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "showCompleteYourKYCBottomSheet: ${e.message}", e)
            }
        }
    }

    override fun onPaymentModesPageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mPaymentModesPageInfoResponse = Gson().fromJson(response.mCommonDataStr, PaymentModesResponse::class.java)
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
        val paymentModesStaticData = mPaymentModesPageInfoResponse?.staticText
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
            upiInstantSettlementTextView?.text = paymentModesStaticData?.text_instant_settlements
            val messageStr = "0% ${paymentModesStaticData?.text_txn_charge}"
            upiTxnChargeTextView?.text = messageStr
            mPaymentModesPageInfoResponse?.upi?.let { upiResponse ->
                upiTextView.text = upiResponse.name
                upiSwitch.isChecked = (1 == upiResponse.status)
                upiSwitch.setOnCheckedChangeListener(this)
                if (!isEmpty(upiResponse.imageUrl)) {
                    mActivity?.let { context -> Glide.with(context).load(upiResponse.imageUrl).into(upiImageView) }
                }
            }
            mPaymentModesPageInfoResponse?.cod?.let { codResponse ->
                codTextView.text = codResponse.name
                codSwitch.isChecked = (1 == codResponse.status)
                codSwitch.setOnCheckedChangeListener(this)
                if (!isEmpty(codResponse.imageUrl)) {
                    mActivity?.let { context -> Glide.with(context).load(codResponse.imageUrl).into(codImageView) }
                }
            }
            val isKycActive = mPaymentModesPageInfoResponse?.kycStatus?.isKycActive
            kycContainer.visibility = if (true == isKycActive) View.GONE else View.VISIBLE
            paymentSettlementContainer.visibility = if (true == isKycActive) View.VISIBLE else View.GONE
            paymentSettlementTextView.text = paymentModesStaticData?.heading_view_your_payments_and_settlements
            paymentSettlementViewTextView.text = paymentModesStaticData?.text_view_your_payment_report
            paymentModesRecyclerView = it.findViewById(R.id.paymentModesRecyclerView)
            paymentModesRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity)
                val list: ArrayList<PaymentModelDTO> = ArrayList()
                mPaymentModesPageInfoResponse?.paymentOptionsMap?.forEach { (key, arrayList) ->
                    val item = PaymentModelDTO(key, arrayList)
                    list.add(item)
                }
                adapter = PaymentModeAdapter(mActivity, isKycActive, list, object : ISwitchCheckChangeListener {

                    override fun onSwitchCheckChangeListener(switch: CompoundButton?, isChecked: Boolean, paymentType: String?) {
                        switch?.isChecked = !isChecked
                        if (isKycActive == true) {
                            mPaymentType = paymentType
                            if (!isAllListItemDisabled() && !upiSwitch.isChecked && !codSwitch.isChecked) {
                                showToast("Please enable at least 1 Payment Mode")
                                return
                            }
                            if (isChecked) {
                                showActivationBottomSheet()
                            } else showConfirmationBottomSheet()
                        } else {
                            AppEventsManager.pushAppEvents(
                                eventName = AFInAppEventType.EVENT_COMPLETE_KYC_FOR_PAYMENTS,
                                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                data = mapOf(
                                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                    AFInAppEventParameterName.PATH to AFInAppEventParameterName.LOCK_BUTTON_CLICKED)
                            )
                            showCompleteYourKYCBottomSheet()
                        }
                    }
                }, object : IActiveOfferDetailsListener {

                    override fun onActiveOfferDetailsListener(offerInfoMap: ArrayList<OfferInfoArray>?) {
                        showActiveOffersBottomSheet(offerInfoMap?.get(0))
                    }

                })
            }
        }
    }


    private fun showActiveOffersBottomSheet(data: OfferInfoArray?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mActivity?.let {
                    val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                    val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_active_offer_details, it.findViewById(R.id.activeBottomSheetContainer))
                    bottomSheetDialog.apply {
                        setContentView(view)
                        setCancelable(true)
                        view.run {
                            val descriptionTextView: TextView = findViewById(R.id.descriptionTextView)
                            val bankImageView: ImageView = findViewById(R.id.bankImageView)
                            val bottomSheetHeading: TextView = findViewById(R.id.bottomSheetHeading)
                            val couponCodeTextView: TextView = findViewById(R.id.couponCodeTextView)
                            val bottomSheetValidityTextView: TextView = findViewById((R.id.bottomSheetValidityTextView))
                            val bottomSheetRecyclerView: RecyclerView = findViewById(R.id.activeOfferBottomSheetRecyclerView)
                            mPaymentModesPageInfoResponse?.staticText?.let { staticText ->
                                bottomSheetHeading.text = staticText.text_offer_details
                                val validString = "${staticText.text_promo_valid} ${data?.endDate}"
                                bottomSheetValidityTextView.text = validString
                            }
                            bottomSheetRecyclerView.apply{
                                isNestedScrollingEnabled = false
                                layoutManager = LinearLayoutManager(context)
                                adapter = PaymentOffersBottomSheetAdapter(data?.notesList)
                            }
                            if (isNotEmpty(data?.imageUrl)) mActivity?.let { context -> Glide.with(context).load(data?.imageUrl).into(bankImageView) }
                            descriptionTextView.text = data?.description
                            couponCodeTextView.text = data?.promoCode
                        }
                    }.show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "showActiveOffersBottomSheet: ${e.message}", e)
            }
        }
    }

    private fun isAllListItemDisabled(): Boolean {
        val list: ArrayList<PaymentModelDTO> = ArrayList()
        mPaymentModesPageInfoResponse?.paymentOptionsMap?.forEach { (key, arrayList) ->
            val item = PaymentModelDTO(key, arrayList)
            list.add(item)
        }
        return list[0].value[0].status == 0
    }

    override fun onPaymentModesServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onCheckedChanged(switch: CompoundButton?, isChecked: Boolean) {
        when (switch?.id) {
            upiSwitch?.id -> {
                val request: PaymentModeRequest
                if (isChecked) {
                    upiSwitch?.isChecked = false
                    request = PaymentModeRequest(1, mPaymentModesPageInfoResponse?.upi?.paymentType)
                } else {
                    upiSwitch?.isChecked = true
                    if (isAllListItemDisabled() && codSwitch?.isChecked != true) {
                        showToast("Please enable at least 1 Payment Mode")
                        return
                    }
                    request = PaymentModeRequest(0, mPaymentModesPageInfoResponse?.upi?.paymentType)
                }
                upiSwitch.setOnCheckedChangeListener(null)
                initiateSetPaymentModeRequest(request)
            }
            codSwitch?.id -> {
                val request: PaymentModeRequest
                if (isChecked) {
                    codSwitch?.isChecked = false
                    request = PaymentModeRequest(1, mPaymentModesPageInfoResponse?.cod?.paymentType)
                } else {
                    codSwitch?.isChecked = true
                    if (isAllListItemDisabled() && upiSwitch?.isChecked != true) {
                        showToast("Please enable at least 1 Payment Mode")
                        return
                    }
                    request = PaymentModeRequest(0, mPaymentModesPageInfoResponse?.cod?.paymentType)
                }
                codSwitch.setOnCheckedChangeListener(null)
                initiateSetPaymentModeRequest(request)
            }
        }
    }

    private fun initiateSetPaymentModeRequest(request: PaymentModeRequest) {
        showProgressDialog(mActivity)
        mService.setPaymentOptions(request)
    }

    override fun onBackPressed(): Boolean {
        Log.d(TAG, "onBackPressed: called")
        if(fragmentManager != null && fragmentManager?.backStackEntryCount == 1) {
            clearFragmentBackStack()
            launchFragment(OrderFragment.newInstance(), true)
            return true
        }
        return false
    }

}