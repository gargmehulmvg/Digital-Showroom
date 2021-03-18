package com.digitaldukaan.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ProfileStatusAdapter
import com.digitaldukaan.adapters.ReferAndEarnAdapter
import com.digitaldukaan.adapters.SettingsStoreAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.interfaces.IStoreSettingsItemClicked
import com.digitaldukaan.models.request.StoreDeliveryStatusChangeRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.ProfileService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProfileServiceInterface
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.settings_fragment.*


class SettingsFragment : BaseFragment(), IOnToolbarIconClick, IProfileServiceInterface,
    SwipeRefreshLayout.OnRefreshListener, IStoreSettingsItemClicked {

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
    private lateinit var mAppSettingsResponseStaticData: AccountStaticTextResponse
    private lateinit var mAppStoreServicesResponse: StoreServicesResponse
    private val mProfileService = ProfileService()
    private var mReferAndEarnResponse: ReferEarnResponse? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.settings_fragment, container, false)
        mProfileService.setProfileServiceInterface(this)
        fetchUserProfile()
        return mContentView
    }

    override fun onStart() {
        super.onStart()
        ToolBarManager.getInstance().apply {
            setSideIconVisibility(true)
            setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_setting_toolbar), this@SettingsFragment)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateNavigationBarState(R.id.menuSettings)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            onBackPressed(this@SettingsFragment)
            setSideIconVisibility(true)
            hideBackPressFromToolBar(mActivity, false)
            setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_setting_toolbar), this@SettingsFragment)
        }
        showBottomNavigationView(false)
        swipeRefreshLayout.setOnRefreshListener(this)
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            digitalShowroomWebLayout.id -> showTrendingOffersBottomSheet()
            storeSwitch.id -> changeStoreDeliveryStatus()
            deliverySwitch.id -> changeStoreDeliveryStatus()
            moreControlsTextView.id -> launchFragment(MoreControlsFragment.newInstance(mAppSettingsResponseStaticData, mAppStoreServicesResponse), true)
            moreControlsImageView.id -> launchFragment(MoreControlsFragment.newInstance(mAppSettingsResponseStaticData, mAppStoreServicesResponse), true)
            userProfileLayout.id -> launchFragment(ProfilePreviewFragment().newInstance(mProfileResponse?.mStoreInfo?.storeInfo?.name), true)
            userProfileLayout.id -> launchFragment(ProfilePreviewFragment().newInstance(mProfileResponse?.mStoreInfo?.storeInfo?.name), true)
        }
    }

    private fun showTrendingOffersBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_trending_offers,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val bottomSheetBrowserText: TextView = view.findViewById(R.id.bottomSheetBrowserText)
        val bottomSheetHeadingTextView: TextView = view.findViewById(R.id.bottomSheetHeadingTextView)
        val bottomSheetUrl: TextView = view.findViewById(R.id.bottomSheetUrl)
        val bottomSheetClose: View = view.findViewById(R.id.bottomSheetClose)
        bottomSheetBrowserText.text = mAppSettingsResponseStaticData.mBestViewedText
        val txtSpannable = SpannableString(Constants.DOTPE_OFFICIAL_URL)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, 6, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        bottomSheetUrl.text = txtSpannable
        bottomSheetHeadingTextView.setHtmlData(mAppSettingsResponseStaticData.mBottomSheetText)
        bottomSheetUrl.setOnClickListener { copyDataToClipboard(Constants.DOTPE_OFFICIAL_URL_CLIPBOARD) }
        bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
        bottomSheetDialog.show()
    }

    private fun showReferAndEarnBottomSheet(response: ReferEarnResponse?) {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_refer_and_earn,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            view.run {
                val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                val bottomSheetUpperImageView: ImageView = findViewById(R.id.bottomSheetUpperImageView)
                val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                val verifyTextView: TextView = findViewById(R.id.verifyTextView)
                val referAndEarnRecyclerView: RecyclerView = findViewById(R.id.referAndEarnRecyclerView)
                Picasso.get().load(response?.mReferAndEarnData?.imageUrl).into(bottomSheetUpperImageView)
                bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                bottomSheetHeadingTextView.text = "${response?.mReferAndEarnData?.heading1}\n${response?.mReferAndEarnData?.heading2}"
                verifyTextView.text = response?.mReferAndEarnData?.settingsTxt
                verifyTextView.setOnClickListener{
                    mReferEarnOverWhatsAppResponse.run {
                        if (mReferAndEarnData.isShareStoreBanner == true) {
                            shareDataOnWhatsAppWithImage(mReferAndEarnData.whatsAppText, mReferAndEarnData.imageUrl)
                        } else {
                            shareDataOnWhatsApp(mReferAndEarnData.whatsAppText)
                        }
                        bottomSheetDialog.dismiss()
                    }
                }
                referAndEarnRecyclerView.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    adapter = ReferAndEarnAdapter(response?.mReferAndEarnData?.workJourneyList)
                }
            }
        }.show()
        bottomSheetDialog.show()
    }

    private fun fetchUserProfile() {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity, "Fetching user profile...")
        mProfileService.getUserProfile(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
    }

    private fun changeStoreDeliveryStatus() {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        val request = StoreDeliveryStatusChangeRequest(
            if (storeSwitch.isChecked) 1 else 0,
            if (deliverySwitch.isChecked) 1 else 0
        )
        mProfileService.changeStoreAndDeliveryStatus(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
    }

    override fun onToolbarSideIconClicked() = launchFragment(
        CommonWebViewFragment().newInstance(
            getString(R.string.help),
            BuildConfig.WEB_VIEW_URL + Constants.WEB_VIEW_HELP + "?storeid=${getStringDataFromSharedPref(
                Constants.STORE_ID
            )}&" + "redirectFrom=settings" + "&token=${getStringDataFromSharedPref(
                Constants.USER_AUTH_TOKEN
            )}"
        ), true
    )

    override fun onStop() {
        super.onStop()
        ToolBarManager.getInstance().setSideIconVisibility(false)
    }

    override fun onProfileResponse(commonResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
            if (commonResponse.mIsSuccessStatus) {
                val response = Gson().fromJson<AccountInfoResponse>(commonResponse.mCommonDataStr, AccountInfoResponse::class.java)
                setupUIFromProfileResponse(response)
                response?.mAccountStaticText?.run { mAppSettingsResponseStaticData = this }
                response?.mStoreInfo?.storeServices?.run { mAppStoreServicesResponse = this }
                response?.mStoreInfo?.bankDetails?.run { StaticInstances.sBankDetails = this }
            }
        }
    }

    override fun onReferAndEarnResponse(response: ReferEarnResponse) {
        stopProgress()
        mReferAndEarnResponse = response
        CoroutineScopeUtils().runTaskOnCoroutineMain { showReferAndEarnBottomSheet(response) }
    }

    private lateinit var mReferEarnOverWhatsAppResponse: ReferEarnOverWhatsAppResponse
    private var mProfileResponse: AccountInfoResponse? = null

    override fun onReferAndEarnOverWhatsAppResponse(response: ReferEarnOverWhatsAppResponse) {
        mReferEarnOverWhatsAppResponse = response
    }

    override fun onChangeStoreAndDeliveryStatusResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val storeDeliveryService = Gson().fromJson<StoreDeliveryServiceResponse>(response.mCommonDataStr, StoreDeliveryServiceResponse::class.java)
                storeDeliveryService.let {
                    storeSwitch.isChecked = (it.mStoreFlag == 1)
                    deliverySwitch.isChecked = (it.mDeliveryFlag == 1)
                }
            } else {
                showToast(response.mMessage)
            }
        }
    }

    private fun setupUIFromProfileResponse(infoResponse: AccountInfoResponse) {
        StaticInstances.sIsStoreImageUploaded = (StaticInstances.sStoreInfo?.mStoreLogoStr?.isNotEmpty() == true)
        mProfileResponse = infoResponse
        dukaanNameTextView.text = infoResponse.mStoreInfo.storeInfo.name
        if (infoResponse.mStoreInfo.storeInfo.logoImage?.isNotEmpty() == true) {
            Picasso.get().load(infoResponse.mStoreInfo.storeInfo.logoImage).into(storePhotoImageView)
            hiddenImageView.visibility = View.INVISIBLE
            hiddenTextView.visibility = View.INVISIBLE
        }
        storeSwitch.isChecked = infoResponse.mStoreInfo.storeServices.mStoreFlag == 1
        infoResponse.mFooterImages?.forEachIndexed { index, imageUrl ->
            if (index == 0) {
                Picasso.get().load(imageUrl).placeholder(R.drawable.ic_auto_data_backup)
                    .into(autoDataBackupImageView)
            } else {
                Picasso.get().load(imageUrl).placeholder(R.drawable.ic_safe_secure)
                    .into(safeSecureImageView)
            }
        }
        deliverySwitch.isChecked = infoResponse.mStoreInfo.storeServices.mDeliveryFlag == 1
        profileStatusRecyclerView.apply {
            layoutManager = GridLayoutManager(mActivity, infoResponse.mTotalSteps)
            adapter = ProfileStatusAdapter(infoResponse.mTotalSteps, infoResponse.mCompletedSteps)
        }
        settingStoreOptionRecyclerView.apply {
            val settingsAdapter = SettingsStoreAdapter(this@SettingsFragment)
            val linearLayoutManager = LinearLayoutManager(mActivity)
            layoutManager = linearLayoutManager
            adapter = settingsAdapter
            settingsAdapter.setSettingsList(infoResponse.mStoreOptions)
//            val dividerItemDecoration = DividerItemDecoration(
//                context,
//                linearLayoutManager.orientation
//            )
//            addItemDecoration(dividerItemDecoration)
        }
        infoResponse.mTrendingList?.forEachIndexed { index, response ->
            if (0 == index) {
                Picasso.get().load(response.mCDN).placeholder(R.drawable.ic_auto_data_backup).into(viewTopStoreImageView)
                viewTopStoreTextView.text = response.mText
            }
            if (1 == index) {
                trendingTextView.text = response.mType
                Picasso.get().load(response.mCDN).placeholder(R.drawable.ic_auto_data_backup).into(digitalShowroomWebImageView)
                digitalShowroomWebTextView.text = response.mText
            }
            if (2 == index) {
                Picasso.get().load(response.mCDN).placeholder(R.drawable.ic_auto_data_backup).into(bulkUploadItemImageView)
                bulkUploadItemTextView.text = response.mText
            }
        }
        val remainingSteps = infoResponse.mTotalSteps.minus(infoResponse.mCompletedSteps)
        stepsLeftTextView.text =
            if (remainingSteps == 1) "$remainingSteps ${infoResponse.mAccountStaticText?.mStepLeft}" else "$remainingSteps ${infoResponse.mAccountStaticText?.mStepsLeft}"
        completeProfileTextView.text = infoResponse.mAccountStaticText?.mCompleteProfile
        whatsAppTextView.setOnClickListener {
            shareDataOnWhatsApp(infoResponse.waShare)
        }
        viewTopOrderLayout2.setOnClickListener {
            if (infoResponse.mTrendingList?.isNotEmpty() == true) openUrlInBrowser(
                infoResponse.mTrendingList?.get(
                    0
                )?.mPage
            )
        }
        bulkUploadItemLayout.setOnClickListener {
            if (infoResponse.mTrendingList?.size!! >= 2) openUrlInBrowser(
                infoResponse.mTrendingList?.get(
                    2
                )?.mPage
            )
        }


        storeStatusTextView.text = "${infoResponse.mAccountStaticText?.mStoreText} : ${infoResponse.mAccountStaticText?.mOffText}"
        storeSwitch.setOnCheckedChangeListener { _, isChecked ->
            run {
                Log.d(
                    SettingsFragment::class.simpleName,
                    "storeSwitch.setOnCheckedChangeListener $isChecked"
                )
                storeStatusTextView.text = "${infoResponse.mAccountStaticText?.mStoreText} : ${if (isChecked) infoResponse.mAccountStaticText?.mOnText else infoResponse.mAccountStaticText?.mOffText}"
            }
        }
        deliveryStatusTextView.text = "${infoResponse.mAccountStaticText?.mDeliveryText} : ${infoResponse.mAccountStaticText?.mOffText}"
        deliverySwitch.setOnCheckedChangeListener { _, isChecked ->
            run {
                Log.d(
                    SettingsFragment::class.simpleName,
                    "deliverySwitch.setOnCheckedChangeListener $isChecked"
                )
                deliveryStatusTextView.text = "${infoResponse.mAccountStaticText?.mDeliveryText} : ${if (isChecked) infoResponse.mAccountStaticText?.mOnText else infoResponse.mAccountStaticText?.mOffText}"
            }
        }
        moreControlsTextView.text = infoResponse.mAccountStaticText?.page_heading_more_controls
        whatsAppShareTextView.text = infoResponse.mAccountStaticText?.mShareText
        shareShowRoomWithCustomerTextView.text = infoResponse.mAccountStaticText?.mShareMessageText
        materialTextView.text = infoResponse.mAccountStaticText?.mStoreControlsText
        newReleaseHeading.text = infoResponse.mAccountStaticText?.mNewReleaseText
        ToolBarManager.getInstance().setHeaderTitle(infoResponse.mAccountStaticText?.page_heading)
    }

    private fun checkStoreOptionClick(response: StoreOptionsResponse) {
        when (response.mPage) {
            Constants.PAGE_REFER -> {
                if (mReferAndEarnResponse != null) {
                    showReferAndEarnBottomSheet(mReferAndEarnResponse)
                    return
                }
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                showProgressDialog(mActivity)
                mProfileService.getReferAndEarnData()
                mProfileService.getReferAndEarnDataOverWhatsApp()
            }
            Constants.PAGE_HELP -> onToolbarSideIconClicked()
            Constants.PAGE_FEEDBACK -> openPlayStore()
            Constants.PAGE_APP_SETTINGS -> launchFragment(AppSettingsFragment().newInstance(mProfileResponse?.mSubPages,
                response.mText, mAppSettingsResponseStaticData), true)
            Constants.PAGE_REWARDS -> launchFragment(
                CommonWebViewFragment().newInstance(
                    getString(R.string.help),
                    BuildConfig.WEB_VIEW_URL + Constants.WEB_VIEW_REWARDS + "?storeid=${getStringDataFromSharedPref(
                        Constants.STORE_ID
                    )}&" + "redirectFrom=settings" + "&token=${getStringDataFromSharedPref(
                        Constants.USER_AUTH_TOKEN
                    )}"
                ), true
            )
        }
    }

    override fun onProfileDataException(e: Exception) {
        Log.e(SettingsFragment::class.simpleName, "onProfileDataException", e)
        if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
        exceptionHandlingForAPIResponse(e)
    }

    override fun onRefresh() {
        fetchUserProfile()
    }

    override fun onStoreSettingItemClicked(storeResponse: StoreOptionsResponse) {
        showToast(storeResponse.mText)
        checkStoreOptionClick(storeResponse)
    }
}