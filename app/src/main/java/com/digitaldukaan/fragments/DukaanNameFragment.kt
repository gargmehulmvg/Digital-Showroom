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
import com.digitaldukaan.models.response.BusinessNameStaticText
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.CreateStoreResponse
import com.digitaldukaan.services.CreateStoreService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ICreateStoreServiceInterface
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_dukaan_name_fragment.*


class DukaanNameFragment : BaseFragment(), ICreateStoreServiceInterface {

    private var mDukaanNameStaticData: BusinessNameStaticText? = null

    companion object {
        private val TAG = DukaanNameFragment::class.simpleName

        fun newInstance(): DukaanNameFragment = DukaanNameFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_dukaan_name_fragment, container, false)
        mDukaanNameStaticData = StaticInstances.sStaticData?.mBusinessNameStaticText
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dukaanNameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
             val str = s.toString()
             nextTextView?.isEnabled = str.isNotBlank()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(TAG, "beforeTextChanged: ")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: ")
            }
        })
        setupUIFromStaticData()
    }

    private fun setupUIFromStaticData() {
        enterDukaanNameHeading?.text = mDukaanNameStaticData?.heading_page
        subHeadingTextView?.text = mDukaanNameStaticData?.sub_heading_page
        dukaanNameEditText?.hint = mDukaanNameStaticData?.hint_enter_here
        nextTextView?.text = mDukaanNameStaticData?.cta_text
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            nextTextView?.id -> {
                val dukanName = dukaanNameEditText?.text?.trim().toString()
                if (dukanName.isEmpty()) {
                    dukaanNameEditText?.apply {
                        requestFocus()
                        showKeyboard()
                        error = getString(R.string.mandatory_field_message)
                    }
                } else {
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                    } else {
                        val service = CreateStoreService()
                        service.setServiceInterface(this)
                        val storeIdStr = PrefsManager.getStringDataFromSharedPref(Constants.USER_ID)
                        val request = CreateStoreRequest(
                            phone = PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER),
                            userId = if (storeIdStr.isNotEmpty()) storeIdStr.toInt() else 0,
                            storeName = dukanName,
                            secretKey = Constants.APP_SECRET_KEY,
                            languageId = 1,
                            referencePhone = StaticInstances.sAppFlyerRefMobileNumber,
                            appsflyerId = AppsFlyerLib.getInstance().getAttributionId(mActivity),
                            cleverTapId = StaticInstances.sCleverTapId,
                            latitude = 0.0,
                            longitude = 0.0
                        )
                        showProgressDialog(mActivity)
                        service.createStore(request)
                    }
                }
            }
        }
    }

    override fun onCreateStoreResponse(commonApiResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonApiResponse.mIsSuccessStatus) {
                showShortSnackBar(commonApiResponse.mMessage, true, R.drawable.ic_check_circle)
                val createStoreResponse = Gson().fromJson<CreateStoreResponse>(commonApiResponse.mCommonDataStr, CreateStoreResponse::class.java)
                PrefsManager.storeStringDataInSharedPref(Constants.STORE_ID, "${createStoreResponse.storeId}")
                PrefsManager.storeStringDataInSharedPref(Constants.STORE_NAME, "${createStoreResponse.storeInfo?.name}")
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_ENTER_NAME,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID          to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PHONE             to PrefsManager.getStringDataFromSharedPref(Constants.USER_MOBILE_NUMBER),
                        AFInAppEventParameterName.USER_ID           to PrefsManager.getStringDataFromSharedPref(Constants.USER_ID),
                        AFInAppEventParameterName.STORE_NAME        to createStoreResponse?.storeInfo?.name,
                        AFInAppEventParameterName.STORE_TYPE        to AFInAppEventParameterName.STORE_TYPE_DUKAAN,
                        AFInAppEventParameterName.VERIFY_PHONE      to "1",
                        AFInAppEventParameterName.REFERENCE_PHONE   to ""
                    )
                )
                launchFragment(CreateStoreFragment.newInstance(), true)
            } else showShortSnackBar(commonApiResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onCreateStoreServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}