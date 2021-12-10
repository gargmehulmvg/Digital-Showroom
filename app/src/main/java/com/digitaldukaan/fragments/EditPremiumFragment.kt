package com.digitaldukaan.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.EditPremiumColorAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.models.response.*
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.EditPremiumService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IEditPremiumServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_edit_premium_fragment.*

class EditPremiumFragment : BaseFragment(), IEditPremiumServiceInterface {

    private var mStaticText: PremiumPageInfoStaticTextResponse? = null
    private var mPremiumPageInfoResponse: PremiumPageInfoResponse? = null
    private var mEditPremiumColorList: ArrayList<EditPremiumColorItemResponse?>? = null
    private var mSelectedColorId = 0
    private var mShareDataOverWhatsAppText = ""
    private var mDefaultSelectedColorItem: EditPremiumColorItemResponse? = null

    companion object {
        private val mService: EditPremiumService = EditPremiumService()
        private var colorAdapter: EditPremiumColorAdapter? = null

        fun newInstance(staticText: PremiumPageInfoStaticTextResponse?, premiumPageInfoResponse: PremiumPageInfoResponse?): EditPremiumFragment{
            val fragment = EditPremiumFragment()
            fragment.mStaticText = staticText
            fragment.mPremiumPageInfoResponse = premiumPageInfoResponse
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_EDIT_THEME,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "EditPremiumFragment"
        mService.setServiceInterface(this)
        mEditPremiumColorList = ArrayList()
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_edit_premium_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, true)
        }
        WebViewBridge.mWebViewListener = this
        hideBottomNavigationView(true)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        editPremiumWebView?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(WebViewBridge(), Constants.KEY_ANDROID)
            val url = "${BuildConfig.WEB_VIEW_PREVIEW_URL}${mPremiumPageInfoResponse?.domain}"
            Log.d(EditPremiumFragment::class.simpleName, "onViewCreated: $url")
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Log.d(TAG, "onPageFinished: called")
                    stopProgress()
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                    return onCommonWebViewShouldOverrideUrlLoading(url, view)
                }
            }
            loadUrl(url)
        }
        mStaticText?.let { text ->
            appTitleTextView?.text = text.heading_edit_theme
            textView3?.text = text.text_customer_will_see
            editColorTextView?.text = text.text_edit_colors
            changeImageTextView?.text = text.text_edit_image
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            backButtonToolbar?.id -> mActivity?.onBackPressed()
            editColorContainer?.id -> {
                if (null == mEditPremiumColorList || true == mEditPremiumColorList?.isEmpty()) {
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return
                    }
                    showProgressDialog(mActivity)
                    mService.getPremiumColors()
                } else showColorBottomSheet()
            }
            changeImageContainer?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_EDIT_THEME_IMAGE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                )
                openMobileGalleryWithoutCrop()
            }
            viewWebsiteImageView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_VIEW_AS_CUSTOMER,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.CHANNEL to "isEditor")
                )
                val url = "${BuildConfig.WEB_VIEW_PREVIEW_URL}${mPremiumPageInfoResponse?.domain}"
                openUrlInBrowser(url)
            }
            whatsAppImageView?.id -> {
                if (StaticInstances.sIsShareStoreLocked) {
                    getLockedStoreShareDataServerCall(Constants.MODE_SHARE_STORE)
                    return
                }
                if (isNotEmpty(mShareDataOverWhatsAppText)) {
                    shareOnWhatsApp(mShareDataOverWhatsAppText)
                } else if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                } else {
                    showProgressDialog(mActivity)
                    CoroutineScopeUtils().runTaskOnCoroutineBackground {
                        try {
                            val response = RetrofitApi().getServerCallObject()?.getProductShareStoreData()
                            CoroutineScopeUtils().runTaskOnCoroutineMain {
                                val commonResponse = response?.body()
                                stopProgress()
                                mShareDataOverWhatsAppText = Gson().fromJson<String>(commonResponse?.mCommonDataStr, String::class.java)
                                shareOnWhatsApp(mShareDataOverWhatsAppText)
                            }
                        } catch (e: Exception) {
                            exceptionHandlingForAPIResponse(e)
                        }
                    }
                }
            }
        }
    }

    override fun onImageSelectionResultUri(fileUri: Uri?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            launchFragment(EditPhotoFragment.newInstance(fileUri, Constants.EDIT_PHOTO_MODE_MOBILE, mStaticText, mPremiumPageInfoResponse), true)
        }
    }

    private fun showColorBottomSheet() {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_EDIT_COLOR,
            isCleverTapEvent = true, isAppFlyerEvent = false, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
        mActivity?.run {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_edit_premium_color, this.findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
                view.run {
                    val colorRecyclerView: RecyclerView = findViewById(R.id.colorRecyclerView)
                    val saveTextView: TextView = findViewById(R.id.saveTextView)
                    saveTextView.text = mStaticText?.text_save_changes
                    colorRecyclerView.apply {
                        layoutManager = GridLayoutManager(mActivity, 5)
                        mEditPremiumColorList?.forEachIndexed { _, colorItemResponse -> colorItemResponse?.isSelected = false }
                        mEditPremiumColorList?.set(0, mDefaultSelectedColorItem)
                        colorAdapter = EditPremiumColorAdapter(mEditPremiumColorList, object : IAdapterItemClickListener {

                            override fun onAdapterItemClickListener(position: Int) {
                                if (position == 0) return
                                mEditPremiumColorList?.forEachIndexed { _, colorItemResponse -> colorItemResponse?.isSelected = false }
                                mEditPremiumColorList?.get(position)?.apply {
                                    isSelected = true
                                    mSelectedColorId = id
                                }
                                colorAdapter?.setEditPremiumColorList(mEditPremiumColorList)
                            }

                        }, mStaticText?.text_theme_color)
                        adapter = colorAdapter
                    }
                    saveTextView.setOnClickListener {
                        bottomSheetDialog.dismiss()
                        if (!isInternetConnectionAvailable(mActivity)) {
                            showNoInternetConnectionDialog()
                            return@setOnClickListener
                        }
                        showProgressDialog(mActivity)
                        mEditPremiumColorList?.forEachIndexed { _, itemResponse ->
                            if (itemResponse?.id == mSelectedColorId) {
                                mDefaultSelectedColorItem = itemResponse
                                return@forEachIndexed
                            }
                        }
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_SAVE_THEME_COLOR,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                        )
                        mService.setStoreThemeColorPalette(mPremiumPageInfoResponse?.theme?.storeThemeId, mSelectedColorId)
                    }
                }
            }.show()
        }
    }

    override fun onPremiumColorsResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val listType = object : TypeToken<List<EditPremiumColorItemResponse?>>() {}.type
                mEditPremiumColorList = Gson().fromJson<ArrayList<EditPremiumColorItemResponse?>>(response.mCommonDataStr, listType)
                mDefaultSelectedColorItem = mPremiumPageInfoResponse?.theme?.colorItem
                mEditPremiumColorList?.add(0, mDefaultSelectedColorItem)
                showColorBottomSheet()
            }
        }
    }

    override fun onSetPremiumColorsResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_green_check_small)
                editPremiumWebView?.reload()
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onEditPremiumServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onLockedStoreShareSuccessResponse(lockedShareResponse: LockedStoreShareResponse) = showLockedStoreShareBottomSheet(lockedShareResponse)

}