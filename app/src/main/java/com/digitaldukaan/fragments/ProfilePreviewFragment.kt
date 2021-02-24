package com.digitaldukaan.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ProfilePreviewAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IProfilePreviewItemClicked
import com.digitaldukaan.models.response.ProfilePreviewResponse
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.digitaldukaan.services.ProfilePreviewService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProfilePreviewServiceInterface
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_preview_fragment.*


class ProfilePreviewFragment : BaseFragment(), IProfilePreviewServiceInterface,
    IProfilePreviewItemClicked {

    private var mStoreName: String? = ""
    private val mProfilePreviewStaticData = mStaticData.mStaticData.mProfileStaticData

    fun newInstance(storeName: String?): ProfilePreviewFragment {
        val fragment = ProfilePreviewFragment()
        fragment.mStoreName = storeName
        return fragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.profile_preview_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            onBackPressed(this@ProfilePreviewFragment)
            setHeaderTitle(mProfilePreviewStaticData.pageHeading)
        }
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        val service = ProfilePreviewService()
        service.setServiceInterface(this)
        service.getProfilePreviewData("2018")
    }

    override fun onProfilePreviewResponse(profilePreviewResponse: ProfilePreviewResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            profilePreviewResponse.mProfileInfo.mProfilePreviewBanner.run {
                profilePreviewBannerHeading.text = mHeading
                profilePreviewBannerStartNow.text = mStartNow
                Picasso.get().isLoggingEnabled = true
                Picasso.get().load(mCDN).into(profilePreviewBannerImageView)
                profilePreviewBannerSubHeading.text = mSubHeading
            }
            profilePreviewResponse.mProfileInfo.run {
                profilePreviewStoreNameTextView.text = mStoreName
                profilePreviewStoreMobileNumber.text = mPhoneNumber
                Picasso.get().load(mStoreLogo).into(storePhotoImageView)
            }
            profilePreviewResponse.mProfileInfo.mSettingsKeysList.run {
                val linearLayoutManager = LinearLayoutManager(mActivity)
                profilePreviewRecyclerView.apply {
                    layoutManager = linearLayoutManager
                    setHasFixedSize(true)
                    adapter = ProfilePreviewAdapter(this@run, this@ProfilePreviewFragment)
                }
                val dividerItemDecoration = DividerItemDecoration(
                    profilePreviewRecyclerView.context,
                    linearLayoutManager.orientation
                )
                profilePreviewRecyclerView.addItemDecoration(dividerItemDecoration)
            }
        }
    }

    override fun onProfilePreviewServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onProfilePreviewItemClicked(profilePreviewResponse: ProfilePreviewSettingsKeyResponse, position: Int) {
        showToast(profilePreviewResponse.mHeadingText)
        when (profilePreviewResponse.mAction) {
            Constants.ACTION_STORE_DESCRIPTION -> launchFragment(StoreDescriptionFragment.newInstance(profilePreviewResponse, position), true)
            Constants.ACTION_BANK_ACCOUNT -> launchFragment(BankAccountFragment.newInstance(profilePreviewResponse), true)
            Constants.ACTION_BUSINESS_TYPE -> launchFragment(BusinessTypeFragment.newInstance(profilePreviewResponse), true)
            Constants.ACTION_EDIT_STORE_LINK -> showEditStoreWarningDialog()
        }
    }

    private fun showEditStoreWarningDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.let {
                val warningDialog = Dialog(mActivity)
                val view = LayoutInflater.from(mActivity).inflate(R.layout.edit_store_link_dialog, null)
                warningDialog.apply {
                    setContentView(view)
                    setCancelable(true)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
                val editStoreDialogConfirmTextView: TextView = view.findViewById(R.id.editStoreDialogConfirmTextView)
                val editStoreDialogWarningOne: TextView = view.findViewById(R.id.editStoreDialogWarningOne)
                val editStoreDialogWarningTwo: TextView = view.findViewById(R.id.editStoreDialogWarningTwo)
                val editStoreDialogYesTextView: TextView = view.findViewById(R.id.editStoreDialogYesTextView)
                val editStoreDialogNoTextView: TextView = view.findViewById(R.id.editStoreDialogNoTextView)
                editStoreDialogConfirmTextView.text = mProfilePreviewStaticData.mStoreLinkChangeDialogHeading
                editStoreDialogWarningOne.text = mProfilePreviewStaticData.mStoreLinkChangeWarningOne
                editStoreDialogWarningTwo.text = mProfilePreviewStaticData.mStoreLinkChangeWarningTwo
                editStoreDialogYesTextView.text = mProfilePreviewStaticData.mYesText
                editStoreDialogNoTextView.text = mProfilePreviewStaticData.mNoText
                editStoreDialogYesTextView.setOnClickListener{ if (warningDialog.isShowing) warningDialog.dismiss() }
                editStoreDialogNoTextView.setOnClickListener{ if (warningDialog.isShowing) warningDialog.dismiss() }
                warningDialog.show()
            }
        }
    }
}