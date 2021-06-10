package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.PaymentModesResponse
import com.digitaldukaan.services.PaymentModesService
import com.digitaldukaan.services.serviceinterface.IPaymentModesServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_payment_modes.*

class PaymentModesFragment: BaseFragment(), IPaymentModesServiceInterface {

    private var paymentModesRecyclerView: RecyclerView? = null
    private val mService: PaymentModesService = PaymentModesService()
    private var mPaymentModesResponse: PaymentModesResponse? = null

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
        mContentView?.let { view ->
            paymentModesRecyclerView = view.findViewById(R.id.paymentModesRecyclerView)
            paymentModesRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity)
                adapter = PaymentModeAdapter(mActivity)
            }
        }
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
                            val completeKycTextView: TextView = findViewById(R.id.completeKycTextView)
                            val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
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

    override fun onPaymentModesResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mPaymentModesResponse = Gson().fromJson<PaymentModesResponse>(response.mCommonDataStr, PaymentModesResponse::class.java)
            Log.d(TAG, "onPaymentModesResponse: MAP :: ${mPaymentModesResponse?.paymentOptionsMap}")
            setupUIFromResponse()
        }
    }

    private fun setupUIFromResponse() {
        val paymentModesStaticData = mPaymentModesResponse?.staticText
        mContentView?.let {
            val paymentModeHeadingTextView: TextView = it.findViewById(R.id.paymentModeHeadingTextView)
            val paymentModeSubHeadingTextView: TextView = it.findViewById(R.id.paymentModeSubHeadingTextView)
            val completeKycTextView: TextView = it.findViewById(R.id.completeKycTextView)
            val completeKycNowTextView: TextView = it.findViewById(R.id.completeKycNowTextView)
            val codTextView: TextView = it.findViewById(R.id.codTextView)
            val upiTextView: TextView = it.findViewById(R.id.headingTextView)
            val upiImageView: ImageView = it.findViewById(R.id.upiImageView)
            val codImageView: ImageView = it.findViewById(R.id.codImageView)
            val kycGroup: View = it.findViewById(R.id.kycGroup)
            paymentModeHeadingTextView.text = paymentModesStaticData?.page_heading_payment_mode
            paymentModeSubHeadingTextView.text = paymentModesStaticData?.page_sub_heading_payment_mode
            completeKycTextView.text = paymentModesStaticData?.message_complete_kyc_to_unlock
            completeKycNowTextView.text = paymentModesStaticData?.text_complete_kyc_now
            mPaymentModesResponse?.upi?.let { upiResponse ->
                upiTextView.text = upiResponse.name
                if (!isEmpty(upiResponse.imageUrl)) {
                    mActivity?.let { context -> Glide.with(context).load(upiResponse.imageUrl).into(upiImageView) }
                }
            }
            mPaymentModesResponse?.cod?.let { codResponse ->
                codTextView.text = codResponse.name
                if (!isEmpty(codResponse.imageUrl)) {
                    mActivity?.let { context -> Glide.with(context).load(codResponse.imageUrl).into(codImageView) }
                }
            }
            kycGroup.visibility = if (true == mPaymentModesResponse?.kycStatus?.kycStatus) View.GONE else View.VISIBLE
        }
    }

    override fun onPaymentModesServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

}