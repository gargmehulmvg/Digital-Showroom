package com.digitaldukaan.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.request.UpdateOrderRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.OrderDetailMainResponse
import com.digitaldukaan.models.response.OrderDetailsStaticTextResponse
import com.digitaldukaan.models.response.UpdateOrderResponse
import com.digitaldukaan.services.SendBillPhotoService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ISendBillPhotoServiceInterface
import com.google.gson.Gson
import kotlinx.android.synthetic.main.send_bill_photo_fragment.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class SendBillPhotoFragment: BaseFragment(), ISendBillPhotoServiceInterface {

    private var mSendPhotoStaticText: OrderDetailsStaticTextResponse? = null
    private var mMainOrderDetailResponse: OrderDetailMainResponse? = null
    private var mImageFile: File? = null
    private var mDeliveryTimeStr: String? = ""
    private val mService = SendBillPhotoService()

    companion object {
        fun newInstance(mainOrderDetailResponse: OrderDetailMainResponse?, file: File?, deliveryTimeStr: String?): SendBillPhotoFragment{
            val fragment = SendBillPhotoFragment()
            fragment.mSendPhotoStaticText = mainOrderDetailResponse?.staticText
            fragment.mImageFile = file
            fragment.mDeliveryTimeStr = deliveryTimeStr
            fragment.mMainOrderDetailResponse = mainOrderDetailResponse
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mService.setServiceListener(this)
        mContentView = inflater.inflate(R.layout.send_bill_photo_fragment, container, false)
        return mContentView
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            addProductContainer.id -> {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                mImageFile?.run {
                    val fileRequestBody = MultipartBody.Part.createFormData("media", name, RequestBody.create("image/*".toMediaTypeOrNull(), this))
                    val imageTypeRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), Constants.BASE64_ORDER_BILL)
                    showProgressDialog(mActivity)
                    mService.generateCDNLink(imageTypeRequestBody, fileRequestBody)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            setHeaderTitle(mSendPhotoStaticText?.text_send_bill)
            setSideIconVisibility(true)
            setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_refresh), object : IOnToolbarIconClick{
                override fun onToolbarSideIconClicked() {
                    openFullCamera()
                }
            })
            setSecondSideIconVisibility(false)
        }
        sendBillTextView.text = mSendPhotoStaticText?.text_send_bill
        val image = BitmapFactory.decodeFile(mImageFile?.absolutePath)
        imageView.setImageBitmap(image)
        amountEditText.text = "${mSendPhotoStaticText?.text_rupees_symbol} ${mMainOrderDetailResponse?.orders?.amount}"
    }

    override fun onImageSelectionResultFile(file: File?, mode: String) {
        mImageFile = file
        val image = BitmapFactory.decodeFile(mImageFile?.absolutePath)
        imageView.setImageBitmap(image)
    }

    override fun onConvertFileToLinkResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                val cdnLink = Gson().fromJson<String>(commonResponse.mCommonDataStr, String::class.java)
                mMainOrderDetailResponse?.orders?.run {
                    val request = UpdateOrderRequest(
                        orderId,
                        amount,
                        false,
                        deliveryInfo?.deliveryTo,
                        deliveryInfo?.deliveryFrom,
                        mDeliveryTimeStr,
                        orderDetailsItemsList,
                        cdnLink
                    )
                    mService.updateOrder(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
                }
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onUpdateOrderResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_green_check_small)
                val response = Gson().fromJson<UpdateOrderResponse>(commonResponse.mCommonDataStr, UpdateOrderResponse::class.java)
                shareDataOnWhatsApp(response?.whatsAppText)
                launchFragment(HomeFragment.newInstance(), true)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onSendBillPhotoException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

}