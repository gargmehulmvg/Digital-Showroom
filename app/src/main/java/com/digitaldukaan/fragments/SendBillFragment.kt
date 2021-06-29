package com.digitaldukaan.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.digitaldukaan.R
import com.digitaldukaan.constants.*
import com.digitaldukaan.network.RetrofitApi
import com.google.gson.Gson
import io.sentry.Sentry
import kotlinx.android.synthetic.main.layout_send_bill.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class SendBillFragment : BaseFragment() {

    private var mImageUri: Uri? = null
    private var mImageFile: File? = null
    private var mAmountStr: String? = ""
    private var mImageCdnLink: String = ""
    private var sendLinkTextView: TextView? = null

    companion object {
        private const val TAG = "SendBillFragment"
        fun newInstance(uri: Uri?, file: File?, amount: String): SendBillFragment {
            val fragment = SendBillFragment()
            fragment.mImageUri = uri
            fragment.mImageFile = file
            fragment.mAmountStr = amount
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_send_bill, container, false)
        hideBottomNavigationView(true)
        ToolBarManager.getInstance()?.apply { hideToolBar(mActivity, true) }
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadImageFromUri()
        val appTitleTextView: TextView? = mContentView?.findViewById(R.id.appTitleTextView)
        val customerCanPayUsingTextView: TextView? = mContentView?.findViewById(R.id.customerCanPayUsingTextView)
        val sendBillToCustomerTextView: TextView? = mContentView?.findViewById(R.id.sendBillToCustomerTextView)
        val bottomSheetClose: View? = mContentView?.findViewById(R.id.bottomSheetClose)
        val amountEditText: EditText? = mContentView?.findViewById(R.id.amountEditText)
        sendLinkTextView = mContentView?.findViewById(R.id.sendLinkTextView)
        bottomSheetClose?.visibility = View.GONE
        amountEditText?.setText(mAmountStr)
        val staticText = StaticInstances.sOrderPageInfoStaticData
        sendLinkTextView?.text = staticText?.text_send_link
        appTitleTextView?.text = staticText?.text_send_payment_link
        sendBillToCustomerTextView?.setHtmlData(staticText?.bottom_sheet_heading_send_link)
        customerCanPayUsingTextView?.setHtmlData(staticText?.bottom_sheet_message_customer_pay)
        mImageFile?.let { file -> uploadImageToGetCDNLink(file) }
    }

    override fun onResume() {
        super.onResume()
        stopProgress()
    }

    private fun loadImageFromUri() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mImageUri?.let {
                val bitmap = getBitmapFromUri(mImageUri, mActivity)
                imageView?.setImageBitmap(bitmap)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            refreshImageView?.id -> openCameraWithoutCrop()
            backButtonToolbar?.id -> mActivity?.onBackPressed()
            sendLinkTextView?.id -> showPaymentLinkSelectionDialog(mAmountStr ?: "0", mImageCdnLink)
        }
    }

    override fun onImageSelectionResultUri(fileUri: Uri?) {
        mImageUri = fileUri
        loadImageFromUri()
    }

    override fun onImageSelectionResultFile(file: File?, mode: String) {
        mImageFile = file
        loadImageFromUri()
    }

    private fun uploadImageToGetCDNLink(file: File) {
        showProgressDialog(mActivity)
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val fileRequestBody = MultipartBody.Part.createFormData("media", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
                val imageTypeRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), Constants.BASE64_THEMES)
                val response = RetrofitApi().getServerCallObject()?.getImageUploadCdnLink(imageTypeRequestBody, fileRequestBody)
                response?.let {
                    stopProgress()
                    if (response.isSuccessful) {
                        mImageCdnLink = Gson().fromJson<String>(response.body()?.mCommonDataStr, String::class.java)
                    }
                }
            } catch (e: Exception) {
                Sentry.captureException(e, "$TAG uploadImageToGetCDNLink: exception")
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

}