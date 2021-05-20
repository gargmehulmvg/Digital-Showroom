package com.digitaldukaan.fragments

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.network.RetrofitApi
import com.google.gson.Gson
import io.sentry.Sentry
import kotlinx.android.synthetic.main.layout_send_bill.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class SendBillFragment : BaseFragment() {

    private var mImageUri: Uri? = null
    private var mAmountStr: String? = ""

    companion object {
        private const val TAG = "SendBillFragment"
        fun newInstance(uri: Uri?): SendBillFragment {
            val fragment = SendBillFragment()
            fragment.mImageUri = uri
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_send_bill, container, false)
        hideBottomNavigationView(true)
        ToolBarManager.getInstance()?.apply { hideToolBar(mActivity, true) }
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadImageFromUri()
        sendBillEditText?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(str: Editable?) {
                mAmountStr = str?.toString()
                sendBillTextView.isEnabled = str?.toString()?.isEmpty() != true
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "beforeTextChanged: do nothing")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Log.d(TAG, "onTextChanged: do nothing")
            }

        })
    }

    private fun loadImageFromUri() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mImageUri?.let {
                val bitmap = getBitmapFromUri(mImageUri, mActivity)
                imageView?.setImageBitmap(bitmap)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            refreshImageView?.id -> openFullCamera()
            backButtonToolbar?.id -> mActivity?.onBackPressed()
            sendBillTextView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_GENERATE_SELF_BILL,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)
                    )
                )
                try {
                    val imageFile = File(mImageUri?.path)
                    imageFile.run {
                        val fileRequestBody = MultipartBody.Part.createFormData("media", name, RequestBody.create("image/*".toMediaTypeOrNull(), this))
                        val imageTypeRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), Constants.BASE64_ORDER_BILL)
                        showProgressDialog(mActivity)
                        CoroutineScopeUtils().runTaskOnCoroutineBackground {
                            try {
                                val response = RetrofitApi().getServerCallObject()?.getImageUploadCdnLink(imageTypeRequestBody, fileRequestBody)
                                response?.let {
                                    CoroutineScopeUtils().runTaskOnCoroutineMain {
                                        stopProgress()
                                        if (response.isSuccessful) {
                                            val base64Str = Gson().fromJson<String>(response.body()?.mCommonDataStr, String::class.java)
                                            launchFragment(
                                                CommonWebViewFragment().newInstance(
                                                    "",
                                                    "${BuildConfig.WEB_VIEW_URL}${WebViewUrls.WEB_VIEW_BILL_CONFIRM}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID
                                                    )}&imageURL=$base64Str&amount=${if (mAmountStr?.isEmpty() == true) 0.0 else mAmountStr?.toDouble()}"
                                                ), true
                                            )
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                Sentry.captureException(e, "$TAG onClick: exception")
                                exceptionHandlingForAPIResponse(e)
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "sendBillTextView: ${e.message}", e)
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                        data = mapOf(
                            AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                            "Exception Point" to "sendBillTextView Click for Image Conversion",
                            "Exception Message" to e.message,
                            "Exception Logs" to e.toString()
                        )
                    )
                }
            }
        }
    }

    override fun onImageSelectionResultUri(fileUri: Uri?) {
        mImageUri = fileUri
        loadImageFromUri()
    }

}