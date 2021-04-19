package com.digitaldukaan.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.transition.TransitionInflater
import com.digitaldukaan.R
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.models.request.StoreNameRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.StoreDescriptionResponse
import com.digitaldukaan.services.ProfilePreviewService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProfilePreviewServiceInterface
import kotlinx.android.synthetic.main.layout_on_board_screen_dukaan_fragment.*


class OnBoardScreenDukaanNameFragment : BaseFragment(), IProfilePreviewServiceInterface {

    private val mDukaanNameStaticData = mStaticData.mStaticData.mOnBoardStep1StaticData

    companion object {
        private val TAG = OnBoardScreenDukaanNameFragment::class.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_on_board_screen_dukaan_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dukaanNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
             val str = s.toString()
             nextTextView.isEnabled = str.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        setupUIFromStaticData()
    }

    private fun setupUIFromStaticData() {
        stepOneTextView.text = mDukaanNameStaticData.mStepCount
        enterDukaanNameHeading.text = mDukaanNameStaticData.mDukaanName
        dukaanNameEditText.hint = mDukaanNameStaticData.mTitleHinText
        nextTextView.text = mDukaanNameStaticData.mNextButton
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            backImageView.id -> {
                mActivity.onBackPressed()
            }
            nextTextView.id -> {
                val dukanName = dukaanNameEditText.text.trim().toString()
                if (dukanName.isEmpty()) {
                    dukaanNameEditText.requestFocus()
                    dukaanNameEditText.showKeyboard()
                    dukaanNameEditText.error = getString(R.string.mandatory_field_message)
                } else {
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                    } else {
                        val service = ProfilePreviewService()
                        service.setServiceInterface(this)
                        val request = StoreNameRequest(dukanName)
                        showProgressDialog(mActivity)
                        service.updateStoreName(request)
                    }
                }
            }
        }
    }

    override fun onProfilePreviewResponse(commonApiResponse: CommonApiResponse) {
        Log.d(TAG, "onProfilePreviewResponse: do nothing")
    }

    override fun onStoreNameResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                launchFragment(OnBoardScreenDukaanLocationFragment(), true)
            } else showToast(response.mMessage)
        }
    }

    override fun onStoreLinkResponse(response: StoreDescriptionResponse) {
        Log.d(TAG, "onStoreLinkResponse: do nothing")
    }

    override fun onStoreLogoResponse(response: CommonApiResponse) {
        Log.d(TAG, "onStoreLogoResponse: do nothing")
    }

    override fun onImageCDNLinkGenerateResponse(response: CommonApiResponse) {
        Log.d(TAG, "onImageCDNLinkGenerateResponse: do nothing")
    }

    override fun onInitiateKycResponse(response: CommonApiResponse) {
        Log.d(TAG, "onInitiateKycResponse: do nothing")
    }

    override fun onProfilePreviewServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}