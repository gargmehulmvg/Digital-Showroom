package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.BusinessTypeAdapter
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.BusinessTypeResponse
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.digitaldukaan.services.BusinessTypeService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IBusinessTypeServiceInterface
import kotlinx.android.synthetic.main.business_type_fragment.*

class BusinessTypeFragment : BaseFragment(), IBusinessTypeServiceInterface {

    private lateinit var mProfilePreviewResponse: ProfilePreviewSettingsKeyResponse
    private var mPosition: Int = 0
    private var mIsSingleStep: Boolean = false

    companion object {
        fun newInstance(
            profilePreviewResponse: ProfilePreviewSettingsKeyResponse,
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
            setHeaderTitle("$stepStr${mProfilePreviewResponse.mHeadingText}")
            onBackPressed(this@BusinessTypeFragment)
            hideBackPressFromToolBar(mActivity, false)
        }
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        val service = BusinessTypeService()
        service.setServiceInterface(this)
        showProgressDialog(mActivity)
        service.getBusinessListData()
    }

    override fun onBusinessTypeResponse(response: BusinessTypeResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                businessTypeRecyclerView.apply {
                    layoutManager = GridLayoutManager(mActivity, 2)
                    adapter = BusinessTypeAdapter(mActivity, response.mBusinessList)
                }
            } else showToast(response.mMessage)
        }
    }

    override fun onBusinessTypeServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}