package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.MainFeaturesPosAdapter
import com.digitaldukaan.adapters.OtherFeaturesPosAdapter
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.BillingPosPageInfoResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.services.BillingPosService
import com.digitaldukaan.services.serviceinterface.IBillingPosServiceInterface
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_billing_pos.*

class BillingPosFragment: BaseFragment(), IBillingPosServiceInterface {

    private var mService: BillingPosService? = BillingPosService()

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
    }

    override fun onBillingPosPageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                if (response.mIsSuccessStatus) {
                    val pageInfoResponse = Gson().fromJson<BillingPosPageInfoResponse>(response.mCommonDataStr, BillingPosPageInfoResponse::class.java)
                    setupUIFromResponse(pageInfoResponse)
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
                if (response.mIsSuccessStatus) {

                }
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
                showProgressDialog(mActivity)
                mService?.requestACallBack()
            }
        }
    }

    private fun setupUIFromResponse(pageInfoResponse: BillingPosPageInfoResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val otherFeaturesRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.otherFeaturesRecyclerView)
            val mainFeaturesRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.retailSolutionRecyclerView)
            val headingTextView: TextView? = mContentView?.findViewById(R.id.headingTextView)
            val otherFeaturesTextView: TextView? = mContentView?.findViewById(R.id.otherFeaturesTextView)
            val subHeadingTextView: TextView? = mContentView?.findViewById(R.id.subHeadingTextView)
            val retailManagementSolutionTextView: TextView? = mContentView?.findViewById(R.id.retailManagementSolutionTextView)
            headingTextView?.text = pageInfoResponse.staticText?.heading_page
            subHeadingTextView?.text = pageInfoResponse.staticText?.sub_heading_page
            retailManagementSolutionTextView?.text = pageInfoResponse.staticText?.heading_main_features
            otherFeaturesTextView?.text = pageInfoResponse.staticText?.heading_other_features
            otherFeaturesRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity)
                adapter = OtherFeaturesPosAdapter(pageInfoResponse.otherFeaturesList)
            }
            mainFeaturesRecyclerView?.apply {
                layoutManager = GridLayoutManager(mActivity, 2)
                adapter = MainFeaturesPosAdapter(pageInfoResponse.mainFeaturesList, mActivity)
            }
        }
    }

    override fun onBillingPosServerException(e: Exception) = exceptionHandlingForAPIResponse(e)
}