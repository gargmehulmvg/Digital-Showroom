package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.BusinessTypeAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.request.BusinessTypeRequest
import com.digitaldukaan.models.response.BusinessTypeItemResponse
import com.digitaldukaan.models.response.BusinessTypeResponse
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.digitaldukaan.models.response.StoreDescriptionResponse
import com.digitaldukaan.services.BusinessTypeService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IBusinessTypeServiceInterface
import kotlinx.android.synthetic.main.business_type_fragment.*

class BusinessTypeFragment : BaseFragment(), IBusinessTypeServiceInterface {

    private var mProfilePreviewResponse: ProfilePreviewSettingsKeyResponse? = null
    private var mPosition: Int = 0
    private var mIsSingleStep: Boolean = false
    private lateinit var businessTypeService: BusinessTypeService
    private var mBusinessSelectedList: ArrayList<BusinessTypeItemResponse> = ArrayList()

    companion object {
        fun newInstance(
            profilePreviewResponse: ProfilePreviewSettingsKeyResponse?,
            position: Int,
            isSingleStep: Boolean
        ): BusinessTypeFragment {
            val fragment = BusinessTypeFragment()
            fragment.mProfilePreviewResponse = profilePreviewResponse
            fragment.mPosition = position
            fragment.mIsSingleStep = isSingleStep
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.business_type_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().hideToolBar(mActivity, true)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            val stepStr = if (mIsSingleStep) "" else "Step $mPosition : "
            setHeaderTitle("$stepStr${mProfilePreviewResponse?.mHeadingText}")
            onBackPressed(this@BusinessTypeFragment)
            hideBackPressFromToolBar(mActivity, false)
        }
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        businessTypeService = BusinessTypeService()
        businessTypeService.setServiceInterface(this)
        showProgressDialog(mActivity)
        businessTypeService.getBusinessListData()
        verifyTextView.setOnClickListener {
            if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
                return@setOnClickListener
            }
            businessTypeService = BusinessTypeService()
            businessTypeService.setServiceInterface(this)
            val businessTypeSelectedList:ArrayList<Int> = ArrayList()
            mBusinessSelectedList.forEachIndexed { _, itemResponse ->
                if (itemResponse.isBusinessTypeSelected) businessTypeSelectedList.add(itemResponse.businessId)
            }
            if (businessTypeSelectedList.isEmpty()) {
                showToast("Please select at least 1 business type")
                return@setOnClickListener
            }
            if (businessTypeSelectedList.size > resources.getInteger(R.integer.business_type_count)) {
                showToast("Only ${resources.getInteger(R.integer.business_type_count)} selections are allowed")
                return@setOnClickListener
            }
            val businessTypRequest = BusinessTypeRequest(getStringDataFromSharedPref(Constants.STORE_ID).toInt(), businessTypeSelectedList)
            showProgressDialog(mActivity)
            businessTypeService.setStoreBusinesses(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), businessTypRequest)
        }
    }

    override fun onBusinessTypeResponse(response: BusinessTypeResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val businessTypeValueSplitArray = mProfilePreviewResponse?.mValue?.split(",")
                mBusinessSelectedList = response.mBusinessList
                businessTypeValueSplitArray?.run {
                    if (isNotEmpty()) {
                        mBusinessSelectedList.forEachIndexed { _, itemResponse ->
                            forEachIndexed { _, value ->
                                if (itemResponse.businessName.trim() == value.trim()) {
                                    itemResponse.isBusinessTypeSelected = true
                                    return@forEachIndexed
                                }
                            }
                        }

                    }
                }
                businessTypeRecyclerView.apply {
                    layoutManager = GridLayoutManager(mActivity, 2)
                    adapter = BusinessTypeAdapter(mActivity, mBusinessSelectedList)
                }
            } else showToast(response.mMessage)
        }
    }

    override fun onSavingBusinessTypeResponse(response: StoreDescriptionResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                mActivity.onBackPressed()
            } else showToast(response.mMessage)
        }
    }

    override fun onBusinessTypeServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}