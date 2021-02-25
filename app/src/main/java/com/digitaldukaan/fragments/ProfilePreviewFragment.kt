package com.digitaldukaan.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ProfilePreviewAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IProfilePreviewItemClicked
import com.digitaldukaan.models.request.StoreNameRequest
import com.digitaldukaan.models.response.ProfilePreviewResponse
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.digitaldukaan.services.ProfilePreviewService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProfilePreviewServiceInterface
import com.digitaldukaan.views.allowOnlyAlphaNumericCharacters
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.profile_preview_fragment.*
import okhttp3.ResponseBody


class ProfilePreviewFragment : BaseFragment(), IProfilePreviewServiceInterface,
    IProfilePreviewItemClicked {

    private var mStoreName: String? = ""
    private val mProfilePreviewStaticData = mStaticData.mStaticData.mProfileStaticData
    private val service = ProfilePreviewService()

    fun newInstance(storeName: String?): ProfilePreviewFragment {
        val fragment = ProfilePreviewFragment()
        fragment.mStoreName = storeName
        return fragment
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.profile_preview_fragment, container, false)
        service.setServiceInterface(this)
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
        service.getProfilePreviewData(getStringDataFromSharedPref(Constants.STORE_ID))
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
                if (mStoreLogo?.isNotEmpty() == true) Picasso.get().load(mStoreLogo).into(storePhotoImageView)
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

    override fun onStoreNameResponse(response: ResponseBody) {
        stopProgress()
        showToast(response.string())
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
            Constants.ACTION_STORE_NAME -> showEditStoreNameBottomSheet()
        }
    }

    private fun showEditStoreWarningDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.let {
                val warningDialog = Dialog(mActivity)
                val view = LayoutInflater.from(mActivity).inflate(R.layout.edit_store_link_warning_dialog, null)
                warningDialog.apply {
                    setContentView(view)
                    setCancelable(false)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
                view?.run {
                    val editStoreDialogConfirmTextView: TextView = findViewById(R.id.editStoreDialogConfirmTextView)
                    val editStoreDialogWarningOne: TextView = findViewById(R.id.editStoreDialogWarningOne)
                    val editStoreDialogWarningTwo: TextView = findViewById(R.id.editStoreDialogWarningTwo)
                    val editStoreDialogYesTextView: TextView = findViewById(R.id.editStoreDialogYesTextView)
                    val editStoreDialogNoTextView: TextView = findViewById(R.id.editStoreDialogNoTextView)
                    editStoreDialogConfirmTextView.text = mProfilePreviewStaticData.mStoreLinkChangeDialogHeading
                    editStoreDialogWarningOne.text = mProfilePreviewStaticData.mStoreLinkChangeWarningOne
                    editStoreDialogWarningTwo.text = mProfilePreviewStaticData.mStoreLinkChangeWarningTwo
                    editStoreDialogYesTextView.text = mProfilePreviewStaticData.mYesText
                    editStoreDialogNoTextView.text = mProfilePreviewStaticData.mNoText
                    editStoreDialogYesTextView.setOnClickListener {
                        if (warningDialog.isShowing) warningDialog.dismiss()
                        showEditStoreLinkBottomSheet()
                    }
                    editStoreDialogNoTextView.setOnClickListener{ if (warningDialog.isShowing) warningDialog.dismiss() }
                }
                warningDialog.show()
            }
        }
    }

    private var mStoreLinkBottomSheet: BottomSheetDialog? = null

    private fun showBottomSheetCancelDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.let {
                val warningDialog = Dialog(mActivity)
                val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_cancel_dialog, null)
                warningDialog.apply {
                    setContentView(view)
                    setCancelable(false)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }
                view?.run {
                    val bottomSheetCancelDialogHeading: TextView = findViewById(R.id.bottomSheetCancelDialogHeading)
                    val bottomSheetCancelDialogMessage: TextView = findViewById(R.id.bottomSheetCancelDialogMessage)
                    val bottomSheetCancelDialogYes: TextView = findViewById(R.id.bottomSheetCancelDialogYes)
                    val bottomSheetCancelDialogNo: TextView = findViewById(R.id.bottomSheetCancelDialogNo)
                    bottomSheetCancelDialogHeading.text = mProfilePreviewStaticData.mStoreLinkChangeDialogHeading
                    bottomSheetCancelDialogMessage.text = mProfilePreviewStaticData.mBottomSheetCloseConfirmationMessage
                    bottomSheetCancelDialogYes.text = mProfilePreviewStaticData.mYesText
                    bottomSheetCancelDialogNo.text = mProfilePreviewStaticData.mNoText
                    bottomSheetCancelDialogYes.setOnClickListener {
                        if (warningDialog.isShowing) {
                            warningDialog.dismiss()
                            mStoreLinkBottomSheet?.dismiss()
                        }
                    }
                    bottomSheetCancelDialogNo.setOnClickListener{ if (warningDialog.isShowing) warningDialog.dismiss() }
                }
                warningDialog.show()
            }
        }
    }

    private fun showEditStoreLinkBottomSheet() {
        mStoreLinkBottomSheet = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_edit_store_link, mActivity.findViewById(R.id.bottomSheetContainer))
        mStoreLinkBottomSheet?.apply {
            setContentView(view)
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            setCancelable(false)
            setOnDismissListener {
                Handler(Looper.getMainLooper()).postDelayed({ hideSoftKeyboard() }, Constants.TIMER_INTERVAL)
            }
            view?.run {
                val bottomSheetEditStoreHeading:TextView = findViewById(R.id.bottomSheetEditStoreHeading)
                val bottomSheetEditStoreTitle:TextView = findViewById(R.id.bottomSheetEditStoreTitle)
                val bottomSheetEditStoreLinkDText:TextView = findViewById(R.id.bottomSheetEditStoreLinkDText)
                val bottomSheetEditStoreSaveTextView:TextView = findViewById(R.id.bottomSheetEditStoreSaveTextView)
                val bottomSheetEditStoreLinkEditText: EditText = findViewById(R.id.bottomSheetEditStoreLinkEditText)
                val bottomSheetEditStoreLinkDotpe:TextView = findViewById(R.id.bottomSheetEditStoreLinkDotpe)
                val bottomSheetEditStoreLinkConditionOne:TextView = findViewById(R.id.bottomSheetEditStoreLinkConditionOne)
                val bottomSheetEditStoreLinkConditionTwo:TextView = findViewById(R.id.bottomSheetEditStoreLinkConditionTwo)
                val bottomSheetEditStoreCloseImageView:View = findViewById(R.id.bottomSheetEditStoreCloseImageView)
                bottomSheetEditStoreTitle.text = mProfilePreviewStaticData.currentLink
                bottomSheetEditStoreLinkDText.text = mProfilePreviewStaticData.dText
                bottomSheetEditStoreLinkDotpe.text = mProfilePreviewStaticData.dotPeDotInText
                bottomSheetEditStoreSaveTextView.text = mProfilePreviewStaticData.saveText
                bottomSheetEditStoreLinkConditionOne.text = mProfilePreviewStaticData.storeLinkConditionOne
                bottomSheetEditStoreLinkConditionTwo.text = mProfilePreviewStaticData.storeLinkConditionTwo
                bottomSheetEditStoreCloseImageView.setOnClickListener { showBottomSheetCancelDialog() }
                bottomSheetEditStoreHeading.text = if (bottomSheetEditStoreLinkEditText.text.isEmpty()) mProfilePreviewStaticData.storeLinkTitle else mProfilePreviewStaticData.editStoreLink
                bottomSheetEditStoreLinkEditText.addTextChangedListener(object : TextWatcher{
                    override fun afterTextChanged(s: Editable?) {
                        val string = s.toString()
                        bottomSheetEditStoreSaveTextView.isEnabled = string.isNotEmpty()
                        bottomSheetEditStoreLinkConditionOne.visibility = View.VISIBLE
                        bottomSheetEditStoreLinkConditionTwo.visibility = View.VISIBLE
                        when {
                            string.isEmpty() -> {
                                bottomSheetEditStoreLinkConditionOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exclamation_mark, 0, 0, 0);
                                bottomSheetEditStoreLinkConditionTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_exclamation_mark, 0, 0, 0);
                            }
                            string.length >= 4 -> {
                                bottomSheetEditStoreLinkConditionOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_small, 0, 0, 0);
                                bottomSheetEditStoreLinkConditionTwo.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_small, 0, 0, 0);
                            }
                            else -> {
                                bottomSheetEditStoreLinkConditionOne.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_check_small, 0, 0, 0);
                            }
                        }
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                    }

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    }

                })
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

    private fun showEditStoreNameBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_edit_store_name, mActivity.findViewById(R.id.bottomSheetContainer))
        bottomSheetDialog.apply {
            setContentView(view)
            behavior.skipCollapsed = true
            behavior.state = BottomSheetBehavior.STATE_EXPANDED
            window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
            setOnDismissListener {
                Handler(Looper.getMainLooper()).postDelayed({ hideSoftKeyboard() }, Constants.TIMER_INTERVAL)
            }
        }
        val bottomSheetEditStoreHeading:TextView = view.findViewById(R.id.bottomSheetEditStoreHeading)
        val bottomSheetEditStoreSaveTextView:TextView = view.findViewById(R.id.bottomSheetEditStoreSaveTextView)
        val bottomSheetEditStoreLinkEditText: EditText = view.findViewById(R.id.bottomSheetEditStoreLinkEditText)
        val bottomSheetEditStoreCloseImageView:View = view.findViewById(R.id.bottomSheetEditStoreCloseImageView)
        bottomSheetEditStoreSaveTextView.text = mProfilePreviewStaticData.mBottomSheetStoreButtonText
        bottomSheetEditStoreCloseImageView.setOnClickListener { if (bottomSheetDialog.isShowing) bottomSheetDialog.dismiss() }
        bottomSheetEditStoreSaveTextView.setOnClickListener {
            if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
            } else {
                val newStoreName = bottomSheetEditStoreLinkEditText.text.trim().toString()
                val request = StoreNameRequest(getStringDataFromSharedPref(Constants.STORE_ID).toInt(), newStoreName)
                showProgressDialog(mActivity)
                service.updateStoreName(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN),request)
            }
        }
        bottomSheetEditStoreHeading.text = mProfilePreviewStaticData.mBottomSheetStoreNameHeading
        bottomSheetEditStoreLinkEditText.hint = mProfilePreviewStaticData.mBottomSheetStoreNameHeading
        bottomSheetEditStoreLinkEditText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val string = s.toString()
                bottomSheetEditStoreSaveTextView.isEnabled = string.isNotEmpty()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

        })
        bottomSheetDialog.show()
    }
}