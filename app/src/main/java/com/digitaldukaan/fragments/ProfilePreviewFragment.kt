package com.digitaldukaan.fragments

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.GmailGuideLineAdapter
import com.digitaldukaan.adapters.ProfilePageBannerAdapter
import com.digitaldukaan.adapters.ProfilePreviewAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.interfaces.IProfilePreviewItemClicked
import com.digitaldukaan.models.request.*
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.ProfilePreviewService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProfilePreviewServiceInterface
import com.digitaldukaan.views.allowOnlyAlphaNumericCharacters
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.bottom_sheet_add_display_number.*
import kotlinx.android.synthetic.main.layout_login_fragment_v2.*
import kotlinx.android.synthetic.main.layout_profile_preview_fragment.*
import kotlinx.android.synthetic.main.layout_profile_preview_fragment.hiddenImageView
import kotlinx.android.synthetic.main.layout_profile_preview_fragment.hiddenTextView
import kotlinx.android.synthetic.main.layout_profile_preview_fragment.lockedProfilePhotoGroup
import kotlinx.android.synthetic.main.layout_profile_preview_fragment.storePhotoImageView
import kotlinx.android.synthetic.main.layout_profile_preview_fragment.swipeRefreshLayout
import kotlinx.android.synthetic.main.layout_settings_fragment.*
import kotlinx.android.synthetic.main.otp_verification_fragment.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class ProfilePreviewFragment : BaseFragment(), IProfilePreviewServiceInterface,
    IProfilePreviewItemClicked, SwipeRefreshLayout.OnRefreshListener {

    private var mStoreName: String? = ""
    private var mProfilePreviewStaticData: ProfileStaticTextResponse? = null
    private val mService = ProfilePreviewService()
    private var mStoreLinkBottomSheet: BottomSheetDialog? = null
    private var mStoreNameEditBottomSheet: BottomSheetDialog? = null
    private var mProfilePreviewResponse: ProfileInfoResponse? = null
    private var mStoreLinkErrorResponse: StoreDescriptionResponse? = null
    private lateinit var mProfileInfoSettingKeyResponse: ProfilePreviewSettingsKeyResponse
    private var cancelWarningDialog: Dialog? = null
    private var mStoreLogo: String? = ""
    private var mStoreLinkLastEntered = ""
    private var mIsCompleteProfileImageInitiated = false
    private var mIsEmailAdded = false
    private var mStoreUserPageInfoStaticTextResponse: StoreUserPageInfoStaticTextResponse? = null
    private var mCountDownTimer: CountDownTimer? = null
    private var mIsTimerCompleted = true
    private var mResendOtpBottomSheetTextView: TextView? = null
    private var mDisplayPhoneBottomSheet: BottomSheetDialog? = null
    private var mDisplayPhoneContentView: View? = null
    private var mDisplayPhoneStr = ""

    companion object {

        fun newInstance(storeName: String? = ""): ProfilePreviewFragment {
            val fragment = ProfilePreviewFragment()
            fragment.mStoreName = storeName
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_PROFILE_PAGE,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "ProfilePreviewFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_profile_preview_fragment, container, false)
        mService.setServiceInterface(this)
        mActivity?.let { cancelWarningDialog = Dialog(it) }
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            onBackPressed(this@ProfilePreviewFragment)
            headerTitle = ""
        }
        mStoreLogo = ""
        fetchProfilePreviewCall()
        hideBottomNavigationView(true)
        StaticInstances.sIsStoreImageUploaded = false
        profilePreviewStoreNameTextView?.setOnClickListener { showEditStoreNameBottomSheet(mProfilePreviewResponse?.mStoreItemResponse?.storeInfo?.name) }
        storePhotoLayout?.setOnClickListener {
            if (false == StaticInstances.sPermissionHashMap?.get(Constants.STORE_LOGO)) {
                showStaffFeatureLockedBottomSheet(Constants.NAV_BAR_SETTINGS)
                return@setOnClickListener
            }
            var storeLogo = mProfilePreviewResponse?.mStoreItemResponse?.storeInfo?.logoImage
            if (isNotEmpty(mStoreLogo)) storeLogo = mStoreLogo
            if (isNotEmpty(storeLogo)) launchFragment(ProfilePhotoFragment.newInstance(storeLogo), true, storePhotoImageView) else askCameraPermission()
        }
        constraintLayoutBanner?.setOnClickListener {
            mIsCompleteProfileImageInitiated = true
            switchToInCompleteProfileFragment(mProfilePreviewResponse)
        }
        lockedProfilePhotoGroup?.apply {
            visibility = if (true == StaticInstances.sPermissionHashMap?.get(Constants.STORE_LOGO)) View.GONE else View.VISIBLE
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        Log.d(ProfilePreviewFragment::class.simpleName, "onRequestPermissionResult")
        if (Constants.IMAGE_PICK_REQUEST_CODE == requestCode) {
            when {
                grantResults.isEmpty() -> Log.i(ProfilePreviewFragment::class.simpleName, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> showImagePickerBottomSheet()
                else -> showShortSnackBar("Permission was denied")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        stopProgress()
    }

    private fun fetchProfilePreviewCall() {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        mService.getProfilePreviewData()
        swipeRefreshLayout?.setOnRefreshListener(this)
    }

    override fun onProfilePreviewResponse(commonApiResponse: CommonApiResponse) {
        val response = Gson().fromJson<ProfileInfoResponse>(commonApiResponse.mCommonDataStr, ProfileInfoResponse::class.java)
        mProfilePreviewResponse = response
        mProfilePreviewStaticData = response?.mProfileStaticText
        StaticInstances.sStepsCompletedList = response.mStepsList
        response.mStoreItemResponse?.bankDetails?.run { StaticInstances.sBankDetails = this }
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mProfilePreviewStaticData?.run {
                profilePreviewStoreNameHeading?.text = text_store_name
                hiddenTextView?.text = text_add_photo
            }
            constraintLayoutBanner?.visibility = if (mProfilePreviewResponse?.mIsProfileCompleted == true) View.GONE else View.VISIBLE
            if (swipeRefreshLayout?.isRefreshing == true) swipeRefreshLayout?.isRefreshing = false
            stopProgress()
            mProfilePreviewResponse?.mProfilePreviewBanner?.run {
                profilePreviewBannerHeading?.text = mHeading
                profilePreviewBannerStartNow?.text = mStartNow
                profilePreviewBannerImageView?.let {
                    Glide.with(this@ProfilePreviewFragment).load(mCDN).into(it)
                }
                profilePreviewBannerSubHeading?.text = mSubHeading
            }
            ToolBarManager.getInstance()?.headerTitle = mProfilePreviewResponse?.mProfileStaticText?.pageHeading
            val bannerRecyclerView: RecyclerView? = mContentView?.findViewById(R.id.bannerRecyclerView)
            val profileBannerList = mProfilePreviewResponse?.mBannerList
            if (isEmpty(profileBannerList)) {
                bannerRecyclerView?.visibility = View.GONE
            } else {
                bannerRecyclerView?.visibility = View.VISIBLE
                bannerRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                    adapter = ProfilePageBannerAdapter(this@ProfilePreviewFragment, mProfilePreviewResponse?.mBannerList,
                        object : IAdapterItemClickListener {
                            override fun onAdapterItemClickListener(position: Int) {
                                val item = mProfilePreviewResponse?.mBannerList?.get(position)
                                when(item?.mAction) {
                                    Constants.NEW_RELEASE_TYPE_CUSTOM_DOMAIN -> {
                                        AppEventsManager.pushAppEvents(
                                            eventName = AFInAppEventType.EVENT_DOMAIN_EXPLORE,
                                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.IS_PROFILE_PAGE)
                                        )
                                        openWebViewFragment(this@ProfilePreviewFragment, "", BuildConfig.WEB_VIEW_URL + item.mDeepLinkUrl)
                                    }
                                }
                            }
                        })
                }
            }
            mProfilePreviewResponse?.mStoreItemResponse?.run {
                profilePreviewStoreNameTextView?.text = storeInfo.name
                profilePreviewStoreMobileNumber?.text = storeOwner?.phone
                mStoreLogo = storeInfo.logoImage
                if (isNotEmpty(mStoreLogo)) {
                    hiddenImageView?.visibility = View.INVISIBLE
                    hiddenTextView?.visibility = View.INVISIBLE
                    storePhotoImageView?.visibility = View.VISIBLE
                    storePhotoImageView?.let {
                        Glide.with(this@ProfilePreviewFragment).load(mStoreLogo).into(it)
                    }
                } else {
                    hiddenImageView?.visibility = View.VISIBLE
                    hiddenTextView?.visibility = View.VISIBLE
                    storePhotoImageView?.visibility = View.GONE
                }
            }
            mProfilePreviewResponse?.mSettingsKeysList?.let { list->
                profilePreviewRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    setHasFixedSize(true)
                    mActivity?.let { context ->
                        adapter = ProfilePreviewAdapter(context, list, this@ProfilePreviewFragment, mProfilePreviewResponse?.mStoreItemResponse?.storeBusiness, mProfilePreviewStaticData)
                    }
                }
            }
        }
    }

    override fun onStoreNameResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val storeNameResponse = Gson().fromJson<StoreResponse>(response.mCommonDataStr, StoreResponse::class.java)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_UPDATE_STORE_NAME,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.DOMAIN to storeNameResponse?.storeInfo?.domain,
                        AFInAppEventParameterName.NAME to storeNameResponse?.storeInfo?.name,
                        AFInAppEventParameterName.STORE_TYPE to AFInAppEventParameterName.STORE_TYPE_DUKAAN,
                        AFInAppEventParameterName.LOGO_IMAGE to storeNameResponse?.storeInfo?.logoImage,
                        AFInAppEventParameterName.STORE_URL to storeNameResponse?.storeInfo?.storeUrl,
                        AFInAppEventParameterName.REFERENCE_STORE_ID to "${storeNameResponse?.storeInfo?.referenceStoreId}"
                    )
                )
                mStoreNameEditBottomSheet?.run { if (isShowing) dismiss() }
                mStoreName = storeNameResponse.storeInfo.name
                PrefsManager.storeStringDataInSharedPref(Constants.STORE_NAME, mStoreName)
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                OrderFragment.sOrderPageInfoResponse = null
                StaticInstances.sSuggestedDomainsListFetchedFromServer = false
                StaticInstances.sSuggestedDomainsList = null
                StaticInstances.sCustomDomainBottomSheetResponse = null
                onRefresh()
            } else showToast(response.mMessage)
        }
    }

    override fun onStoreLinkResponse(response: CommonApiResponse) {
        var resultStr = ""
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val storeShareResponse = Gson().fromJson<ValidateUserResponse>(response.mCommonDataStr, ValidateUserResponse::class.java)
            mStoreLinkErrorResponse = StoreDescriptionResponse(response.mIsSuccessStatus, response.mMessage, storeShareResponse?.store, response.mErrorType)
            if (response.mIsSuccessStatus) {
                resultStr = "Available"
                mStoreLinkBottomSheet?.dismiss()
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                onRefresh()
                showShareStoreLinkBottomSheet(storeShareResponse.store?.storeInfo?.storeUrl)
            } else {
                mStoreLinkBottomSheet?.run {
                    if (isShowing) dismiss()
                    resultStr = "Unavailable"
                    showEditStoreLinkBottomSheet(mProfileInfoSettingKeyResponse , true)
                }
            }
        }
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_EDIT_STORE_LINK_SAVE,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.RESULT to resultStr)
        )
    }

    override fun onProfilePreviewServerException(e: Exception) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (true == swipeRefreshLayout?.isRefreshing) swipeRefreshLayout?.isRefreshing = false
            exceptionHandlingForAPIResponse(e)
        }
    }

    override fun onAppShareDataResponse(apiResponse: CommonApiResponse) {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_STORE_SHARE,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.IS_EDIT_STORE_LINK to "yes")
        )
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (apiResponse.mIsSuccessStatus) shareOnWhatsApp(Gson().fromJson<String>(apiResponse.mCommonDataStr, String::class.java)) else showShortSnackBar(apiResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onStoreUserPageInfoResponse(apiResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val storeUserPageInfoResponse = Gson().fromJson<StoreUserPageInfoResponse>(apiResponse.mCommonDataStr, StoreUserPageInfoResponse::class.java)
            if (apiResponse.mIsSuccessStatus) {
                mStoreUserPageInfoStaticTextResponse = storeUserPageInfoResponse?.staticText
                showStoreUserGmailGuidelineBottomSheet(storeUserPageInfoResponse)
            }
        }
    }

    override fun onSetStoreUserDetailsResponse(apiResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (apiResponse.mIsSuccessStatus) {
                showShortSnackBar(apiResponse.mMessage, true, R.drawable.ic_check_circle)
                onRefresh()
            } else showShortSnackBar(apiResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onSetGstResponse(apiResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (apiResponse.mIsSuccessStatus) {
                showShortSnackBar(apiResponse.mMessage, true, R.drawable.ic_check_circle)
                onRefresh()
            } else showShortSnackBar(apiResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onProfilePreviewItemClicked(profilePreviewResponse: ProfilePreviewSettingsKeyResponse, position: Int) {
        Log.d(TAG, "onProfilePreviewItemClicked: $profilePreviewResponse")
        mProfileInfoSettingKeyResponse = profilePreviewResponse
        when (profilePreviewResponse.mAction) {
            Constants.ACTION_STORE_DESCRIPTION -> launchFragment(StoreDescriptionFragment.newInstance(profilePreviewResponse, position, true, mProfilePreviewResponse), true)
            Constants.ACTION_STORE_LOCATION -> launchFragment(StoreMapLocationFragment.newInstance(position, true), true)
            Constants.ACTION_DOMAIN_SUCCESS -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_DOMAIN_DETAIL,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.CHANNEL to AFInAppEventParameterName.IS_PROFILE_PAGE)
                )
                openWebViewFragment(this@ProfilePreviewFragment, "", BuildConfig.WEB_VIEW_URL + profilePreviewResponse.mAction)
            }
            Constants.ACTION_BANK_ACCOUNT -> launchFragment(BankAccountFragment.newInstance(profilePreviewResponse, position, true, mProfilePreviewResponse), true)
            Constants.ACTION_BUSINESS_TYPE -> launchFragment(BusinessTypeFragment.newInstance(profilePreviewResponse, position, true, mProfilePreviewResponse), true)
            Constants.ACTION_EDIT_STORE_LINK -> showEditStoreWarningDialog(profilePreviewResponse)
            Constants.ACTION_STORE_NAME -> showEditStoreNameBottomSheet(mProfilePreviewResponse?.mStoreItemResponse?.storeInfo?.name)
            Constants.ACTION_KYC_STATUS -> {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                showProgressDialog(mActivity)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_KYC_COMPLETE_NOW,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.STATE to mProfileInfoSettingKeyResponse.mDefaultText)
                )
                mService.initiateKyc()
            }
            Constants.ACTION_EMAIL_AUTHENTICATION -> {
                showProgressDialog(mActivity)
                mService.getStoreUserPageInfo()
            }
            Constants.ACTION_GST_ADD -> showGstAdditionBottomSheet()
            Constants.ACTION_GST_REJECTED -> showGstAdditionBottomSheet()
            Constants.ACTION_DISPLAY_NUMBER -> showDisplayNumberBottomSheet(profilePreviewResponse)
        }
    }

    private fun showUserEmailDialog(isLogout: Boolean = false, isServerCall: Boolean = false) {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_EMAIL_SIGN_IN,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.IS_EDIT to if (mIsEmailAdded) "1" else "0")
        )
        mActivity?.let { context ->
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            if (isLogout) {
                signOut(googleSignInClient)
            } else {
                val account = GoogleSignIn.getLastSignedInAccount(context)
                if (null == account) {
                    val signInIntent: Intent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, Constants.REQUEST_CODE_EMAIL)
                } else {
                    updateUserAccountInfo(account, isServerCall)
                    Log.d(TAG, "showUserEmailDialog: $account")
                }
            }
        }
    }

    private fun signOut(googleSignInClient: GoogleSignInClient) {
        googleSignInClient.signOut().addOnCompleteListener {
            showUserEmailDialog(isServerCall = false)
        }
    }

    private fun updateUserAccountInfo(acct: GoogleSignInAccount?, isServerCall: Boolean = false) {
        val personGivenName = acct?.givenName
        val personFamilyName = acct?.familyName
        val personEmail = acct?.email
        val personId = acct?.id
        val personPhoto: Uri? = acct?.photoUrl
        if (isServerCall) {
            showProgressDialog(mActivity)
            val request= StoreUserMailDetailsRequest(
                firstName = personGivenName,
                lastName = personFamilyName,
                emailId = personEmail,
                photo = if (null == personPhoto) "" else "$personPhoto",
                signInId = personId
            )
            mService.setStoreUserGmailDetails(request)
        }
    }

    private fun showEditStoreWarningDialog(profilePreviewResponse: ProfilePreviewSettingsKeyResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.let {
                val warningDialog = Dialog(it)
                val view = LayoutInflater.from(it).inflate(R.layout.edit_store_link_warning_dialog, null)
                warningDialog.apply {
                    setContentView(view)
                    setCancelable(false)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    view?.run {
                        val editStoreDialogConfirmTextView: TextView = findViewById(R.id.editStoreDialogConfirmTextView)
                        val editStoreDialogWarningOne: TextView = findViewById(R.id.editStoreDialogWarningOne)
                        val editStoreDialogWarningTwo: TextView = findViewById(R.id.editStoreDialogWarningTwo)
                        val editStoreDialogYesTextView: TextView = findViewById(R.id.editStoreDialogYesTextView)
                        val editStoreDialogNoTextView: TextView = findViewById(R.id.editStoreDialogNoTextView)
                        editStoreDialogConfirmTextView.text = mProfilePreviewStaticData?.mStoreLinkChangeDialogHeading
                        editStoreDialogWarningOne.text = mProfilePreviewStaticData?.mStoreLinkChangeWarningOne
                        editStoreDialogWarningTwo.text = mProfilePreviewStaticData?.mStoreLinkChangeWarningTwo
                        editStoreDialogYesTextView.text = mProfilePreviewStaticData?.mYesText
                        editStoreDialogNoTextView.text = mProfilePreviewStaticData?.mNoText
                        editStoreDialogYesTextView.setOnClickListener {
                            AppEventsManager.pushAppEvents(
                                eventName = AFInAppEventType.EVENT_EDIT_STORE_LINK_WARNING,
                                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.SELECT to "yes")
                            )
                            if (warningDialog.isShowing) warningDialog.dismiss()
                            showEditStoreLinkBottomSheet(profilePreviewResponse)
                        }
                        editStoreDialogNoTextView.setOnClickListener{
                            AppEventsManager.pushAppEvents(
                                eventName = AFInAppEventType.EVENT_EDIT_STORE_LINK_WARNING,
                                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.SELECT to "no")
                            )
                            if (warningDialog.isShowing) warningDialog.dismiss()
                        }
                    }
                }.show()
            }
        }
    }

    private fun showBottomSheetCancelDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.let {
                val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_cancel_dialog, null)
                cancelWarningDialog?.apply {
                    setContentView(view)
                    setCancelable(false)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    view?.run {
                        val bottomSheetCancelDialogHeading: TextView = findViewById(R.id.bottomSheetCancelDialogHeading)
                        val bottomSheetCancelDialogMessage: TextView = findViewById(R.id.bottomSheetCancelDialogMessage)
                        val bottomSheetCancelDialogYes: TextView = findViewById(R.id.bottomSheetCancelDialogYes)
                        val bottomSheetCancelDialogNo: TextView = findViewById(R.id.bottomSheetCancelDialogNo)
                        bottomSheetCancelDialogHeading.text = mProfilePreviewStaticData?.mStoreLinkChangeDialogHeading
                        bottomSheetCancelDialogMessage.text = mProfilePreviewStaticData?.mBottomSheetCloseConfirmationMessage
                        bottomSheetCancelDialogYes.text = mProfilePreviewStaticData?.mYesText
                        bottomSheetCancelDialogNo.text = mProfilePreviewStaticData?.mNoText
                        bottomSheetCancelDialogYes.setOnClickListener {
                            cancelWarningDialog?.dismiss()
                            mStoreLinkBottomSheet?.dismiss()
                        }
                        bottomSheetCancelDialogNo.setOnClickListener { cancelWarningDialog?.dismiss() }
                    }
                }?.show()
            }
        }
    }

    private fun showEditStoreLinkBottomSheet(profilePreviewResponse: ProfilePreviewSettingsKeyResponse, isErrorResponse:Boolean = false) {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_EDIT_STORE_LINK,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
        mActivity?.let {
            mStoreLinkBottomSheet = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_edit_store_link, it.findViewById(R.id.bottomSheetContainer))
            mStoreLinkBottomSheet?.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
                view?.run {
                    val bottomSheetEditStoreHeading: TextView = findViewById(R.id.bottomSheetEditStoreHeading)
                    val bottomSheetEditStoreTitle: TextView = findViewById(R.id.bottomSheetEditStoreTitle)
                    val bottomSheetEditStoreLinkDText: TextView = findViewById(R.id.bottomSheetEditStoreLinkDText)
                    val bottomSheetEditStoreSaveTextView: TextView = findViewById(R.id.bottomSheetEditStoreSaveTextView)
                    val bottomSheetEditStoreLinkEditText: EditText = findViewById(R.id.bottomSheetEditStoreLinkEditText)
                    val bottomSheetEditStoreLinkDotpe: TextView = findViewById(R.id.bottomSheetEditStoreLinkDotpe)
                    val bottomSheetEditStoreLinkConditionOne: TextView = findViewById(R.id.bottomSheetEditStoreLinkConditionOne)
                    val bottomSheetEditStoreLinkConditionTwo: TextView = findViewById(R.id.bottomSheetEditStoreLinkConditionTwo)
                    val bottomSheetEditStoreCloseImageView: View = findViewById(R.id.bottomSheetEditStoreCloseImageView)
                    val bottomSheetEditStoreLinkServerError: TextView = findViewById(R.id.bottomSheetEditStoreLinkServerError)
                    bottomSheetEditStoreLinkDText.text = mProfilePreviewStaticData?.dText
                    bottomSheetEditStoreLinkDotpe.text = mProfilePreviewStaticData?.dotPeDotInText
                    bottomSheetEditStoreSaveTextView.text = mProfilePreviewStaticData?.saveText
                    bottomSheetEditStoreLinkConditionOne.text = mProfilePreviewStaticData?.storeLinkConditionOne
                    bottomSheetEditStoreLinkConditionTwo.text = mProfilePreviewStaticData?.storeLinkConditionTwo
                    bottomSheetEditStoreCloseImageView.setOnClickListener { showBottomSheetCancelDialog() }
                    bottomSheetEditStoreSaveTextView.setOnClickListener {
                        val newStoreLink = bottomSheetEditStoreLinkEditText.text.trim().toString()
                        if (newStoreLink.isEmpty()) {
                            bottomSheetEditStoreLinkEditText.apply {
                                error = mProfilePreviewStaticData?.error_mandatory_field
                                requestFocus()
                            }
                            return@setOnClickListener
                        } else if (newStoreLink.length < resources.getInteger(R.integer.store_name_min_length)) {
                            bottomSheetEditStoreLinkEditText.apply {
                                error = mProfilePreviewStaticData?.storeLinkConditionTwo
                                requestFocus()
                            }
                            return@setOnClickListener
                        }
                        if (!isInternetConnectionAvailable(mActivity)) {
                            showNoInternetConnectionDialog()
                        } else {
                            val request = StoreLinkRequest(getStringDataFromSharedPref(Constants.STORE_ID).toInt(), newStoreLink)
                            showProgressDialog(mActivity)
                            mStoreLinkLastEntered = newStoreLink
                            mService.updateStoreLink(request)
                        }
                    }
                    bottomSheetEditStoreHeading.text = if (bottomSheetEditStoreLinkEditText.text.isEmpty()) mProfilePreviewStaticData?.storeLinkTitle else mProfilePreviewStaticData?.editStoreLink
                    val requiredString = profilePreviewResponse.mValue?.run {
                        substring(indexOf("-") + 1, indexOf("."))
                    }
                    bottomSheetEditStoreLinkEditText.setText(if (isErrorResponse) mStoreLinkLastEntered else requiredString)
                    bottomSheetEditStoreTitle.text = if (isErrorResponse) it.getString(R.string.your_edited_link) else mProfilePreviewStaticData?.currentLink
                    bottomSheetEditStoreLinkEditText.isEnabled = profilePreviewResponse.mIsEditable
                    bottomSheetEditStoreLinkEditText.addTextChangedListener(object : TextWatcher{
                        override fun afterTextChanged(s: Editable?) {
                            val string = s.toString()
                            bottomSheetEditStoreSaveTextView.isEnabled = string.isNotEmpty()
                            bottomSheetEditStoreLinkConditionOne.visibility = View.VISIBLE
                            bottomSheetEditStoreLinkConditionTwo.visibility = View.VISIBLE
                            bottomSheetEditStoreLinkServerError.visibility = View.GONE
                            when {
                                isEmpty(string) -> {
                                    bottomSheetEditStoreLinkConditionOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exclamation_mark, 0, 0, 0)
                                    bottomSheetEditStoreLinkConditionTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exclamation_mark, 0, 0, 0)
                                }
                                string.length >= resources.getInteger(R.integer.store_name_min_length) -> {
                                    bottomSheetEditStoreLinkConditionOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_small, 0, 0, 0)
                                    bottomSheetEditStoreLinkConditionTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_small, 0, 0, 0)
                                }
                                string.length <= resources.getInteger(R.integer.store_name_min_length) -> {
                                    bottomSheetEditStoreLinkConditionTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exclamation_mark, 0, 0, 0)
                                }
                                else -> {
                                    bottomSheetEditStoreLinkConditionOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_small, 0, 0, 0)
                                }
                            }
                        }

                        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                    })
                    if (isErrorResponse) {
                        bottomSheetEditStoreLinkConditionOne.visibility = View.GONE
                        bottomSheetEditStoreLinkConditionTwo.visibility = View.GONE
                        bottomSheetEditStoreLinkServerError.visibility = View.VISIBLE
                        bottomSheetEditStoreLinkServerError.text = when (mStoreLinkErrorResponse?.mErrorString) {
                            Constants.ERROR_DOMAIN_ALREADY_EXISTS -> mProfilePreviewStaticData?.mDomainAlreadyExistError
                            else -> mProfilePreviewStaticData?.mDomainUnAvailableError
                        }
                    }
                    bottomSheetEditStoreLinkEditText.allowOnlyAlphaNumericCharacters()
                }
            }?.show()
            mStoreLinkBottomSheet?.setOnKeyListener { _, keyCode, _ ->
                if (KeyEvent.KEYCODE_BACK == keyCode) {
                    showBottomSheetCancelDialog()
                    true
                } else false
            }
        }
    }

    private fun showEditStoreNameBottomSheet(storeValue: String?) {
        mActivity?.let {
            mStoreNameEditBottomSheet = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_edit_store_name, it.findViewById(R.id.bottomSheetContainer))
            mStoreNameEditBottomSheet?.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
            }
            val bottomSheetEditStoreHeading:TextView = view.findViewById(R.id.bottomSheetEditStoreHeading)
            val bottomSheetEditStoreSaveTextView:TextView = view.findViewById(R.id.bottomSheetEditStoreSaveTextView)
            val bottomSheetEditStoreLinkEditText: EditText = view.findViewById(R.id.bottomSheetEditStoreLinkEditText)
            val bottomSheetEditStoreCloseImageView:View = view.findViewById(R.id.bottomSheetEditStoreCloseImageView)
            bottomSheetEditStoreSaveTextView.text = mProfilePreviewStaticData?.mBottomSheetStoreButtonText
            bottomSheetEditStoreCloseImageView.setOnClickListener { mStoreNameEditBottomSheet?.dismiss() }
            bottomSheetEditStoreSaveTextView.setOnClickListener {
                val newStoreName = bottomSheetEditStoreLinkEditText.text.trim().toString()
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                } else {
                    val request = StoreNameRequest(newStoreName)
                    showProgressDialog(mActivity)
                    bottomSheetEditStoreSaveTextView.isEnabled = false
                    mService.updateStoreName(request)
                }
            }
            bottomSheetEditStoreHeading.text = mProfilePreviewStaticData?.mBottomSheetStoreNameHeading
            bottomSheetEditStoreLinkEditText.hint = mProfilePreviewStaticData?.mBottomSheetStoreNameHeading
            if (isNotEmpty(storeValue)) bottomSheetEditStoreLinkEditText.setText(storeValue)
            bottomSheetEditStoreLinkEditText.addTextChangedListener(object : TextWatcher{
                override fun afterTextChanged(s: Editable?) {
                    val string = s.toString()
                    bottomSheetEditStoreSaveTextView.isEnabled = string.isNotEmpty()
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            })
            mStoreNameEditBottomSheet?.show()
        }
    }
    private fun showGstAdditionBottomSheet() {
        mActivity?.let {
            val gstAdditionBottomSheet = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_gst_addition, it.findViewById(R.id.bottomSheetContainer))
            gstAdditionBottomSheet.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
            }
            val bottomSheetEditStoreHeading:TextView = view.findViewById(R.id.bottomSheetEditStoreHeading)
            val bottomSheetEditStoreSaveTextView:TextView = view.findViewById(R.id.bottomSheetEditStoreSaveTextView)
            val bottomSheetEditStoreLinkEditText: EditText = view.findViewById(R.id.bottomSheetEditStoreLinkEditText)
            val bottomSheetEditStoreCloseImageView:View = view.findViewById(R.id.bottomSheetEditStoreCloseImageView)
            bottomSheetEditStoreCloseImageView.setOnClickListener { gstAdditionBottomSheet.dismiss() }
            bottomSheetEditStoreSaveTextView.apply {
                text = mProfilePreviewStaticData?.bottom_sheet_gst_cta_text
                setOnClickListener {
                    val value = bottomSheetEditStoreLinkEditText.text.trim().toString()
                    when {
                        isEmpty(value) -> {
                            bottomSheetEditStoreLinkEditText.apply {
                                error = mProfilePreviewStaticData?.error_mandatory_field
                                requestFocus()
                            }
                            return@setOnClickListener
                        }
                        value.length < (mActivity?.resources?.getInteger(R.integer.gst_count) ?: 15) -> {
                            bottomSheetEditStoreLinkEditText.apply {
                                error = mProfilePreviewStaticData?.bottom_sheet_gst_error_invalid_input
                                requestFocus()
                            }
                            return@setOnClickListener
                        }
                        !isInternetConnectionAvailable(mActivity) -> { showNoInternetConnectionDialog() }
                        else -> {
                            gstAdditionBottomSheet.dismiss()
                            showProgressDialog(mActivity)
                            mService.setGST(value)
                        }
                    }
                }
            }
            bottomSheetEditStoreHeading.text = mProfilePreviewStaticData?.bottom_sheet_gst_heading
            bottomSheetEditStoreLinkEditText.apply {
                hint = mProfilePreviewStaticData?.bottom_sheet_gst_hint
                addTextChangedListener(object : TextWatcher {

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                    override fun afterTextChanged(s: Editable?) {
                        val str = s?.toString()?.trim()
                        bottomSheetEditStoreSaveTextView.isEnabled = isNotEmpty(str)
                    }

                })
            }
            gstAdditionBottomSheet.show()
        }
    }

    override fun onRefresh() = fetchProfilePreviewCall()

    override fun onImageSelectionResultFile(file: File?, mode: String) {
        if (Constants.MODE_CROP == mode) {
            val fragment = CropPhotoFragment.newInstance(file?.toUri())
            fragment.setTargetFragment(this, Constants.REQUEST_CODE_CROP_IMAGE)
            launchFragment(fragment, true)
            return
        }
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
        }
        file?.run {
            val fileRequestBody = MultipartBody.Part.createFormData("media", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
            val imageTypeRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), Constants.BASE64_STORE_LOGO)
            mService.generateCDNLink(imageTypeRequestBody, fileRequestBody)
        }
    }

    override fun onStoreLogoResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val photoResponse = Gson().fromJson<StoreResponse>(response.mCommonDataStr, StoreResponse::class.java)
                mStoreLogo = photoResponse.storeInfo.logoImage
                if (isNotEmpty(mStoreLogo)) {
                    storePhotoImageView?.visibility = View.VISIBLE
                    hiddenImageView?.visibility = View.INVISIBLE
                    hiddenTextView?.visibility = View.INVISIBLE
                    storePhotoImageView?.let {
                        try {
                            Glide.with(this).load(mStoreLogo).into(it)
                        } catch (e: Exception) {
                            Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                        }
                    }
                } else {
                    StaticInstances.sIsStoreImageUploaded = false
                    storePhotoImageView?.visibility = View.GONE
                    hiddenImageView?.visibility = View.VISIBLE
                    hiddenTextView?.visibility = View.VISIBLE
                }
                if (mIsCompleteProfileImageInitiated) {
                    StaticInstances.sStepsCompletedList?.run {
                        for (completedItem in this) {
                            if (completedItem.action == Constants.ACTION_LOGO) {
                                completedItem.isCompleted = true
                                break
                            }
                        }
                        switchToInCompleteProfileFragment(mProfilePreviewResponse)
                    }
                }
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
            } else showToast(response.mMessage)
        }
    }

    override fun onImageCDNLinkGenerateResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            val photoResponse = Gson().fromJson<String>(response.mCommonDataStr, String::class.java)
            stopProgress()
            mService.updateStoreLogo(StoreLogoRequest(photoResponse))
        }
    }

    override fun onInitiateKycResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                onRefresh()
                openUrlInBrowser(Gson().fromJson<String>(response.mCommonDataStr, String::class.java))
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            Constants.REQUEST_CODE_CROP_IMAGE -> {
                val file = data?.getSerializableExtra(Constants.MODE_CROP) as File
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    onImageSelectionResultFile(file, "")
                }
            }
            Constants.REQUEST_CODE_EMAIL -> {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount = completedTask.getResult(ApiException::class.java)
            updateUserAccountInfo(account, true)
            Log.d(TAG, "handleSignInResult: $account")
        } catch (e: ApiException) {
            Log.d(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun showShareStoreLinkBottomSheet(storeDomain: String?) {
        mActivity?.let {
            val shareStoreBottomSheet = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_share_store, it.findViewById(R.id.bottomSheetContainer))
            shareStoreBottomSheet.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
                val storeLinkTextView:TextView = view.findViewById(R.id.storeLinkTextView)
                val shareStoreLinkTextView:TextView = view.findViewById(R.id.shareStoreLinkTextView)
                storeLinkTextView.text = storeDomain
                shareStoreLinkTextView.setOnClickListener {
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return@setOnClickListener
                    }
                    showProgressDialog(mActivity)
                    mService.getShareStoreData()
                    shareStoreBottomSheet.dismiss()
                }
            }.show()
        }
    }

    private fun showStoreUserGmailGuidelineBottomSheet(response: StoreUserPageInfoResponse?) {
        mActivity?.let {
            val shareStoreBottomSheet = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_store_user_gmail, it.findViewById(R.id.bottomSheetContainer))
            shareStoreBottomSheet.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
                val closeImageView: View = view.findViewById(R.id.closeImageView)
                val noGoogleUserLayout: View = view.findViewById(R.id.noGoogleUserLayout)
                val googleUserLayout: View = view.findViewById(R.id.googleUserLayout)
                val imageView: ImageView = view.findViewById(R.id.imageView)
                val recyclerView: RecyclerView = view.findViewById(R.id.guidelineRecyclerView)
                val openGmailDialogTextView: TextView = view.findViewById(R.id.openGmailDialogTextView)
                val headingTextView: TextView = view.findViewById(R.id.headingTextView)
                val gmailAccountTextView: TextView = view.findViewById(R.id.gmailAccountTextView)
                val signInWithAnotherAccountTextView: TextView = view.findViewById(R.id.signInWithAnotherAccountTextView)
                val youLinkGmailAccountTextView: TextView = view.findViewById(R.id.youLinkGmailAccountTextView)
                if (isEmpty(response?.gmailUserDetailsList)) {
                    mIsEmailAdded = false
                    noGoogleUserLayout.visibility =  View.VISIBLE
                    googleUserLayout.visibility =  View.GONE
                } else {
                    mIsEmailAdded = true
                    noGoogleUserLayout.visibility =  View.GONE
                    googleUserLayout.visibility =  View.VISIBLE
                }
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_ADD_EMAIL,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.IS_EDIT to if (mIsEmailAdded) "1" else "0")
                )
                youLinkGmailAccountTextView.text = mStoreUserPageInfoStaticTextResponse?.header_linked_gmail_account
                openGmailDialogTextView.text = mStoreUserPageInfoStaticTextResponse?.button_sign_in_with_google
                headingTextView.text = mStoreUserPageInfoStaticTextResponse?.header_add_gmail_account
                gmailAccountTextView.text = response?.gmailUserDetailsList?.get(0)?.email_id
                signInWithAnotherAccountTextView.text = mStoreUserPageInfoStaticTextResponse?.button_sign_in_with_another_account
                recyclerView.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    adapter = GmailGuideLineAdapter(response?.gmailGuideLineList)
                }
                activity?.let { context ->
                    Glide.with(context).load(response?.gmailCdn).into(imageView)
                }
                closeImageView.setOnClickListener {
                    shareStoreBottomSheet.dismiss()
                }
                openGmailDialogTextView.setOnClickListener {
                    shareStoreBottomSheet.dismiss()
                    showUserEmailDialog(true)
                }
                signInWithAnotherAccountTextView.setOnClickListener {
                    shareStoreBottomSheet.dismiss()
                    showUserEmailDialog(true)
                }
            }.show()
        }
    }

    override fun onBackPressed(): Boolean {
        Log.d(TAG, "onBackPressed: called")
        if(fragmentManager != null && fragmentManager?.backStackEntryCount == 1) {
            clearFragmentBackStack()
            launchFragment(SettingsFragment.newInstance(), true)
            return true
        }
        return false
    }

    private fun showDisplayNumberBottomSheet(profilePreviewResponse: ProfilePreviewSettingsKeyResponse) {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_EDIT_DISPLAY_PHONE, isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (true == mDisplayPhoneBottomSheet?.isShowing) return@runTaskOnCoroutineMain
            mActivity?.let { context ->
                mDisplayPhoneBottomSheet = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
                mDisplayPhoneContentView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_add_display_number, context.findViewById(R.id.bottomSheetContainer))
                mDisplayPhoneBottomSheet?.apply {
                    mDisplayPhoneContentView?.let { view ->
                        setContentView(view)
                        setBottomSheetCommonProperty()
                        mResendOtpBottomSheetTextView = view.findViewById(R.id.resendOtpTextView)
                        val headingTextView: TextView = view.findViewById(R.id.headingTextView)
                        val subHeadingTextView: TextView = view.findViewById(R.id.subHeadingTextView)
                        val verifyTextView: TextView = view.findViewById(R.id.verifyTextView)
                        val continueTextView: TextView = view.findViewById(R.id.continueTextView)
                        val displayNameEditText: EditText = view.findViewById(R.id.displayNameEditText)
                        val displayNameLayout: TextInputLayout = view.findViewById(R.id.displayNameLayout)
                        continueTextView.text = mProfilePreviewStaticData?.text_get_otp
                        verifyTextView.text = mProfilePreviewStaticData?.text_verified
                        mResendOtpBottomSheetTextView?.setOnClickListener {
                            if (mProfilePreviewStaticData?.text_resend_otp.equals(mResendOtpBottomSheetTextView?.text.toString(), true)) {
                                showProgressDialog(mActivity)
                                mService.generateOTP(mDisplayPhoneStr)
                            }
                        }
                        displayNameLayout.hint = profilePreviewResponse.mHeadingText
                        displayNameEditText.apply {
                            setText(profilePreviewResponse.mValue)
                            continueTextView.isEnabled = false
                            addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                                override fun afterTextChanged(s: Editable?) {
                                    val str = s?.toString() ?: ""
                                    displayNameLayout.error = null
                                    if (mProfilePreviewStaticData?.text_get_otp == continueTextView.text) {
                                        continueTextView.isEnabled = (context.resources?.getInteger(R.integer.mobile_number_length) == str.length && str != profilePreviewResponse.mValue)
                                        verifyTextView.visibility = View.INVISIBLE
                                    } else {
                                        continueTextView.isEnabled = (context.resources?.getInteger(R.integer.otp_length) == str.length)
                                    }
                                }

                            })
                            setOnEditorActionListener { _, actionId, _ ->
                                if (EditorInfo.IME_ACTION_DONE == actionId) continueTextView.callOnClick()
                                true
                            }
                        }
                        continueTextView.setOnClickListener {
                            val userInput = displayNameEditText.text.toString().trim()
                            if (isEmpty(userInput)) {
                                displayNameEditText.error = mProfilePreviewStaticData?.error_mandatory_field
                                return@setOnClickListener
                            }
                            if (context.resources.getText(R.string.get_otp) == continueTextView.text) {
                                mDisplayPhoneStr = userInput
                                showProgressDialog(mActivity)
                                mService.generateOTP(userInput)
                                AppEventsManager.pushAppEvents(
                                    eventName = AFInAppEventType.EVENT_GET_OTP,
                                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                    data = mapOf(
                                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                        AFInAppEventParameterName.PHONE to mDisplayPhoneStr)
                                )
                            } else {
                                showProgressDialog(mActivity)
                                mService.verifyDisplayPhoneNumber(VerifyDisplayPhoneNumberRequest(phone = mDisplayPhoneStr, otp = userInput.toInt()))
                            }
                        }
                        mProfilePreviewStaticData?.let { staticText ->
                            headingTextView.text = staticText.heading_bottom_sheet_display_number
                            subHeadingTextView.text = staticText.sub_heading_bottom_sheet_display_number
                        }
                    }
                }?.show()
            }
        }
    }

    override fun onGenerateOTPResponse(generateOtpResponse: GenerateOtpResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (generateOtpResponse.mStatus) {
                mDisplayPhoneContentView?.let { view ->
                    val otpSentToTextView: TextView = view.findViewById(R.id.otpSentToTextView)
                    val continueTextView: TextView = view.findViewById(R.id.continueTextView)
                    val displayNameEditText: EditText = view.findViewById(R.id.displayNameEditText)
                    val displayNameLayout: TextInputLayout = view.findViewById(R.id.displayNameLayout)
                    val otpMessageStr = "${mProfilePreviewStaticData?.text_otp_sent_to} $mDisplayPhoneStr"
                    otpSentToTextView.apply {
                        visibility = View.VISIBLE
                        text = otpMessageStr
                    }
                    continueTextView.text = mProfilePreviewStaticData?.text_verify
                    displayNameEditText.setMaxLength(context?.resources?.getInteger(R.integer.otp_length) ?: 4)
                    displayNameEditText.text = null
                    displayNameLayout.hint = mProfilePreviewStaticData?.hint_bottom_sheet_otp
                    if (mIsTimerCompleted) startCountDownTimer()
                }
            } else showToast(generateOtpResponse.mMessage)
            stopProgress()
        }
    }

    override fun onVerifyDisplayPhoneNumberResponse(apiResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val isOtpVerified: Boolean
            if (apiResponse.mIsSuccessStatus) {
                stopProgress()
                isOtpVerified = true
                mDisplayPhoneBottomSheet?.dismiss()
                showDisplayPhoneSuccessBottomSheet()
            } else {
                isOtpVerified = false
                mDisplayPhoneContentView?.let { view ->
                    val displayNameLayout: TextInputLayout = view.findViewById(R.id.displayNameLayout)
                    displayNameLayout.error = apiResponse.mMessage
                }
            }
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_VERIFY_OTP,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    AFInAppEventParameterName.VERIFY to if (isOtpVerified) "1" else "0",
                    AFInAppEventParameterName.PHONE to mDisplayPhoneStr)
            )
            stopProgress()
        }
    }

    private fun startCountDownTimer() {
        mResendOtpBottomSheetTextView?.visibility = View.VISIBLE
        mCountDownTimer = object: CountDownTimer(Constants.TIMER_RESEND_OTP, Constants.TIMER_DELAY) {

            override fun onTick(millisUntilFinished: Long) {
                mIsTimerCompleted = false
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    try {
                        val seconds = (millisUntilFinished / 1000)
                        val secRemaining = "00:${if (isSingleDigitNumber(seconds)) "0$seconds" else "$seconds"}"
                        mResendOtpBottomSheetTextView?.text = secRemaining
                    } catch (e: Exception) {
                        Log.e(TAG, "onTick: ${e.message}", e)
                    }
                }
            }

            override fun onFinish() {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    try {
                        mIsTimerCompleted = true
                        mResendOtpBottomSheetTextView?.text = mProfilePreviewStaticData?.text_resend_otp
                    } catch (e: Exception) {
                        Log.e(TAG, "onFinish: ${e.message}", e)
                    }
                }
            }
        }
        mCountDownTimer?.start()
    }

    private fun showDisplayPhoneSuccessBottomSheet() {
        mActivity?.let {
            val successBottomSheet = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_display_number_success, it.findViewById(R.id.bottomSheetContainer))
            successBottomSheet.apply {
                setContentView(view)
                setCancelable(false)
                val messageTextView: TextView = view.findViewById(R.id.messageTextView)
                val ctaTextView: TextView = view.findViewById(R.id.ctaTextView)
                messageTextView.text = mProfilePreviewStaticData?.message_bottom_sheet_display_number_success
                ctaTextView.apply {
                    text = mProfilePreviewStaticData?.text_close
                    setOnClickListener {
                        successBottomSheet.dismiss()
                        onRefresh()
                    }
                }
            }.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (false == mActivity?.isDestroyed) mCountDownTimer?.cancel()
    }
}