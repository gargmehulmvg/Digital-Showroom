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
import com.digitaldukaan.services.serviceinterface.IProfileServiceInterface
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.settings_fragment.*


class SettingsFragment : BaseFragment(), IOnToolbarIconClick, IProfileServiceInterface {

    fun newInstance(): SettingsFragment{
        return SettingsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.settings_fragment, container, false)
        val service = ProfileService()
        service.setProfileServiceInterface(this)
        showProgressDialog(mActivity, "Fetching user profile...")
        service.getUserProfile("2018")
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
        storeSwitch.isChecked = true
        deliverySwitch.isChecked = false
    }

    override fun onToolbarSideIconClicked() {
        showShortSnackBar()
    }

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
        dukaanNameTextView.text = profileResponse.mAccountInfoResponse?.mStoreInfo?.mStoreName
        profileResponse.mAccountInfoResponse?.mFooterImages?.forEachIndexed { index, imageUrl ->
            if (index == 0) {
                Picasso.get().load(imageUrl).placeholder(R.drawable.ic_auto_data_backup).into(autoDataBackupImageView)
            } else {
                Picasso.get().load(imageUrl).placeholder(R.drawable.ic_safe_secure).into(safeSecureImageView)
            }
        }
    }

    override fun onProfileDataException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }
}