package com.digitaldukaan.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ProfileStatusAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.response.ProfileResponse
import com.digitaldukaan.models.response.StoreOptionsResponse
import com.digitaldukaan.services.ProfileService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProfileServiceInterface
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.settings_fragment.*


class SettingsFragment : BaseFragment(), IOnToolbarIconClick, IProfileServiceInterface,
    SwipeRefreshLayout.OnRefreshListener {

    fun newInstance(): SettingsFragment = SettingsFragment()
    private val mAppSettingsStaticData = mStaticData.mStaticData.mSettingsStaticData

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.settings_fragment, container, false)
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
            setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_setting_toolbar), this@SettingsFragment)
        }
        storeSwitch.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) storeStatusTextView.text = "Store : Open" else storeStatusTextView.text = "Store : Closed"
            }
        }
        deliverySwitch.setOnCheckedChangeListener { _, isChecked ->
            run {
                if (isChecked) deliveryStatusTextView.text = "Delivery : On" else deliveryStatusTextView.text = "Delivery : Off"
            }
        }
        swipeRefreshLayout.setOnRefreshListener(this)
        fetchUserProfile()
        startShinningAnimation(shinningNewTextView)
        digitalShowroomWebLayout.setOnClickListener { showTrendingOffersBottomSheet() }
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
        val service = ProfileService()
        service.setProfileServiceInterface(this)
        service.getUserProfile("2018")
    }

    override fun onToolbarSideIconClicked() = launchFragment(CommonWebViewFragment().newInstance(getString(R.string.help), BuildConfig.WEB_VIEW_URL + Constants.WEB_VIEW_HELP + "?storeid=2018&" + "redirectFrom=settings" + "&token=${getStringDataFromSharedPref(
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

    private fun setupUIFromProfileResponse(profileResponse: ProfileResponse) {
        val infoResponse = profileResponse.mAccountInfoResponse
        dukaanNameTextView.text = infoResponse?.mStoreInfo?.mStoreName
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
        infoResponse?.mStoreOptions?.forEachIndexed { index, response ->
            if (0 == index) {
                Picasso.get().load(response.mLogo).into(storeOptionOneLeftImageView)
                if (response.mIsShowMore) storeOptionOneRightImageView.visibility = View.VISIBLE
                storeOptionOneTextView.text = response.mText
                storeOptionOneLayout.setOnClickListener{
                    checkStoreOptionClick(response)
                }
            }
            if (1 == index) {
                Picasso.get().load(response.mLogo).into(storeOptionTwoLeftImageView)
                if (response.mIsShowMore) storeOptionTwoRightImageView.visibility = View.VISIBLE
                storeOptionTwoTextView.text = response.mText
                storeOptionTwoLayout.setOnClickListener{
                    checkStoreOptionClick(response)
                }
            }
            if (2 == index) {
                Picasso.get().load(response.mLogo).into(storeOptionThreeLeftImageView)
                if (response.mIsShowMore) {
                    storeOptionThreeRightImageView.visibility = View.VISIBLE
                    storeOptionThreeLayout.setOnClickListener{
                        launchFragment(AppSettingsFragment().newInstance(infoResponse.mSubPages, response.mText), true) }
                }
                storeOptionThreeTextView.text = response.mText
            }
            if (3 == index) {
                Picasso.get().load(response.mLogo).into(storeOptionFourLeftImageView)
                if (response.mIsShowMore) storeOptionFourRightImageView.visibility = View.VISIBLE
                storeOptionFourTextView.text = response.mText
                storeOptionFourLayout.setOnClickListener{
                    checkStoreOptionClick(response)
                }
            }
            if (4 == index) {
                Picasso.get().load(response.mLogo).into(storeOptionFiveLeftImageView)
                if (response.mIsShowMore) storeOptionFiveRightImageView.visibility = View.VISIBLE
                storeOptionFiveTextView.text = response.mText
                storeOptionFiveLayout.setOnClickListener{
                    checkStoreOptionClick(response)
                }
            }
        }
        infoResponse?.mTrendingList?.forEachIndexed { index, response ->
            if (0 == index) {
                newTextView.text = response.mType
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
    }

    private fun checkStoreOptionClick(response: StoreOptionsResponse) {
        when (response.mPage) {
            Constants.PAGE_HELP -> onToolbarSideIconClicked()
            Constants.PAGE_FEEDBACK -> openPlayStore()
            Constants.PAGE_REWARDS -> launchFragment(CommonWebViewFragment().newInstance(getString(R.string.help), BuildConfig.WEB_VIEW_URL + Constants.WEB_VIEW_REWARDS + "?storeid=2018&" + "redirectFrom=settings" + "&token=${getStringDataFromSharedPref(
                Constants.USER_AUTH_TOKEN)}"), true)
        }
    }

    override fun onProfileDataException(e: Exception) {
        if (swipeRefreshLayout.isRefreshing) swipeRefreshLayout.isRefreshing = false
        exceptionHandlingForAPIResponse(e)
    }

    override fun onRefresh() {
        fetchUserProfile()
    }
}