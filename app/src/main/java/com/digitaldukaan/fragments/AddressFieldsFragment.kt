package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.digitaldukaan.R
import com.digitaldukaan.adapters.AddAddressFieldAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.AddressFieldItemRequest
import com.digitaldukaan.models.request.AddressFieldRequest
import com.digitaldukaan.models.response.AddressFieldsPageInfoResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.services.AddressFieldsService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IAddressFieldsServiceInterface
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_common_webview_fragment.*
import kotlinx.android.synthetic.main.layout_view_as_customer.*

class AddressFieldsFragment: BaseFragment(), IAddressFieldsServiceInterface,
    SwipeRefreshLayout.OnRefreshListener {

    private var mService: AddressFieldsService? = AddressFieldsService()
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAddressFieldsPageInfoResponse: AddressFieldsPageInfoResponse? = null

    companion object {
        fun newInstance(): AddressFieldsFragment = AddressFieldsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "AddressFieldsFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_address_fields, container, false)
        swipeRefreshLayout = mContentView?.findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout?.setOnRefreshListener(this)
        mService?.setServiceInterface(this)
        mService?.getAddressFieldsPageInfo()
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            headerTitle = null
            onBackPressed(this@AddressFieldsFragment)
            setSideIconVisibility(false)
        }
    }

    override fun onAddressFieldsPageInfoResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                if (response.mIsSuccessStatus) {
                    mAddressFieldsPageInfoResponse = Gson().fromJson<AddressFieldsPageInfoResponse>(response.mCommonDataStr, AddressFieldsPageInfoResponse::class.java)
                    setupUIFromResponse()
                }
                if (true == swipeRefreshLayout?.isRefreshing) swipeRefreshLayout?.isRefreshing = false
            } catch (e: Exception) {
                Log.e(TAG, "onAddressFieldsPageInfoResponse: ${e.message}", e)
            }
        }
    }

    override fun onAddressFieldsChangedResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                if (response.mIsSuccessStatus) {
                    showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                    onRefresh()
                } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
            } catch (e: Exception) {
                Log.e(TAG, "onAddressFieldsPageInfoResponse: ${e.message}", e)
            }
        }
    }

    private fun setupUIFromResponse() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mAddressFieldsPageInfoResponse?.staticText?.let { staticText ->
                val verifyTextView: TextView? = mContentView?.findViewById(R.id.verifyTextView)
                ToolBarManager.getInstance().headerTitle = staticText.heading_page
                verifyTextView?.apply {
                    text = staticText.text_save_changes
                    setOnClickListener {
                        if (!isInternetConnectionAvailable(mActivity)) return@setOnClickListener
                        showCancellableProgressDialog(mActivity)
                        val requestList: ArrayList<AddressFieldItemRequest?> = ArrayList()
                        mAddressFieldsPageInfoResponse?.addressFieldsList?.forEachIndexed { _, itemResponse ->
                            val item = AddressFieldItemRequest(
                                id = itemResponse?.id ?: 0,
                                isMandatory = itemResponse?.isMandatory ?: false,
                                isFieldSelected = itemResponse?.isFieldSelected ?: false
                            )
                            requestList.add(item)
                        }
                        mService?.setAddressFields(AddressFieldRequest(requestList))
                    }
                }
            }
            val recyclerView: RecyclerView? = mContentView?.findViewById(R.id.recyclerView)
            recyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity)
                adapter = AddAddressFieldAdapter(mAddressFieldsPageInfoResponse?.addressFieldsList)
            }
        }
    }

    override fun onAddressFieldsServerException(e: Exception) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (true == swipeRefreshLayout?.isRefreshing) swipeRefreshLayout?.isRefreshing = false
        }
        exceptionHandlingForAPIResponse(e)
    }

    override fun onRefresh() {
        mService?.getAddressFieldsPageInfo()
    }

}