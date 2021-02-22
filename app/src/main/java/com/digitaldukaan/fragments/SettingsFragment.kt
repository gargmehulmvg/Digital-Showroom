package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.digitaldukaan.R
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.response.ProfileResponse
import com.digitaldukaan.services.ProfileService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProfileServiceInterface
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.settings_fragment.*


class SettingsFragment : BaseFragment(), IOnToolbarIconClick, IProfileServiceInterface {

    fun newInstance(): SettingsFragment = SettingsFragment()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.settings_fragment, container, false)
        return mContentView
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
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity, "Fetching user profile...")
        val service = ProfileService()
        service.setProfileServiceInterface(this)
        service.getUserProfile("2018")
    }

    override fun onToolbarSideIconClicked() = showShortSnackBar()

    override fun onStop() {
        super.onStop()
        ToolBarManager.getInstance().setSideIconVisibility(false)
    }

    override fun onProfileResponse(profileResponse: ProfileResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            showToast(profileResponse.mMessage)
            if (profileResponse.mStatus) {
                setupUIFromProfileResponse(profileResponse)
            }
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
        infoResponse?.mStoreOptions?.forEachIndexed { index, response ->
            if (0 == index) {
                Picasso.get().load(response.mLogo).into(storeOptionOneLeftImageView)
                if (response.mIsShowMore) storeOptionOneRightImageView.visibility = View.VISIBLE
                storeOptionOneTextView.text = response.mText
            }
            if (1 == index) {
                Picasso.get().load(response.mLogo).into(storeOptionTwoLeftImageView)
                if (response.mIsShowMore) storeOptionTwoRightImageView.visibility = View.VISIBLE
                storeOptionTwoTextView.text = response.mText
            }
            if (2 == index) {
                Picasso.get().load(response.mLogo).into(storeOptionThreeLeftImageView)
                if (response.mIsShowMore) storeOptionThreeRightImageView.visibility = View.VISIBLE
                storeOptionThreeTextView.text = response.mText
            }
            if (3 == index) {
                Picasso.get().load(response.mLogo).into(storeOptionFourLeftImageView)
                if (response.mIsShowMore) storeOptionFourRightImageView.visibility = View.VISIBLE
                storeOptionFourTextView.text = response.mText
            }
            if (4 == index) {
                Picasso.get().load(response.mLogo).into(storeOptionFiveLeftImageView)
                if (response.mIsShowMore) storeOptionFiveRightImageView.visibility = View.VISIBLE
                storeOptionFiveTextView.text = response.mText
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
    }

    override fun onProfileDataException(e: Exception) = exceptionHandlingForAPIResponse(e)
}