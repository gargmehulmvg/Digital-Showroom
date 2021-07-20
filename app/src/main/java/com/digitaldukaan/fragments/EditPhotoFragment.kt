package com.digitaldukaan.fragments

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.ImageUrlItemRequest
import com.digitaldukaan.models.request.StoreThemeBannerRequest
import com.digitaldukaan.models.response.PremiumPageInfoResponse
import com.digitaldukaan.models.response.PremiumPageInfoStaticTextResponse
import com.digitaldukaan.network.RetrofitApi
import com.google.gson.Gson
import io.sentry.Sentry
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
        private const val TAG = "EditPhotoFragment"
        fun newInstance(uri: Uri?, mode: String, staticText: PremiumPageInfoStaticTextResponse?, premiumPageInfoResponse: PremiumPageInfoResponse?): EditPhotoFragment {
            val fragment = EditPhotoFragment()
            fragment.mFileUri = uri
            fragment.mMode = mode
            fragment.mStaticText = staticText
            fragment.mPremiumPageInfoResponse = premiumPageInfoResponse
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_MOBILE_BANNER_CROPPED,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)
            )
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_edit_photo_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupUI()
    }

    private fun setupUI() {
        cropImageView?.setImageUriAsync(mFileUri)
        appSubTitleTextView?.text = getString(R.string.adjust_image_in_white_box)
        when (mMode) {
            Constants.EDIT_PHOTO_MODE_MOBILE -> {
                appTitleTextView?.text = mStaticText?.heading_crop_for_mobile_view
                val mobileObj = mPremiumPageInfoResponse?.theme?.themeComponent?.body?.get(1)?.images?.get(0)
                var factor = greatestCommonFactor(mobileObj?.width ?: 0, mobileObj?.height ?: 0)
                Log.d(TAG, "Constants.EDIT_PHOTO_MODE_MOBILE: factor: $factor width : ${mobileObj?.width} height : ${mobileObj?.height}")
                if (0 == factor) factor = 1
                val widthRatio = (mobileObj?.width ?: 0) / factor
                val heightRatio = (mobileObj?.height ?: 0) / factor
                Log.d(TAG, "Constants.EDIT_PHOTO_MODE_MOBILE: Aspect Ratio: $widthRatio : $heightRatio")
                cropImageView?.setAspectRatio(widthRatio, heightRatio)
            }
            Constants.EDIT_PHOTO_MODE_DESKTOP -> {
                val desktopObj = mPremiumPageInfoResponse?.theme?.themeComponent?.body?.get(1)?.images?.get(1)
                var factor = greatestCommonFactor(desktopObj?.width ?: 0, desktopObj?.height ?: 0)
                Log.d(TAG, "Constants.EDIT_PHOTO_MODE_DESKTOP: factor: $factor width : ${desktopObj?.width} height : ${desktopObj?.height}")
                if (0 == factor) factor = 1
                val widthRatio = (desktopObj?.width ?: 0) / factor
                val heightRatio = (desktopObj?.height ?: 0) / factor
                cropImageView?.setAspectRatio(widthRatio, heightRatio)
                appTitleTextView?.text = mStaticText?.heading_crop_for_desktop_view
                Log.d(TAG, "Constants.EDIT_PHOTO_MODE_DESKTOP: Aspect Ratio: $widthRatio : $heightRatio")
                cropImageView?.setAspectRatio(widthRatio, heightRatio)
            }
        }
    }

    private fun uploadImageToGetCDNLink(file: File, mode: String) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val fileRequestBody = MultipartBody.Part.createFormData("media", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
                val imageTypeRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), Constants.BASE64_THEMES)
                val response = RetrofitApi().getServerCallObject()?.getImageUploadCdnLink(imageTypeRequestBody, fileRequestBody)
                response?.let {
                    if (response.isSuccessful) {
                        val base64Str = Gson().fromJson<String>(response.body()?.mCommonDataStr, String::class.java)
                        if (mode == Constants.EDIT_PHOTO_MODE_MOBILE) mMobileCdnLink = base64Str else mDesktopCdnLink = base64Str
                        if (mode == Constants.EDIT_PHOTO_MODE_DESKTOP) {
                            AppEventsManager.pushAppEvents(
                                eventName = AFInAppEventType.EVENT_DESKTOP_BANNER_CROPPED,
                                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                data = mapOf(
                                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)
                                )
                            )
                            onDesktopCDNLinkGenerated()
                        }
                    }
                }
            } catch (e: Exception) {
                Sentry.captureException(e, "$TAG uploadImageToGetCDNLink: exception")
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

    private fun onDesktopCDNLinkGenerated() {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_SAVE_THEME_IMAGE,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)
            )
        )
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
            try {
                val request = StoreThemeBannerRequest(mPremiumPageInfoResponse?.theme?.storeThemeId, imageUrlItems)
                val response = RetrofitApi().getServerCallObject()?.setStoreThemeBanner(request)
                response?.let {
                    CoroutineScopeUtils().runTaskOnCoroutineMain {
                        stopProgress()
                        if (response.isSuccessful) {
                            if (response.body()?.mIsSuccessStatus == true) {
                                showShortSnackBar(response.body()?.mMessage, true, R.drawable.ic_green_check_small)
                                mActivity?.onBackPressed()
                            }
                            else
                                showShortSnackBar(response.body()?.mMessage, true, R.drawable.ic_close_red)
                        }
                    }
                }
            } catch (e: Exception) {
                Sentry.captureException(e, "$TAG onDesktopCDNLinkGenerated: exception")
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            backButtonToolbar.id -> mActivity?.onBackPressed()
            viewWebsiteImageView.id -> when(mMode) {
                Constants.EDIT_PHOTO_MODE_MOBILE -> {
                    val croppedImage = cropImageView.croppedImage
                    val croppedImageFile = getImageFileFromBitmap(croppedImage, mActivity)
                    croppedImageFile?.let {
                        uploadImageToGetCDNLink(it, Constants.EDIT_PHOTO_MODE_MOBILE)
                        showMobileImageUploadDialog()
                    }
                }
                Constants.EDIT_PHOTO_MODE_DESKTOP -> {
                    val croppedImage = cropImageView.croppedImage
                    val croppedImageFile = getImageFileFromBitmap(croppedImage, mActivity)
                    showProgressDialog(mActivity)
                    croppedImageFile?.let { uploadImageToGetCDNLink(it, Constants.EDIT_PHOTO_MODE_DESKTOP) }
                }
            }
        }
    }

    private fun showMobileImageUploadDialog() {
        mActivity?.let {
            val view = LayoutInflater.from(it).inflate(R.layout.dialog_mobile_view_image_upload, null)
            val dialog = Dialog(it, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
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
                        mActivity?.onBackPressed()
                    }
                }
            }.show()
        }
    }
}