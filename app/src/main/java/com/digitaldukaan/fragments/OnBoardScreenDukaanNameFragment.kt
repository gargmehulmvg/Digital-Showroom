package com.digitaldukaan.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.transition.TransitionInflater
import com.appsflyer.AppsFlyerLib
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.CreateStoreRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.CreateStoreResponse
import com.digitaldukaan.services.CreateStoreService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ICreateStoreServiceInterface
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_on_board_screen_dukaan_fragment.*


class OnBoardScreenDukaanNameFragment : BaseFragment(),
    ICreateStoreServiceInterface {

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
                        val service = CreateStoreService()
                        service.setServiceInterface(this)
                        val storeIdStr = PrefsManager.getStringDataFromSharedPref(Constants.USER_ID)
                        val request = CreateStoreRequest(
                            PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER),
                            if (storeIdStr.isNotEmpty()) storeIdStr.toInt() else 0,
                            dukanName,
                            Constants.APP_SECRET_KEY,
                            1,
                            StaticInstances.sAppFlyerRefMobileNumber,
                            AppsFlyerLib.getInstance().getAttributionId(mActivity),
                            StaticInstances.sCleverTapId
                        )
                        showProgressDialog(mActivity)
                        service.createStore(request)
                    }
                }
            }
        }
    }

    override fun onCreateStoreResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                val createStoreResponse = Gson().fromJson<CreateStoreResponse>(response.mCommonDataStr, CreateStoreResponse::class.java)
                PrefsManager.storeStringDataInSharedPref(Constants.STORE_ID, "${createStoreResponse.storeId}")
                Log.d("STORE_OBJECT_TEST", "$TAG onCreateStoreResponse: STORE_ID :: ${PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)}")
                PrefsManager.storeStringDataInSharedPref(Constants.STORE_NAME, "${createStoreResponse.storeInfo?.name}")
                Log.d("STORE_OBJECT_TEST", "$TAG onCreateStoreResponse: STORE_NAME :: ${PrefsManager.getStringDataFromSharedPref(Constants.STORE_NAME)}")
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_ENTER_NAME,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PHONE to PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER),
                        AFInAppEventParameterName.USER_ID to PrefsManager.getStringDataFromSharedPref(Constants.USER_ID),
                        AFInAppEventParameterName.STORE_NAME to createStoreResponse?.storeInfo?.name,
                        AFInAppEventParameterName.STORE_TYPE to AFInAppEventParameterName.STORE_TYPE_DUKAAN,
                        AFInAppEventParameterName.VERIFY_PHONE to "1",
                        AFInAppEventParameterName.REFERENCE_PHONE to ""
                    )
                )
                launchFragment(OnBoardScreenDukaanLocationFragment(), true)
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onCreateStoreServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}