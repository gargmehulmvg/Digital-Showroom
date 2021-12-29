package com.digitaldukaan.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.digitaldukaan.interfaces.IAdapterItemNotifyListener
import com.digitaldukaan.models.request.AddressFieldItemRequest
import com.digitaldukaan.models.request.AddressFieldRequest
import com.digitaldukaan.models.response.AddressFieldsPageInfoResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.services.AddressFieldsService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IAddressFieldsServiceInterface
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_address_fields.view.*
import kotlinx.android.synthetic.main.layout_common_webview_fragment.*
import kotlinx.android.synthetic.main.layout_view_as_customer.*

class AddressFieldsFragment: BaseFragment(), IAddressFieldsServiceInterface,
    SwipeRefreshLayout.OnRefreshListener {

    private var mService: AddressFieldsService? = AddressFieldsService()
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var mAddressFieldsPageInfoResponse: AddressFieldsPageInfoResponse? = null
    private var mIsItemUpdated = false
    private var saveChangesCtaTextView: View? = null

    companion object {
        fun newInstance(): AddressFieldsFragment = AddressFieldsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "AddressFieldsFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_address_fields, container, false)
        swipeRefreshLayout = mContentView?.findViewById(R.id.swipeRefreshLayout)
        saveChangesCtaTextView = mContentView?.findViewById(R.id.verifyTextView)
        swipeRefreshLayout?.setOnRefreshListener(this)
        mService?.setServiceInterface(this)
        mService?.getAddressFieldsPageInfo()
        saveChangesCtaTextView?.isEnabled = false
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
                    mIsItemUpdated = false
                    showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                    Handler(Looper.getMainLooper()).postDelayed({
                        mActivity?.onBackPressed()
                    }, Constants.TIMER_ARROW_ANIMATION)
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
                        val requestList: ArrayList<AddressFieldItemRequest?> = ArrayList()
                        var isCheckBoxChecked = false
                        mAddressFieldsPageInfoResponse?.addressFieldsList?.forEachIndexed { _, itemResponse ->
                            val item = AddressFieldItemRequest(
                                id = itemResponse?.id ?: 0,
                                isMandatory = itemResponse?.isMandatory ?: false,
                                isFieldSelected = itemResponse?.isFieldSelected ?: false
                            )
                            if (true == itemResponse?.isFieldSelected && !isCheckBoxChecked) isCheckBoxChecked = true
                            requestList.add(item)
                        }
                        val errorTextView: TextView? = mContentView?.findViewById(R.id.errorTextView)
                        if (!isCheckBoxChecked) {
                            errorTextView?.apply {
                                visibility = View.VISIBLE
                                text = mAddressFieldsPageInfoResponse?.staticText?.error_select_1_field
                            }
                            return@setOnClickListener
                        } else errorTextView?.visibility = View.GONE
                        showCancellableProgressDialog(mActivity)
                        mService?.setAddressFields(AddressFieldRequest(requestList))
                    }
                }
            }
            val recyclerView: RecyclerView? = mContentView?.findViewById(R.id.recyclerView)
            recyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity)
                adapter = AddAddressFieldAdapter(mAddressFieldsPageInfoResponse?.addressFieldsList, object : IAdapterItemNotifyListener {

                    override fun onAdapterItemNotifyListener(position: Int) {
                        CoroutineScopeUtils().runTaskOnCoroutineMain {
                            Log.d(TAG, "onAdapterItemNotifyListener: ")
                            mIsItemUpdated = true
                            saveChangesCtaTextView?.isEnabled = true
                            val item = mAddressFieldsPageInfoResponse?.addressFieldsList?.get(position)
                            val errorTextView: TextView? = mContentView?.findViewById(R.id.errorTextView)
                            if (false == item?.isFieldEnabled) {
                                errorTextView?.apply {
                                    visibility = View.VISIBLE
                                    val str = "${item.heading} ${mAddressFieldsPageInfoResponse?.staticText?.error_field_is_mandatory}"
                                    text = str
                                }
                            } else
                                errorTextView?.visibility = View.GONE
                        }
                    }

                })
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

    override fun onBackPressed(): Boolean {
        return try {
            Log.d(TAG, "onBackPressed :: called")
            if(mIsItemUpdated) {
                showGoBackDialog()
                true
            } else {
                fragmentManager?.popBackStack()
                true
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun showGoBackDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mActivity?.let { context ->
                    Dialog(context).apply {
                        setCancelable(true)
                        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        setContentView(R.layout.dialog_common_back_pressed)
                        val noTextView: TextView = findViewById(R.id.noTextView)
                        val yesTextView: TextView = findViewById(R.id.yesTextView)
                        val headingTextView: TextView = findViewById(R.id.headingTextView)
                        val messageTextView: TextView = findViewById(R.id.messageTextView)
                        yesTextView.text = mAddressFieldsPageInfoResponse?.staticText?.text_yes
                        noTextView.text = mAddressFieldsPageInfoResponse?.staticText?.text_no
                        headingTextView.text = mAddressFieldsPageInfoResponse?.staticText?.text_confirm_back
                        messageTextView.text = null
                        noTextView.setOnClickListener {
                            this.dismiss()
                        }
                        yesTextView.setOnClickListener {
                            this.dismiss()
                            mIsItemUpdated = false
                            mActivity?.onBackPressed()
                        }
                    }.show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "showGoBackDialog: ${e.message}", e)
            }
        }
    }

}