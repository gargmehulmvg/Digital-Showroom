package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.PrepaidOrderWorkFlowAdapter
import com.digitaldukaan.adapters.SetOrderTypeAdapter
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.AccountStaticTextResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.SetOrderTypePageInfoResponse
import com.digitaldukaan.models.response.SetOrderTypePageStaticTextResponse
import com.digitaldukaan.services.SetOrderTypeService
import com.digitaldukaan.services.serviceinterface.ISetOrderTypeServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_set_order_type_fragment.*

class SetOrderTypeFragment: BaseFragment(), ISetOrderTypeServiceInterface {

    private var mMoreControlsStaticData: AccountStaticTextResponse? = null
    private var prepaidOrderContainer: View? = null
    private var payOnPickUpDeliveryContainer: View? = null
    private var payBothContainer: View? = null
    private var mService: SetOrderTypeService? = null
    private var mSetOrderTypePageInfoResponse: SetOrderTypePageInfoResponse? = null
    private var mStaticText: SetOrderTypePageStaticTextResponse? = null

    companion object {
        private const val TAG = "SetOrderTypeFragment"
        fun newInstance(moreControlsStaticData: AccountStaticTextResponse?): SetOrderTypeFragment{
            val fragment = SetOrderTypeFragment()
            fragment.mMoreControlsStaticData = moreControlsStaticData
            return fragment
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
        prepaidOrderContainer = mContentView?.findViewById(R.id.prepaidOrderContainer)
        payOnPickUpDeliveryContainer = mContentView?.findViewById(R.id.payOnPickUpDeliveryContainer)
        payBothContainer = mContentView?.findViewById(R.id.payBothContainer)
        val prepaidOrderTypeRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.prepaidOrderTypeRecyclerView)
        prepaidOrderTypeRecyclerView?.apply {
            layoutManager = LinearLayoutManager(mActivity)
            adapter = SetOrderTypeAdapter(mActivity)
        }
        val bothOrderTypeRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.bothOrderTypeRecyclerView)
        bothOrderTypeRecyclerView?.apply {
            layoutManager = LinearLayoutManager(mActivity)
            adapter = SetOrderTypeAdapter(mActivity)
        }
    }

    private fun setupUIWithStaticData() {
        val howDoesPrepaidWorkTextView: TextView? = mContentView?.findViewById(R.id.howDoesPrepaidWorkTextView)
        howDoesPrepaidWorkTextView?.text = mStaticText?.heading_how_does_prepaid_orders_works
        ToolBarManager.getInstance()?.setHeaderTitle(mStaticText?.heading_set_orders_type)
    }

    override fun onSetOrderTypeResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                stopProgress()
                if (response.mIsSuccessStatus) {
                    mSetOrderTypePageInfoResponse = Gson().fromJson(response.mCommonDataStr, SetOrderTypePageInfoResponse::class.java)
                    mSetOrderTypePageInfoResponse?.let {
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
            howDoesPrepaidWorkTextView?.id -> {
                showPrepaidOrderWorkFlowBottomSheet()
            }
        }
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

}