package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.BusinessTypeAdapter
import com.digitaldukaan.adapters.ProfileStatusAdapter2
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.BusinessTypeRequest
import com.digitaldukaan.models.response.BusinessTypeItemResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ProfileInfoResponse
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.digitaldukaan.services.BusinessTypeService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IBusinessTypeServiceInterface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_business_type_fragment.*

class BusinessTypeFragment : BaseFragment(), IBusinessTypeServiceInterface {

    private var mProfilePreviewResponse: ProfilePreviewSettingsKeyResponse? = null
    private var mProfileInfoResponse: ProfileInfoResponse? = null
    private var mPosition: Int = 0
    private var mIsSingleStep: Boolean = false
    private lateinit var businessTypeService: BusinessTypeService
    private var mBusinessSelectedList: ArrayList<BusinessTypeItemResponse> = ArrayList()

    companion object {
        fun newInstance(profilePreviewResponse: ProfilePreviewSettingsKeyResponse?, position: Int, isSingleStep: Boolean, profileInfoResponse: ProfileInfoResponse?): BusinessTypeFragment {
            val fragment = BusinessTypeFragment()
            fragment.mProfilePreviewResponse = profilePreviewResponse
            fragment.mPosition = position
            fragment.mIsSingleStep = isSingleStep
            fragment.mProfileInfoResponse = profileInfoResponse
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "BusinessTypeFragment"
        mContentView = inflater.inflate(R.layout.layout_business_type_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance()?.apply {
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
        if (mIsSingleStep) {
            statusRecyclerView?.visibility = View.GONE
            skipTextView?.visibility = View.GONE
        } else {
            skipTextView?.visibility = View.VISIBLE
            statusRecyclerView?.apply {
                visibility = View.VISIBLE
                layoutManager = GridLayoutManager(mActivity, mProfileInfoResponse?.mTotalSteps?.toInt() ?: 1)
                adapter = ProfileStatusAdapter2(mProfileInfoResponse?.mTotalSteps?.toInt(), mPosition)
            }
        }
        verifyTextView?.text = StaticInstances.sStaticData?.mProfileStaticData?.saveChanges
        businessTypeService = BusinessTypeService()
        businessTypeService.setServiceInterface(this)
        showProgressDialog(mActivity)
        businessTypeService.getBusinessListData()
    }

    override fun onBusinessTypeResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val listType = object : TypeToken<List<BusinessTypeItemResponse>>() {}.type
                val businessTypeValueSplitArray = mProfilePreviewResponse?.mValue?.split(",")
                mBusinessSelectedList = Gson().fromJson<ArrayList<BusinessTypeItemResponse>>(response.mCommonDataStr, listType)
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
                businessTypeRecyclerView?.apply {
                    layoutManager = GridLayoutManager(mActivity, 2)
                    adapter = BusinessTypeAdapter(this@BusinessTypeFragment, mBusinessSelectedList)
                }
            } else showToast(response.mMessage)
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            skipTextView?.id -> {
                StaticInstances.sStepsCompletedList?.run {
                    for (completedItem in this) {
                        if (completedItem.action == Constants.ACTION_DESCRIPTION) {
                            completedItem.isCompleted = true
                            break
                        }
                    }
                    switchToInCompleteProfileFragment(mProfileInfoResponse)
                }
            }
            verifyTextView?.id -> {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                businessTypeService = BusinessTypeService()
                businessTypeService.setServiceInterface(this)
                val businessTypeSelectedList:ArrayList<Int> = ArrayList()
                mBusinessSelectedList.forEachIndexed { _, itemResponse ->
                    if (itemResponse.isBusinessTypeSelected) businessTypeSelectedList.add(itemResponse.businessId)
                }
                if (businessTypeSelectedList.isEmpty()) {
                    showToast("Please select at least 1 business type")
                    return
                }
                if (businessTypeSelectedList.size > resources.getInteger(R.integer.business_type_count)) {
                    showToast("Only ${resources.getInteger(R.integer.business_type_count)} selections are allowed")
                    return
                }
                verifyTextView?.isEnabled = false
                val businessTypRequest = BusinessTypeRequest(businessTypeSelectedList)
                showProgressDialog(mActivity)
                businessTypeService.setStoreBusinesses(businessTypRequest)
            }
        }
    }

    override fun onSavingBusinessTypeResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            verifyTextView?.isEnabled = true
            if (response.mIsSuccessStatus) {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_BUSINESS_TYPE_SELECT,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)
                    )
                )
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                if (!mIsSingleStep) {
                    StaticInstances.sStepsCompletedList?.run {
                        for (completedItem in this) {
                            if (completedItem.action == Constants.ACTION_BUSINESS) {
                                completedItem.isCompleted = true
                                break
                            }
                        }
                        switchToInCompleteProfileFragment(mProfileInfoResponse)
                    }
                } else {
                    mActivity?.onBackPressed()
                }
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onBusinessTypeServerException(e: Exception) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            verifyTextView?.isEnabled = true
            exceptionHandlingForAPIResponse(e)
        }
    }

}