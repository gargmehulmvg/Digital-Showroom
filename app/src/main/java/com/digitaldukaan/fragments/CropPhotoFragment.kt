package com.digitaldukaan.fragments

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.constants.getImageFileFromBitmap
import kotlinx.android.synthetic.main.layout_crop_photo.*
import kotlinx.android.synthetic.main.layout_edit_photo_fragment.cropImageView

class CropPhotoFragment: BaseFragment() {

    private var mFileUri: Uri? = null

    companion object {
        fun newInstance(fileUri: Uri?): CropPhotoFragment {
            val fragment = CropPhotoFragment()
            fragment.mFileUri = fileUri
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "CropPhotoFragment"
        mContentView = inflater.inflate(R.layout.layout_crop_photo, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, true)
        }
        hideBottomNavigationView(true)
        cropImageView?.setAspectRatio(1, 1)
        cropImageView?.setFixedAspectRatio(true)
        cropImageView?.isAutoZoomEnabled = true
        cropImageView?.setMaxCropResultSize(2040, 2040)
        cropImageView?.setImageUriAsync(mFileUri)
        doneImageView?.setOnClickListener {
            try {
                val fragment = targetFragment as BaseFragment
                val croppedImage = cropImageView.croppedImage
                val croppedImageFile = getImageFileFromBitmap(croppedImage, mActivity)
                croppedImageFile?.let {
                    fragment.onActivityResult(Constants.CROP_IMAGE_REQUEST_CODE, Constants.CROP_IMAGE_REQUEST_CODE, Intent().putExtra(Constants.MODE_CROP, it))
                    fragmentManager?.popBackStack()
                }
            } catch (e: Exception) {
                Log.e(TAG, "onViewCreated: ${e.message}", e)
            }
        }
    }
}