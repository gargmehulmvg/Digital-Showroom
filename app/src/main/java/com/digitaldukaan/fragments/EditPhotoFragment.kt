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
import com.digitaldukaan.constants.getImageFileFromBitmap
import com.digitaldukaan.models.response.PremiumPageInfoStaticTextResponse
import kotlinx.android.synthetic.main.layout_edit_photo_fragment.*

class EditPhotoFragment: BaseFragment() {

    private var mStaticText: PremiumPageInfoStaticTextResponse? = null
    private var mFileUri: Uri? = null
    private var mMode: String? = null

    companion object {
        fun newInstance(uri: Uri?, mode: String, staticText: PremiumPageInfoStaticTextResponse?): EditPhotoFragment {
            val fragment = EditPhotoFragment()
            fragment.mFileUri = uri
            fragment.mMode = mode
            fragment.mStaticText = staticText
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

    override fun onClick(view: View?) {
        when (view?.id) {
            backButtonToolbar.id -> mActivity.onBackPressed()
            viewWebsiteImageView.id -> when(mMode) {
                Constants.EDIT_PHOTO_MODE_MOBILE -> {
                    val croppedImage = cropImageView.croppedImage
                    val croppedImageFile = getImageFileFromBitmap(croppedImage, mActivity)
                    showToast(croppedImageFile.name)
                    showMobileImageUploadDialog()
                }
                Constants.EDIT_PHOTO_MODE_DESKTOP -> showToast()
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