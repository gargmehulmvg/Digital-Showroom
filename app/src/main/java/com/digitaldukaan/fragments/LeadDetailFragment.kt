package com.digitaldukaan.fragments

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.LeadsDetailItemAdapter
import com.digitaldukaan.adapters.LeadsPromoCodeAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IPromoCodeItemClickListener
import com.digitaldukaan.models.request.AbandonedCartReminderRequest
import com.digitaldukaan.models.request.GetPromoCodeRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.LeadsDetailService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ILeadsDetailServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main2.backButtonToolbar
import kotlinx.android.synthetic.main.bottom_layout_share_offer.*
import kotlinx.android.synthetic.main.layout_lead_detail_fragment.*
import kotlinx.android.synthetic.main.layout_promo_code_page_info_fragment.*
import java.util.*

class LeadDetailFragment: BaseFragment(), ILeadsDetailServiceInterface,
    IPromoCodeItemClickListener {

    private var mService: LeadsDetailService? = null
    private var mLeadResponse: LeadsResponse? = null
    private var mSuccessDialogMessage = ""
    private var mSuccessDialogCtaText = ""
    private var mPromoCodePageNumber = 1
    private var mIsNextPage = false
    private var mShareText = ""
    private var mShareCdn = ""
    private var mLeadsOfferBottomSheetDialog: BottomSheetDialog? = null
    private var mLeadDetailPageInfoResponse: LeadDetailResponse? = null
    private var mPromoCodeList: ArrayList<PromoCodeListItemResponse> = ArrayList()

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
            shareOfferTextView?.id -> {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                showProgressDialog(mActivity)
                mService?.getAllMerchantPromoCodes(GetPromoCodeRequest(Constants.MODE_ACTIVE, mPromoCodePageNumber))
            }
        }
    }

    override fun onLeadsDetailException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onGetOrderCartByIdResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                mLeadDetailPageInfoResponse = Gson().fromJson(commonResponse.mCommonDataStr, LeadDetailResponse::class.java)
                setupUIFromResponse(mLeadDetailPageInfoResponse)
            }
        }
    }

    override fun onGetPromoCodeListResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val promoCodeListResponse = Gson().fromJson(commonResponse.mCommonDataStr, PromoCodeListResponse::class.java)
                mIsNextPage = promoCodeListResponse?.mIsNextPage ?: false
                if (1 == mPromoCodePageNumber) mPromoCodeList.clear()
                mPromoCodeList.addAll(promoCodeListResponse?.mPromoCodeList ?: ArrayList())
                if (isEmpty(mPromoCodeList)) {

                } else {
                    showOffersBottomSheet()
                }
            }
        }
    }

    override fun onPromoCodeShareResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                val promoCodeShareResponse = Gson().fromJson(commonResponse.mCommonDataStr, PromoCodeShareResponse::class.java)
                promoCodeShareResponse?.let { response ->
                    mShareText = response.mCouponText ?: ""
                    mShareCdn = response.mCouponCdn ?: ""
                    shareCouponsWithImage(mShareText, mShareCdn)
                }
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    private fun shareCouponsWithImage(str: String?, url: String?) {
        if (isEmpty(url)) return
        Picasso.get().load(url).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                stopProgress()
                Log.d(TAG, "onBitmapLoaded: called")
                try {
                    bitmap?.let { shareOnWhatsApp(str, bitmap) }
                } catch (e: Exception) {
                    Log.e(TAG, "onBitmapLoaded: ${e.message}", e)
                }
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                stopProgress()
                Log.d(TAG, "onPrepareLoad: ")
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                stopProgress()
                Log.d(TAG, "onBitmapFailed: ")
            }
        })
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
                        displayStr = displayStr + "${pageInfoResponse.deliveryInfo?.address1}" + ", "
                    if (isNotEmpty(pageInfoResponse.deliveryInfo?.address2))
                        displayStr = displayStr + "${pageInfoResponse.deliveryInfo?.address2}" + ", "
                    if (isNotEmpty(pageInfoResponse.deliveryInfo?.city))
                        displayStr = displayStr + "${pageInfoResponse.deliveryInfo?.city}" + ", "
                    if (isNotEmpty(pageInfoResponse.deliveryInfo?.pincode))
                        displayStr += "${pageInfoResponse.deliveryInfo?.city}"
                    if (isEmpty(displayStr))
                        displayStr = emptyStr
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
            var totalItemCount = 0
            pageInfoResponse?.orderDetailsItemsList?.forEachIndexed { _, orderDetailItemResponse -> totalItemCount += orderDetailItemResponse.quantity }
            totalItemsDetailTextView?.text = "$totalItemCount"
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

    private fun showOffersBottomSheet() {
        try {
            mActivity?.let {
                if (true == mLeadsOfferBottomSheetDialog?.isShowing) return
                mLeadsOfferBottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_leads_promo_code, it.findViewById(R.id.bottomSheetContainer))
                mLeadsOfferBottomSheetDialog?.apply {
                    setContentView(view)
                    view.run {
                        val offersTextView: TextView = findViewById(R.id.offersTextView)
                        val ctaTextView: TextView = findViewById(R.id.ctaTextView)
                        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
                        offersTextView.text = mLeadDetailPageInfoResponse?.staticText?.headingBottomSheetOffers
                        ctaTextView.apply {
                            text = mLeadDetailPageInfoResponse?.staticText?.textCtaBottomSheetCreateOffers
                            setOnClickListener { launchFragment(CustomCouponsFragment.newInstance(null), true) }
                        }
                        recyclerView.apply {
                            layoutManager = LinearLayoutManager(mActivity)
                            adapter = LeadsPromoCodeAdapter(mLeadDetailPageInfoResponse?.staticText, mPromoCodeList, this@LeadDetailFragment)
                        }
                    }
                }?.show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "showOffersBottomSheet: ${e.message}", e)
        }
    }

    override fun onPromoCodeDetailClickListener(position: Int) = Unit

    override fun onPromoCodeShareClickListener(position: Int) {
        if (position < 0 || position >= mPromoCodeList.size) return
        val item = mPromoCodeList[position]
        showProgressDialog(mActivity)
        mLeadsOfferBottomSheetDialog?.dismiss()
        mService?.shareCoupon(item.promoCode)
    }

}