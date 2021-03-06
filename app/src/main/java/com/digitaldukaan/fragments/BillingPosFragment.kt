package com.digitaldukaan.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.adapters.MainFeaturesPosAdapter
import com.digitaldukaan.adapters.OtherFeaturesPosAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.response.BillingPosPageInfoResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.services.BillingPosService
import com.digitaldukaan.services.serviceinterface.IBillingPosServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_billing_pos.*

class BillingPosFragment: BaseFragment(), IBillingPosServiceInterface {

    private var mService: BillingPosService? = BillingPosService()
    private var mPageInfoResponse: BillingPosPageInfoResponse? = null

    companion object {
        fun newInstance(): BillingPosFragment = BillingPosFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "BillingPosFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_billing_pos, container, false)
        mService?.setServiceInterface(this)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.hideToolBar(mActivity, true)
        hideBottomNavigationView(true)
        showProgressDialog(mActivity)
        mService?.getAddressFieldsPageInfo()
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_BILLING_POS, isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
    }

    override fun onBillingPosPageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                if (response.mIsSuccessStatus) {
                    mPageInfoResponse = Gson().fromJson<BillingPosPageInfoResponse>(response.mCommonDataStr, BillingPosPageInfoResponse::class.java)
                    setupUIFromResponse()
                }
            } catch (e: Exception) {
                Log.e(TAG, "onBillingPosPageInfoResponse: ${e.message}", e)
            }
        }
        stopProgress()
    }

    override fun onRequestCallBackResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                if (response.mIsSuccessStatus) showRequestCallSuccessDialog() else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
            } catch (e: Exception) {
                Log.e(TAG, "onRequestCallBackResponse: ${e.message}", e)
            }
        }
        stopProgress()
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            backButtonToolbar?.id -> mActivity?.onBackPressed()
            requestCallbackContainer?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_BILLING_POS_CALLBACK, isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                showRequestCallBackConfirmationBottomSheet()
            }
        }
    }

    private fun showRequestCallBackConfirmationBottomSheet() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.let { context ->
                val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_request_callback_confirmation, context.findViewById(R.id.bottomSheetContainer))
                bottomSheetDialog.apply {
                    setContentView(view)
                    val headingTextView: TextView = view.findViewById(R.id.headingTextView)
                    val noTextView: TextView = view.findViewById(R.id.noTextView)
                    val yesTextView: TextView = view.findViewById(R.id.yesTextView)
                    headingTextView.text = mPageInfoResponse?.staticText?.prompt_heading
                    noTextView.text = mPageInfoResponse?.staticText?.text_no
                    yesTextView.text = mPageInfoResponse?.staticText?.text_yes
                    yesTextView.setOnClickListener {
                        bottomSheetDialog.dismiss()
                        showProgressDialog(mActivity)
                        mService?.requestACallBack(true)
                    }
                    noTextView.setOnClickListener {
                        bottomSheetDialog.dismiss()
                        showProgressDialog(mActivity)
                        mService?.requestACallBack(false)
                    }
                }.show()
            }
        }
    }

    private fun setupUIFromResponse() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val otherFeaturesRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.otherFeaturesRecyclerView)
            val mainFeaturesRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.retailSolutionRecyclerView)
            val ctaTextView: TextView? = mContentView?.findViewById(R.id.addProductTextView)
            val headingTextView: TextView? = mContentView?.findViewById(R.id.headingTextView)
            val upperImageView: ImageView? = mContentView?.findViewById(R.id.upperImageView)
            val addProductImageView: ImageView? = mContentView?.findViewById(R.id.addProductImageView)
            val imageViewBackground: ImageView? = mContentView?.findViewById(R.id.imageViewBackground)
            val otherFeaturesTextView: TextView? = mContentView?.findViewById(R.id.otherFeaturesTextView)
            val subHeadingTextView: TextView? = mContentView?.findViewById(R.id.subHeadingTextView)
            val retailManagementSolutionTextView: TextView? = mContentView?.findViewById(R.id.retailManagementSolutionTextView)
            headingTextView?.text = mPageInfoResponse?.staticText?.heading_page
            subHeadingTextView?.text = mPageInfoResponse?.staticText?.sub_heading_page
            retailManagementSolutionTextView?.text = mPageInfoResponse?.staticText?.heading_main_features
            otherFeaturesTextView?.text = mPageInfoResponse?.staticText?.heading_other_features
            mActivity?.let { context ->
                if (isNotEmpty(mPageInfoResponse?.cdnHero)) {
                    upperImageView?.let { view -> Glide.with(context).load(mPageInfoResponse?.cdnHero).into(view) }
                }
                if (isNotEmpty(mPageInfoResponse?.cdnBackground)) {
                    imageViewBackground?.let { view -> Glide.with(context).load(mPageInfoResponse?.cdnBackground).into(view) }
                }
                if (isNotEmpty(mPageInfoResponse?.cta?.cdn)) {
                    addProductImageView?.let { view -> Glide.with(context).load(mPageInfoResponse?.cta?.cdn).into(view) }
                } else addProductImageView?.visibility = View.GONE
                ctaTextView?.text = mPageInfoResponse?.cta?.text
            }
            otherFeaturesRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity)
                adapter = OtherFeaturesPosAdapter(mPageInfoResponse?.otherFeaturesList)
            }
            mainFeaturesRecyclerView?.apply {
                layoutManager = GridLayoutManager(mActivity, 2)
                adapter = MainFeaturesPosAdapter(mPageInfoResponse?.mainFeaturesList, mActivity)
            }
        }
    }

    private fun showRequestCallSuccessDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.let {
                val view = LayoutInflater.from(it).inflate(R.layout.request_call_success_dialog, null)
                val successDialog = Dialog(it)
                successDialog.apply {
                    setContentView(view)
                    setCancelable(true)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    view?.run {
                        val headingTextView: TextView = findViewById(R.id.headingTextView)
                        val subHeadingTextView: TextView = findViewById(R.id.subHeadingTextView)
                        val ctaTextView: TextView = findViewById(R.id.ctaTextView)
                        headingTextView.text = mPageInfoResponse?.staticText?.message_callback_success
                        subHeadingTextView.text = mPageInfoResponse?.staticText?.sub_message_instruction
                        ctaTextView.apply {
                            text = mPageInfoResponse?.staticText?.text_ok
                            setOnClickListener { successDialog.dismiss() }
                        }
                    }
                }.show()
            }
        }
    }

    override fun onBillingPosServerException(e: Exception) = exceptionHandlingForAPIResponse(e)
}