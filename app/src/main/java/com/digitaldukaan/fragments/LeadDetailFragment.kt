package com.digitaldukaan.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.LeadsDetailItemAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.request.AbandonedCartReminderRequest
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.LeadDetailResponse
import com.digitaldukaan.models.response.LeadsResponse
import com.digitaldukaan.services.LeadsDetailService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ILeadsDetailServiceInterface
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main2.backButtonToolbar
import kotlinx.android.synthetic.main.bottom_layout_share_offer.*
import kotlinx.android.synthetic.main.layout_lead_detail_fragment.*
import java.util.*


class LeadDetailFragment: BaseFragment(), ILeadsDetailServiceInterface {

    private var mService: LeadsDetailService? = null
    private var mLeadResponse: LeadsResponse? = null
    private var mSuccessDialogMessage = ""
    private var mSuccessDialogCtaText = ""

    companion object {

        private const val REMINDER_TYPE_SMS = 0
        private const val REMINDER_TYPE_WA  = 1
        private const val REMINDER_SENT_TO  = 0

        fun newInstance(item: LeadsResponse?): LeadDetailFragment {
            val fragment = LeadDetailFragment()
            fragment.mLeadResponse = item
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "LeadsDetailFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_lead_detail_fragment, container, false)
        mService = LeadsDetailService()
        mService?.setServiceListener(this)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.hideToolBar(mActivity, true)
        hideBottomNavigationView(true)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        mService?.getOrderCartById(mLeadResponse?.cartId ?: "")
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            backButtonToolbar?.id -> mActivity?.onBackPressed()
            phoneImageView?.id -> openDialer()
            okayTextView?.id -> {
                notificationContainer?.visibility = View.GONE
                PrefsManager.storeBoolDataInSharedPref(PrefsManager.KEY_ABANDONED_CART_OKAY_CLICKED, true)
            }
            whatsAppImageView?.id -> {
                val request = AbandonedCartReminderRequest(
                    cartId = mLeadResponse?.cartId,
                    customerPhone = mLeadResponse?.phoneNumber,
                    reminderType = REMINDER_TYPE_WA,
                    reminderSendTo = REMINDER_SENT_TO
                )
                showProgressDialog(mActivity)
                mService?.sendAbandonedCartReminder(request)
            }
            messageImageView?.id -> {
                val request = AbandonedCartReminderRequest(
                    cartId = mLeadResponse?.cartId,
                    customerPhone = mLeadResponse?.phoneNumber,
                    reminderType = REMINDER_TYPE_SMS,
                    reminderSendTo = REMINDER_SENT_TO
                )
                showProgressDialog(mActivity)
                mService?.sendAbandonedCartReminder(request)
            }
        }
    }

    override fun onLeadsDetailException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onGetOrderCartByIdResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val leadDetailPageInfoResponse = Gson().fromJson(commonResponse.mCommonDataStr, LeadDetailResponse::class.java)
                setupUIFromResponse(leadDetailPageInfoResponse)
            }
        }
    }

    override fun onSendAbandonedCartReminderResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val reminderResponse = Gson().fromJson(commonResponse.mCommonDataStr, String::class.java)
                if (isNotEmpty(reminderResponse)) {
                    shareDataOnWhatsAppByNumber(mLeadResponse?.phoneNumber, reminderResponse)
                } else {
                    showSmsSentDialog()
                }
            }
        }
    }

    private fun showSmsSentDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.let {
                val successDialog = Dialog(it)
                val view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_abandoned_success_reminder, null)
                successDialog.apply {
                    setContentView(view)
                    setCancelable(false)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    view?.run {
                        val updateTextView: TextView = findViewById(R.id.updateTextView)
                        val messageTextView: TextView = findViewById(R.id.messageTextView)
                        messageTextView.text = mSuccessDialogMessage
                        updateTextView.text = mSuccessDialogCtaText
                        updateTextView.setOnClickListener {
                            (this@apply).dismiss()
                        }
                    }
                }.show()
            }
        }
    }

    private fun setupUIFromResponse(pageInfoResponse: LeadDetailResponse?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            var displayStr: String
            val emptyStr = "       -"
            pageInfoResponse?.staticText?.let { static ->
                reminderTextView?.text = static.headingSendReminder
                itemTextView?.text = static.headingItems
                priceTextView?.text = static.headingPrice
                okayTextView?.text = static.textOkay
                mobileTextView?.text = static.textMobileNumber
                amountTextView?.text = static.textTotalCartAmount
                shareOfferHeadingTextView?.text = static.textFooterConnectWithCustomer
                shareOfferTextView?.text = static.textCtaShareOffer
                totalItemsTextView?.text = static.textTotalCartItems
                appTitleTextView?.text = static.headingCart
                itemTotalHeadingTextView?.text = static.textItemTotal
                deliveryChargeHeadingTextView?.text = static.textDeliveryCharge
                promoDiscountHeadingTextView?.text = static.textPromoAmount
                totalAmountHeadingTextView?.text = static.textTotalAmount
                mSuccessDialogCtaText = static.textOkay ?: ""
                mSuccessDialogMessage = static.textReminderSent ?: ""
                displayStr = "${static.textCartUpdatedOn} ${getDateStringForLeadsHeader(getDateFromOrderString(mLeadResponse?.lastUpdateOn) ?: Date())}"
                appSubTitleTextView?.text = displayStr
                deliveryTextView?.text = if (Constants.ORDER_TYPE_ADDRESS == pageInfoResponse.orderType) static.textDelivery else static.textPickup
                cartAbandonedTextView?.text = if (Constants.CART_TYPE_ABANDONED == pageInfoResponse.cartType) static.textCartAbandoned else static.textCartActive
                mActivity?.let { context ->
                    cartAbandonedTextView?.background = ContextCompat.getDrawable(context,if (Constants.CART_TYPE_ABANDONED == pageInfoResponse.cartType) R.drawable.curve_red_cart_abandoned_background else R.drawable.curve_blue_cart_active_background)
                    cartAbandonedTextView?.setTextColor(ContextCompat.getColor(context,if (Constants.CART_TYPE_ABANDONED == pageInfoResponse.cartType) R.color.leads_cart_abandoned_text_color else R.color.leads_cart_active_text_color))
                }
                if (Constants.ORDER_TYPE_ADDRESS == pageInfoResponse.orderType) {
                    addressDetailsLayout?.visibility = View.VISIBLE
                    addressHeadingTextView?.text = static.headingAddressDetails
                    nameMobileHeadingTextView?.text = static.textNameAndMobile
                    deliveryAddressHeadingTextView?.text = static.textDeliveryAddress
                    landmarkHeadingTextView?.text = static.textLandmark
                    cityPincodeHeadingTextView?.text = static.textCityAndPinCode
                    displayStr = when {
                        isEmpty(pageInfoResponse.deliveryInfo?.deliverTo) && isEmpty(pageInfoResponse.userPhone) -> emptyStr
                        isEmpty(pageInfoResponse.deliveryInfo?.deliverTo) -> "${pageInfoResponse.userPhone}"
                        isEmpty(pageInfoResponse.userPhone) -> "${pageInfoResponse.deliveryInfo?.deliverTo}"
                        else -> "${pageInfoResponse.deliveryInfo?.deliverTo} | ${pageInfoResponse.userPhone}"
                    }
                    nameMobileDetailTextView?.text = displayStr
                    displayStr = "${pageInfoResponse.deliveryInfo?.city} ${pageInfoResponse.deliveryInfo?.pincode}"
                    cityPincodeDetailTextView?.text = if (isEmpty(pageInfoResponse.deliveryInfo?.city) && isEmpty(pageInfoResponse.deliveryInfo?.pincode)) emptyStr else displayStr
                    displayStr = ""
                    if (isNotEmpty(pageInfoResponse.deliveryInfo?.address1))
                        displayStr =
                            displayStr + "${pageInfoResponse.deliveryInfo?.address1}" + ", "
                    if (isNotEmpty(pageInfoResponse.deliveryInfo?.address2))
                        displayStr =
                            displayStr + "${pageInfoResponse.deliveryInfo?.address2}" + ", "
                    if (isNotEmpty(pageInfoResponse.deliveryInfo?.city))
                        displayStr = displayStr + "${pageInfoResponse.deliveryInfo?.city}" + ", "
                    if (isNotEmpty(pageInfoResponse.deliveryInfo?.pincode))
                        displayStr += "${pageInfoResponse.deliveryInfo?.city}"
                    if (isEmpty(displayStr))
                        displayStr = "       -"
                    deliveryAddressDetailTextView?.text = displayStr
                    displayStr = "${pageInfoResponse.deliveryInfo?.landmark}"
                    landmarkDetailTextView?.text = if (isEmpty(displayStr)) emptyStr else displayStr
                } else addressDetailsLayout?.visibility = View.GONE
            }
            displayStr = "₹${pageInfoResponse?.itemsTotal}"
            itemTotalHeadingDetailTextView?.text = displayStr
            displayStr = "₹${pageInfoResponse?.deliveryCharge}"
            deliveryChargeHeadingDetailTextView?.text = displayStr
            displayStr = "-₹${pageInfoResponse?.storeOffer?.promoDiscount}"
            promoDiscountHeadingDetailTextView?.text = displayStr
            displayStr = "₹${pageInfoResponse?.payAmount}"
            amountDetailTextView?.text = displayStr
            totalAmountHeadingDetailTextView?.text = displayStr
            totalItemsDetailTextView?.text = "${pageInfoResponse?.orderDetailsItemsList?.size}"
            mobileDetailTextView?.text = "${pageInfoResponse?.userPhone}"
            cartDetailItemRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity)
                adapter = LeadsDetailItemAdapter(mActivity, pageInfoResponse?.staticText, pageInfoResponse?.orderDetailsItemsList)
            }
            notificationContainer?.visibility = if (!PrefsManager.getBoolDataFromSharedPref(PrefsManager.KEY_ABANDONED_CART_OKAY_CLICKED)) View.VISIBLE else View.GONE
        }
    }

    private fun openDialer() {
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = Uri.parse("tel:${mLeadResponse?.phoneNumber}")
        startActivity(intent)
    }

}