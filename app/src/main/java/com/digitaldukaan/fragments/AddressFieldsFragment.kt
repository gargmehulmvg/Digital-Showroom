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
import com.digitaldukaan.adapters.AddAddressFieldAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.response.AddressFieldsPageInfoResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.services.AddressFieldsService
import com.digitaldukaan.services.serviceinterface.IAddressFieldsServiceInterface
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_common_webview_fragment.*
import kotlinx.android.synthetic.main.layout_view_as_customer.*

class AddressFieldsFragment: BaseFragment(), IAddressFieldsServiceInterface {

    private var mService: AddressFieldsService? = AddressFieldsService()
    private var mAddressFieldsPageInfoResponse: AddressFieldsPageInfoResponse? = null

    companion object {
        fun newInstance(): AddressFieldsFragment = AddressFieldsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "AddressFieldsFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_address_fields, container, false)
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
                        Log.d(TAG, "mAddressFieldsPageInfoResponse:: ${mAddressFieldsPageInfoResponse?.addressFieldsList}")
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

    override fun onAddressFieldsServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

}