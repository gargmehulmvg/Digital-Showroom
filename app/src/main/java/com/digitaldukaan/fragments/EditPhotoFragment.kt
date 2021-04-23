package com.digitaldukaan.fragments

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.getImageFileFromBitmap
import com.digitaldukaan.models.request.ImageUrlItemRequest
import com.digitaldukaan.models.request.StoreThemeBannerRequest
import com.digitaldukaan.models.response.PremiumPageInfoResponse
import com.digitaldukaan.models.response.PremiumPageInfoStaticTextResponse
import com.digitaldukaan.network.RetrofitApi
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_edit_photo_fragment.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class EditPhotoFragment: BaseFragment() {

    private var mStaticText: PremiumPageInfoStaticTextResponse? = null
    private var mFileUri: Uri? = null
    private var mMode: String? = null
    private var mMobileCdnLink = ""
    private var mDesktopCdnLink = ""
    private var mPremiumPageInfoResponse: PremiumPageInfoResponse? = null

    companion object {
        fun newInstance(uri: Uri?, mode: String, staticText: PremiumPageInfoStaticTextResponse?, premiumPageInfoResponse: PremiumPageInfoResponse?): EditPhotoFragment {
            val fragment = EditPhotoFragment()
            fragment.mFileUri = uri
            fragment.mMode = mode
            fragment.mStaticText = staticText
            fragment.mPremiumPageInfoResponse = premiumPageInfoResponse
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_edit_photo_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUI()
    }

    private fun setupUI() {
        cropImageView.setImageUriAsync(mFileUri)
        appSubTitleTextView.text = "Adjust the image in the white box"
        when (mMode) {
            Constants.EDIT_PHOTO_MODE_MOBILE -> {
                appTitleTextView.text = mStaticText?.heading_crop_for_mobile_view
                cropImageView.setAspectRatio(1, 1)
            }
            Constants.EDIT_PHOTO_MODE_DESKTOP -> {
                appTitleTextView.text = mStaticText?.heading_crop_for_desktop_view
                cropImageView.setAspectRatio(2, 1)
            }
        }
    }

    private fun uploadImageToGetCDNLink(file: File, mode: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            val fileRequestBody = MultipartBody.Part.createFormData("media", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
            val imageTypeRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), Constants.BASE64_THEMES)
            val response = RetrofitApi().getServerCallObject()?.getImageUploadCdnLink(imageTypeRequestBody, fileRequestBody)
            response?.let {
                if (response.isSuccessful) {
                    val base64Str = Gson().fromJson<String>(response.body()?.mCommonDataStr, String::class.java)
                    if (mode == Constants.EDIT_PHOTO_MODE_MOBILE) mMobileCdnLink = base64Str else mDesktopCdnLink = base64Str
                    if (mode == Constants.EDIT_PHOTO_MODE_DESKTOP) onDesktopCDNLinkGenerated()
                }
            }
        }
    }

    private fun onDesktopCDNLinkGenerated() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            val imageUrlItems: ArrayList<ImageUrlItemRequest>? = ArrayList()
            mPremiumPageInfoResponse?.theme?.themeComponent?.body?.get(1)?.images?.forEachIndexed { position, themeBodyResponse ->
                if (position == 0) {
                    val request = ImageUrlItemRequest(themeBodyResponse.id, mMobileCdnLink)
                    imageUrlItems?.add(request)
                } else {
                    val request = ImageUrlItemRequest(themeBodyResponse.id, mDesktopCdnLink)
                    imageUrlItems?.add(request)
                }
            }
            val request = StoreThemeBannerRequest(mPremiumPageInfoResponse?.theme?.storeThemeId, imageUrlItems)
            val response = RetrofitApi().getServerCallObject()?.setStoreThemeBanner(request)
            response?.let {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    stopProgress()
                    if (response.isSuccessful) {
                        if (response.body()?.mIsSuccessStatus == true) {
                            showShortSnackBar(response.body()?.mMessage, true, R.drawable.ic_green_check_small)
                            launchFragment(HomeFragment.newInstance(), true)
                        }
                        else
                            showShortSnackBar(response.body()?.mMessage, true, R.drawable.ic_close_red)
                    }
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            backButtonToolbar.id -> mActivity.onBackPressed()
            viewWebsiteImageView.id -> when(mMode) {
                Constants.EDIT_PHOTO_MODE_MOBILE -> {
                    val croppedImage = cropImageView.croppedImage
                    val croppedImageFile = getImageFileFromBitmap(croppedImage, mActivity)
                    uploadImageToGetCDNLink(croppedImageFile, Constants.EDIT_PHOTO_MODE_MOBILE)
                    showMobileImageUploadDialog()
                }
                Constants.EDIT_PHOTO_MODE_DESKTOP -> {
                    val croppedImage = cropImageView.croppedImage
                    val croppedImageFile = getImageFileFromBitmap(croppedImage, mActivity)
                    showProgressDialog(mActivity)
                    uploadImageToGetCDNLink(croppedImageFile, Constants.EDIT_PHOTO_MODE_DESKTOP)
                }
            }
        }
    }

    private fun showMobileImageUploadDialog() {
        val view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_mobile_view_image_upload, null)
        val dialog: Dialog = Dialog(mActivity, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.apply {
            setContentView(view)
            setCancelable(false)
            view?.run {
                val cropTextView: TextView = findViewById(R.id.cropTextView)
                val fadedMobileTextView: TextView = findViewById(R.id.fadedMobileTextView)
                val desktopTextView: TextView = findViewById(R.id.desktopTextView)
                val backButtonToolbar: View = findViewById(R.id.backButtonToolbar)
                fadedMobileTextView.text = mStaticText?.text_cropped_for_mobile_website
                desktopTextView.text = mStaticText?.text_lets_crop_for_desktop_website
                cropTextView.text = mStaticText?.text_crop_for_desktop
                cropTextView.setOnClickListener {
                    dialog.dismiss()
                    mMode = Constants.EDIT_PHOTO_MODE_DESKTOP
                    setupUI()
                }
                backButtonToolbar.setOnClickListener {
                    dialog.dismiss()
                    mActivity.onBackPressed()
                }
            }
        }.show()
    }
}