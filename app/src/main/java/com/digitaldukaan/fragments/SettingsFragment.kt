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
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ProfileStatusAdapter
import com.digitaldukaan.adapters.SettingsStoreAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.interfaces.IStoreSettingsItemClicked
import com.digitaldukaan.models.request.StoreDeliveryStatusChangeRequest
import com.digitaldukaan.models.response.AccountInfoResponse
import com.digitaldukaan.models.response.ProfileResponse
import com.digitaldukaan.models.response.StoreDeliveryStatusChangeResponse
import com.digitaldukaan.models.response.StoreOptionsResponse
import com.digitaldukaan.services.ProfileService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProfileServiceInterface
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.settings_fragment.*


class SettingsFragment : BaseFragment(), IOnToolbarIconClick, IProfileServiceInterface,
    SwipeRefreshLayout.OnRefreshListener, IStoreSettingsItemClicked {

    companion object {
        fun newInstance(): SettingsFragment = SettingsFragment()
    }

    private val mAppSettingsStaticData = mStaticData.mStaticData.mSettingsStaticData
    private val mProfileService = ProfileService()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.settings_fragment, container, false)
        mProfileService.setProfileServiceInterface(this)
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
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            onBackPressed(this@SettingsFragment)
            setHeaderTitle(getString(R.string.my_account))
            setSideIconVisibility(true)
            hideBackPressFromToolBar(mActivity, false)
            setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_setting_toolbar), this@SettingsFragment)
        }
        storeStatusTextView.text = "${mAppSettingsStaticData.mStoreText} : ${mAppSettingsStaticData.mOffText}"
        storeSwitch.setOnCheckedChangeListener { _, isChecked ->
            run {
                Log.d(SettingsFragment::class.simpleName, "storeSwitch.setOnCheckedChangeListener $isChecked")
                storeStatusTextView.text = "${mAppSettingsStaticData.mStoreText} : ${if (isChecked) mAppSettingsStaticData.mOnText else mAppSettingsStaticData.mOffText}"
            }
        }
        deliveryStatusTextView.text = "${mAppSettingsStaticData.mDeliveryText} : ${mAppSettingsStaticData.mOffText}"
        deliverySwitch.setOnCheckedChangeListener { _, isChecked ->
            run {
                Log.d(SettingsFragment::class.simpleName, "deliverySwitch.setOnCheckedChangeListener $isChecked")
                deliveryStatusTextView.text = "${mAppSettingsStaticData.mDeliveryText} : ${if (isChecked) mAppSettingsStaticData.mOnText else mAppSettingsStaticData.mOffText}"
            }
        }
        swipeRefreshLayout.setOnRefreshListener(this)
        fetchUserProfile()
        digitalShowroomWebLayout.setOnClickListener { showTrendingOffersBottomSheet() }
        storeSwitch.setOnClickListener { changeStoreDeliveryStatus() }
        deliverySwitch.setOnClickListener { changeStoreDeliveryStatus() }
        moreControlsTextView.setOnClickListener { launchFragment(MoreControlsFragment.newInstance(), true) }
        moreControlsImageView.setOnClickListener { launchFragment(MoreControlsFragment.newInstance(), true) }
    }

    private fun showTrendingOffersBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_trending_offers, mActivity.findViewById(R.id.bottomSheetContainer))
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        val bottomSheetBrowserText:TextView = view.findViewById(R.id.bottomSheetBrowserText)
        val bottomSheetHeadingTextView:TextView = view.findViewById(R.id.bottomSheetHeadingTextView)
        val bottomSheetUrl:TextView = view.findViewById(R.id.bottomSheetUrl)
        val bottomSheetClose:View = view.findViewById(R.id.bottomSheetClose)
        bottomSheetBrowserText.text = mAppSettingsStaticData.mBestViewedText
        val txtSpannable = SpannableString(Constants.DOTPE_OFFICIAL_URL)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, 6, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        bottomSheetUrl.text = txtSpannable
        bottomSheetHeadingTextView.setHtmlData(mAppSettingsStaticData.mBottomSheetText)
        bottomSheetUrl.setOnClickListener { copyDataToClipboard(Constants.DOTPE_OFFICIAL_URL_CLIPBOARD) }
        bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
        bottomSheetDialog.show()
    }

    private fun fetchUserProfile() {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity, "Fetching user profile...")
        mProfileService.getUserProfile(getStringDataFromSharedPref(Constants.STORE_ID))
    }

    private fun changeStoreDeliveryStatus() {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        val request = StoreDeliveryStatusChangeRequest(getStringDataFromSharedPref(Constants.STORE_ID).toInt(), if (storeSwitch.isChecked) 1 else 0, if (deliverySwitch.isChecked) 1 else 0, 0)
        mProfileService.changeStoreAndDeliveryStatus(request)
    }

    override fun onToolbarSideIconClicked() = launchFragment(CommonWebViewFragment().newInstance(getString(R.string.help), BuildConfig.WEB_VIEW_URL + Constants.WEB_VIEW_HELP + "?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&" + "redirectFrom=settings" + "&token=${getStringDataFromSharedPref(
        Constants.USER_AUTH_TOKEN)}"), true)

    override fun onStop() {
        super.onStop()
        ToolBarManager.getInstance().setSideIconVisibility(false)
    }

    override fun onProfileResponse(profileResponse: ProfileResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
            if (profileResponse.mStatus) setupUIFromProfileResponse(profileResponse)
        }
    }

    override fun onChangeStoreAndDeliveryStatusResponse(response: StoreDeliveryStatusChangeResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                response.mStoreDeliveryService.let {
                    storeSwitch.isChecked = (it.mStoreFlag == 1)
                    deliverySwitch.isChecked = (it.mDeliveryFlag == 1)
                }
            } else {
                showToast(response.mMessage)
            }
        }
    }

    private var mProfileResponse:AccountInfoResponse? = null

    private fun setupUIFromProfileResponse(profileResponse: ProfileResponse) {
        StaticInstances.sStoreInfo = profileResponse.mAccountInfoResponse?.mStoreInfo
        StaticInstances.sIsStoreImageUploaded = (StaticInstances.sStoreInfo?.mStoreLogoStr?.isNotEmpty() == true)
        Log.e(SettingsFragment::class.simpleName, "setupUIFromProfileResponse ${profileResponse.mMessage}")
        val infoResponse = profileResponse.mAccountInfoResponse
        mProfileResponse = infoResponse
        dukaanNameTextView.text = infoResponse?.mStoreInfo?.mStoreName
        if (infoResponse?.mStoreInfo?.mStoreLogoStr?.isNotEmpty() == true) Picasso.get().load(infoResponse.mStoreInfo.mStoreLogoStr).into(storePhotoImageView)
        storeSwitch.isChecked = infoResponse?.mStoreInfo?.mStoreService?.mStoreFlag == 1
        infoResponse?.mFooterImages?.forEachIndexed { index, imageUrl ->
            if (index == 0) {
                Picasso.get().load(imageUrl).placeholder(R.drawable.ic_auto_data_backup).into(autoDataBackupImageView)
            } else {
                Picasso.get().load(imageUrl).placeholder(R.drawable.ic_safe_secure).into(safeSecureImageView)
            }
        }
        deliverySwitch.isChecked = infoResponse?.mStoreInfo?.mStoreService?.mDeliveryFlag == 1
        profileStatusRecyclerView.apply {
            layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = ProfileStatusAdapter(infoResponse?.mTotalSteps, infoResponse?.mCompletedSteps)
        }
        settingStoreOptionRecyclerView.apply {
            val settingsAdapter = SettingsStoreAdapter(this@SettingsFragment)
            val linearLayoutManager = LinearLayoutManager(mActivity)
            layoutManager = linearLayoutManager
            adapter = settingsAdapter
            settingsAdapter.setSettingsList(infoResponse?.mStoreOptions)
            val dividerItemDecoration = DividerItemDecoration(
                context,
                linearLayoutManager.orientation
            )
            addItemDecoration(dividerItemDecoration)
        }
        infoResponse?.mTrendingList?.forEachIndexed { index, response ->
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
        val remainingSteps = infoResponse?.mTotalSteps?.minus(infoResponse.mCompletedSteps)
        stepsLeftTextView.text = if (remainingSteps == 1) "$remainingSteps ${infoResponse.mAccountStaticText?.mStepLeft}" else "$remainingSteps ${infoResponse?.mAccountStaticText?.mStepsLeft}"
        completeProfileTextView.text = infoResponse?.mAccountStaticText?.mCompleteProfile
        whatsAppTextView.setOnClickListener {
            val sharingStr = infoResponse?.mStoreShare?.mWaText
            if (infoResponse?.mStoreShare?.mShareStoreBanner!!) "\n\n" + sharingStr + infoResponse.mStoreShare?.mImageUrl
            shareDataOnWhatsApp(sharingStr)
        }
        viewTopOrderLayout.setOnClickListener{
            if (infoResponse?.mTrendingList?.isNotEmpty()!!) openUrlInBrowser(infoResponse.mTrendingList?.get(0)?.mPage)
        }
        bulkUploadItemLayout.setOnClickListener{
            if (infoResponse?.mTrendingList?.size!! >=2) openUrlInBrowser(infoResponse.mTrendingList?.get(2)?.mPage)
        }
        userProfileLayout.setOnClickListener { launchFragment(ProfilePreviewFragment().newInstance(infoResponse?.mStoreInfo?.mStoreName), true) }
    }

    private fun checkStoreOptionClick(response: StoreOptionsResponse) {
        when (response.mPage) {
            Constants.PAGE_HELP -> onToolbarSideIconClicked()
            Constants.PAGE_FEEDBACK -> openPlayStore()
            Constants.PAGE_APP_SETTINGS -> launchFragment(AppSettingsFragment().newInstance(mProfileResponse?.mSubPages, response.mText), true)
            Constants.PAGE_REWARDS -> launchFragment(CommonWebViewFragment().newInstance(getString(R.string.help), BuildConfig.WEB_VIEW_URL + Constants.WEB_VIEW_REWARDS + "?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&" + "redirectFrom=settings" + "&token=${getStringDataFromSharedPref(
                Constants.USER_AUTH_TOKEN)}"), true)
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

    override fun onStoreSettingItemClicked(storeOptionResponse: StoreOptionsResponse) {
        showToast(storeOptionResponse.mText)
        checkStoreOptionClick(storeOptionResponse)
    }
}