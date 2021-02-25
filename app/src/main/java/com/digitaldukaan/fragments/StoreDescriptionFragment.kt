package com.digitaldukaan.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.request.StoreDescriptionRequest
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.digitaldukaan.models.response.StoreDescriptionResponse
import com.digitaldukaan.services.StoreDescriptionService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IStoreDescriptionServiceInterface
import kotlinx.android.synthetic.main.store_description_fragment.*

class StoreDescriptionFragment : BaseFragment(), IStoreDescriptionServiceInterface {

    private lateinit var mProfilePreviewResponse: ProfilePreviewSettingsKeyResponse
    private var mPosition: Int = 0
    private val mStoreDescriptionStaticData = mStaticData.mStaticData.mProfileStaticData

    companion object {
        fun newInstance(
            profilePreviewResponse: ProfilePreviewSettingsKeyResponse,
            position: Int
        ): StoreDescriptionFragment {
            val fragment = StoreDescriptionFragment()
            fragment.mProfilePreviewResponse = profilePreviewResponse
            fragment.mPosition = position
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.store_description_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            setHeaderTitle("")
            onBackPressed(this@StoreDescriptionFragment)
            hideBackPressFromToolBar(mActivity, false)
        }
        storeDescriptionHeading.text = "Step $mPosition : ${mProfilePreviewResponse.mHeadingText}"
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        val service = StoreDescriptionService()
        service.setServiceInterface(this)
        storeDescriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val str = s.toString()
                continueTextView.isEnabled = str.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        storeDescriptionEditText.setText(mProfilePreviewResponse.mValue)
        storeDescriptionEditText.hint = mStoreDescriptionStaticData.storeDescriptionHint
        continueTextView.text = mStoreDescriptionStaticData.saveChanges
        continueTextView.setOnClickListener {
            val description = storeDescriptionEditText.text.trim().toString()
            val request = StoreDescriptionRequest(getStringDataFromSharedPref(Constants.STORE_ID).toInt(), description)
            showCancellableProgressDialog(mActivity)
            service.saveStoreDescriptionData(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
        }
    }

    override fun onStoreDescriptionResponse(response: StoreDescriptionResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                mActivity.onBackPressed()
            } else showToast(response.mMessage)
        }
    }

    override fun onStoreDescriptionServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}