package com.digitaldukaan.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.DeliveryTimeAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IChipItemClickListener
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.request.UpdateOrderRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.SendBillPhotoService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ISendBillPhotoServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
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
    private var mExtraChargeName: String? = ""
    private var mExtraCharge: Double = 0.0
    private var mDeliveryCharge: Double = 0.0
    private var mDiscount: Double = 0.0
    private var mPayAmount: Double? = 0.0
    private var mDeliveryTimeResponse: DeliveryTimeResponse? = null
    private val mService = SendBillPhotoService()

    companion object {
        private const val TAG = "SendBillPhotoFragment"
        private const val CUSTOM = "custom"

        fun newInstance(mainOrderDetailResponse: OrderDetailMainResponse?, file: File?, deliveryTimeStr: String?, extraChargeName: String?, extraCharge: Double, discount: Double, payAmount: Double?, deliveryChargesAmount: Double, deliveryTimeResponse: DeliveryTimeResponse?): SendBillPhotoFragment {
            val fragment = SendBillPhotoFragment()
            fragment.mSendPhotoStaticText = mainOrderDetailResponse?.staticText
            fragment.mImageFile = file
            fragment.mDeliveryTimeStr = deliveryTimeStr
            fragment.mExtraChargeName = extraChargeName
            fragment.mExtraCharge = extraCharge
            fragment.mDiscount = discount
            fragment.mPayAmount = payAmount
            fragment.mDeliveryCharge = deliveryChargesAmount
            fragment.mMainOrderDetailResponse = mainOrderDetailResponse
            fragment.mDeliveryTimeResponse = deliveryTimeResponse
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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
                Log.d(TAG, "onClick: orderDetailMainResponse?.sendBillAction :: ${mMainOrderDetailResponse?.sendBillAction}")
                if (Constants.ACTION_HOW_TO_SHIP == mMainOrderDetailResponse?.sendBillAction) {
                    showShipmentConfirmationBottomSheet(mMainOrderDetailResponse?.staticText, mMainOrderDetailResponse?.orders?.orderId)
                    return
                }
                initiateSendBillServerCall()
            }
        }
    }

    private fun initiateSendBillServerCall() {
        mImageFile?.run {
            val fileRequestBody = MultipartBody.Part.createFormData("media", name, RequestBody.create("image/*".toMediaTypeOrNull(), this))
            val imageTypeRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), Constants.BASE64_ORDER_BILL)
            showProgressDialog(mActivity)
            mService.generateCDNLink(imageTypeRequestBody, fileRequestBody)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            setHeaderTitle(mSendPhotoStaticText?.text_send_bill)
            setSideIconVisibility(true)
            onBackPressed(this@SendBillPhotoFragment)
            mActivity?.let {
                setSideIcon(ContextCompat.getDrawable(it, R.drawable.ic_refresh), object : IOnToolbarIconClick {
                    override fun onToolbarSideIconClicked() = openCameraWithoutCrop()
                })
            }
            setSecondSideIconVisibility(false)
        }
        sendBillTextView.text = mSendPhotoStaticText?.text_send_bill
        val image = BitmapFactory.decodeFile(mImageFile?.absolutePath)
        imageView?.setImageBitmap(image)
        val amountStr = "${mSendPhotoStaticText?.text_rupees_symbol} $mPayAmount"
        amountEditText?.text = amountStr
    }

    override fun onImageSelectionResultFile(file: File?, mode: String) {
        mImageFile = file
        val image = BitmapFactory.decodeFile(mImageFile?.absolutePath)
        imageView?.setImageBitmap(image)
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
                        cdnLink,
                        mExtraChargeName,
                        mExtraCharge,
                        mDiscount,
                        mPayAmount,
                        mDeliveryCharge
                    )
                    mService.updateOrder(request)
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
                shareOnWhatsApp(response?.whatsAppText)
                launchFragment(HomeFragment.newInstance(), true)
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onSendBillPhotoException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onShipmentCtaClicked(initiateServerCall: Boolean) = if (initiateServerCall) initiateSendBillServerCall() else showDeliveryTimeBottomSheet()

    private fun showDeliveryTimeBottomSheet() {
        mActivity?.run {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(this).inflate(
                R.layout.bottom_sheet_delivery_time,
                findViewById(R.id.bottomSheetContainer)
            )
            bottomSheetDialog.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
                view.run {
                    val deliveryTimeEditText: EditText = findViewById(R.id.deliveryTimeEditText)
                    val bottomSheetHeading: TextView = findViewById(R.id.bottomSheetHeading)
                    val bottomSheetSendBillText: TextView = findViewById(R.id.bottomSheetSendBillText)
                    val deliveryTimeRecyclerView: RecyclerView = findViewById(R.id.deliveryTimeRecyclerView)
                    mDeliveryTimeResponse?.staticText?.run {
                        bottomSheetHeading.text = heading_choose_delivery_time
                        bottomSheetSendBillText.text = text_send_bill
                        deliveryTimeRecyclerView.apply {
                            layoutManager = StaggeredGridLayoutManager(3, LinearLayoutManager.VERTICAL)
                            var deliveryTimeAdapter: DeliveryTimeAdapter? = null
                            deliveryTimeAdapter = DeliveryTimeAdapter(mDeliveryTimeResponse?.deliveryTimeList, object : IChipItemClickListener {

                                    override fun onChipItemClickListener(position: Int) {
                                        mDeliveryTimeResponse?.deliveryTimeList?.forEachIndexed { _, itemResponse ->
                                            itemResponse.isSelected = false
                                        }
                                        mDeliveryTimeResponse?.deliveryTimeList?.get(position)?.isSelected = true
                                        deliveryTimeEditText.apply {
                                            visibility = if (CUSTOM == mDeliveryTimeResponse?.deliveryTimeList?.get(position)?.key) View.VISIBLE else View.INVISIBLE
                                            if (View.VISIBLE == visibility) {
                                                requestFocus()
                                                showKeyboard()
                                            }
                                        }
                                        deliveryTimeAdapter?.setDeliveryTimeList(mDeliveryTimeResponse?.deliveryTimeList)
                                        mDeliveryTimeStr = mDeliveryTimeResponse?.deliveryTimeList?.get(position)?.value
                                        bottomSheetSendBillText.isEnabled = true
                                    }

                                })
                            adapter = deliveryTimeAdapter
                        }
                    }
                    bottomSheetSendBillText.setOnClickListener {
                        if (true == mDeliveryTimeStr?.equals(CUSTOM, true)) {
                            mDeliveryTimeStr = deliveryTimeEditText.text.toString()
                            if (true == mDeliveryTimeStr?.isEmpty()) {
                                deliveryTimeEditText.apply {
                                    error = getString(R.string.mandatory_field_message)
                                    requestFocus()
                                }
                                return@setOnClickListener
                            }
                        } else {
                            bottomSheetDialog.dismiss()
                            initiateSendBillServerCall()
                        }
                    }
                }
            }.show()
        }
    }

}