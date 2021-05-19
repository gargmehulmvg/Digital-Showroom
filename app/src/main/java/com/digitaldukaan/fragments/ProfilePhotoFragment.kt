package com.digitaldukaan.fragments

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.net.toUri
import androidx.transition.TransitionInflater
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.StaticInstances
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.request.StoreLogoRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.StoreResponse
import com.digitaldukaan.services.ProfilePhotoService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProfilePhotoServiceInterface
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_profile_photo_fragment.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class ProfilePhotoFragment : BaseFragment(), View.OnClickListener, IProfilePhotoServiceInterface {

    private var mStoreLogoLinkStr: String? = ""

    companion object {

        private const val TAG = "ProfilePhotoFragment"
        private val service: ProfilePhotoService = ProfilePhotoService()

        fun newInstance(storeLinkStr: String?): ProfilePhotoFragment {
            val fragment = ProfilePhotoFragment()
            fragment.mStoreLogoLinkStr = storeLinkStr
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_profile_photo_fragment, container, false)
        service.setServiceInterface(this)
        StaticInstances.sIsStoreImageUploaded = false
        hideBottomNavigationView(true)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ToolBarManager.getInstance()?.hideToolBar(mActivity, true)
        profilePhotoImageView?.let {
            if (mStoreLogoLinkStr?.isEmpty() == false) {
                try {
                    Picasso.get().load(mStoreLogoLinkStr).into(it)
                } catch (e: Exception) {
                    Log.e(TAG, "picasso image loading issue: ${e.message}", e)
                }
            }
        }
        backImageView?.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            backImageView?.id -> mActivity?.onBackPressed()
            removePhotoTextView?.id -> showImageRemovalApprovalDialog()
            changePhotoTextView?.id -> askCameraPermission()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "$TAG onRequestPermissionResult")
        if (requestCode == Constants.IMAGE_PICK_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.i(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    showImagePickerBottomSheet()
                }
            }
        }
    }

    private fun showImageRemovalApprovalDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.let {
                val builder: AlertDialog.Builder = AlertDialog.Builder(it)
                builder.apply {
                    setTitle("Confirmation")
                    setMessage("Are you sure to remove the Store logo?")
                    setCancelable(false)
                    setNegativeButton(getString(R.string.text_no)) { dialog, _ -> dialog.dismiss() }
                    setPositiveButton(getString(R.string.txt_yes)) { dialog, _ ->
                        dialog.dismiss()
                        onImageSelectionResultFile(null)
                    }
                }.create().show()
            }
        }
    }

    override fun onStoreLogoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            val photoResponse = Gson().fromJson<StoreResponse>(response.mCommonDataStr, StoreResponse::class.java)
            mStoreLogoLinkStr = photoResponse.storeInfo.logoImage
            if (mStoreLogoLinkStr?.isNotEmpty() == true) {
                profilePhotoImageView?.let {
                    try {
                        Picasso.get().load(mStoreLogoLinkStr).into(it)
                    } catch (e: Exception) {
                        Log.e(TAG, "picasso image loading issue: ${e.message}", e)
                    }
                }
            } else {
                StaticInstances.sIsStoreImageUploaded = false
                mActivity?.onBackPressed()
            }
        }
    }

    override fun onImageCDNLinkGenerateResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val photoResponse = Gson().fromJson<String>(response.mCommonDataStr, String::class.java)
            service.uploadStoreLogo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), StoreLogoRequest(photoResponse))
        }
    }

    override fun onProfilePhotoServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onImageSelectionResultFile(file: File?, mode: String) {
        if (mode == Constants.MODE_CROP) {
            val fragment = CropPhotoFragment.newInstance(file?.toUri())
            fragment.setTargetFragment(this, Constants.CROP_IMAGE_REQUEST_CODE)
            launchFragment(fragment, true)
            return
        }
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        if (file == null) {
            service.uploadStoreLogo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), StoreLogoRequest(""))
        } else {
            val fileRequestBody = MultipartBody.Part.createFormData("media", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
            val imageTypeRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), Constants.BASE64_STORE_LOGO)
            service.generateCDNLink(imageTypeRequestBody, fileRequestBody)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == Constants.CROP_IMAGE_REQUEST_CODE) {
            val file = data?.getSerializableExtra(Constants.MODE_CROP) as File
            CoroutineScopeUtils().runTaskOnCoroutineMain {
                onImageSelectionResultFile(file, "")
            }
        } else super.onActivityResult(requestCode, resultCode, data)
    }

}