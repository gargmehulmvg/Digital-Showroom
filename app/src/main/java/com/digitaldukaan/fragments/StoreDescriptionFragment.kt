package com.digitaldukaan.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ProfileStatusAdapter2
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.request.StoreDescriptionRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ProfileInfoResponse
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.digitaldukaan.models.response.ProfileStaticData
import com.digitaldukaan.services.StoreDescriptionService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IStoreDescriptionServiceInterface
import kotlinx.android.synthetic.main.store_description_fragment.*

class StoreDescriptionFragment : BaseFragment(), IStoreDescriptionServiceInterface {

    private var mProfilePreviewResponse: ProfilePreviewSettingsKeyResponse? = null
    private var mProfileInfoResponse: ProfileInfoResponse? = null
    private var mPosition: Int = 0
    private var mIsSingleStep: Boolean = false
    private var mStoreDescriptionStaticData: ProfileStaticData? = null

    companion object {
        fun newInstance(profilePreviewResponse: ProfilePreviewSettingsKeyResponse?, position: Int, isSingleStep: Boolean, profileInfoResponse: ProfileInfoResponse?): StoreDescriptionFragment {
            val fragment = StoreDescriptionFragment()
            fragment.mProfilePreviewResponse = profilePreviewResponse
            fragment.mPosition = position
            fragment.mIsSingleStep = isSingleStep
            fragment.mProfileInfoResponse = profileInfoResponse
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "StoreDescriptionFragment"
        mContentView = inflater.inflate(R.layout.store_description_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mStoreDescriptionStaticData = StaticInstances.sStaticData?.mProfileStaticData
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            val stepStr = if (mIsSingleStep) "" else "Step $mPosition : "
            setHeaderTitle("$stepStr${mProfilePreviewResponse?.mHeadingText}")
            onBackPressed(this@StoreDescriptionFragment)
            hideBackPressFromToolBar(mActivity, false)
        }
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        storeDescriptionEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                Log.d(TAG, "afterTextChanged: BEFORE TRIM :: ${s.toString()}")
                val str = s.toString().trim()
                Log.d(TAG, "afterTextChanged: AFTER TRIM :: $str")
                continueTextView.isEnabled = str.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(TAG, "beforeTextChanged: ")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: ")
            }

        })
        storeDescriptionEditText?.setText(mProfilePreviewResponse?.mValue)
        storeDescriptionEditText?.hint = mStoreDescriptionStaticData?.storeDescriptionHint
        continueTextView?.text = mStoreDescriptionStaticData?.saveChanges
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
    }

    override fun onClick(view: View?) {
        when (view?.id) {
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
            continueTextView?.id -> {
                val service = StoreDescriptionService()
                service.setServiceInterface(this)
                val request = StoreDescriptionRequest(storeDescriptionEditText.text.trim().toString())
                showCancellableProgressDialog(mActivity)
                service.saveStoreDescriptionData(request)
            }
        }
    }

    override fun onStoreDescriptionResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                if (!mIsSingleStep) {
                    StaticInstances.sStepsCompletedList?.run {
                        for (completedItem in this) {
                            if (completedItem.action == Constants.ACTION_DESCRIPTION) {
                                completedItem.isCompleted = true
                                break
                            }
                        }
                        switchToInCompleteProfileFragment(mProfileInfoResponse)
                    }
                } else {
                    mActivity?.onBackPressed()
                }
            } else showToast(response.mMessage)
        }
    }

    override fun onStoreDescriptionServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}