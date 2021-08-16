package com.digitaldukaan.fragments

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.adapters.PromoCodeAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IPromoCodeItemClickListener
import com.digitaldukaan.models.request.GetPromoCodeRequest
import com.digitaldukaan.models.request.UpdatePromoCodeRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.CustomCouponsService
import com.digitaldukaan.services.serviceinterface.IPromoCodePageInfoServiceInterface
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import io.sentry.Sentry
import kotlinx.android.synthetic.main.layout_promo_code_page_info_fragment.*


class PromoCodePageInfoFragment : BaseFragment(), IPromoCodePageInfoServiceInterface {

    companion object {
        private const val TAG = "CreateCouponsFragment"
        fun newInstance(): PromoCodePageInfoFragment = PromoCodePageInfoFragment()
    }

    private var mService = CustomCouponsService()
    private var couponsListRecyclerView: RecyclerView? = null
    private var zeroCouponLayout: View? = null
    private var couponsListLayout: View? = null
    private var inActiveBottomSelectedView: View? = null
    private var activeBottomSelectedView: View? = null
    private var mLayoutManager: LinearLayoutManager? = null
    private var mStaticText: PromoCodePageStaticTextResponse? = null
    private var mPromoCodeMode = Constants.MODE_ACTIVE
    private var mPromoCodePageNumber = 1
    private var mPromoCodeList: ArrayList<PromoCodeListItemResponse> = ArrayList()
    private var mAdapter: PromoCodeAdapter? = null
    private var mIsNextPage = false
    private var mShareText = ""
    private var mShareCdn = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_promo_code_page_info_fragment, container, false)
        hideBottomNavigationView(true)
        ToolBarManager.getInstance()?.hideToolBar(mActivity, true)
        mService.setPromoPageInfoServiceListener(this)
        showProgressDialog(mActivity)
        mPromoCodeMode = Constants.MODE_ACTIVE
        mService.getPromoCodePageInfo()
        return mContentView
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            backButtonToolbar?.id -> mActivity?.onBackPressed()
            createCouponsTextView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_CREATE_COUPON,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PATH to AFInAppEventParameterName.COUPON_LIST_SCREEN,
                        AFInAppEventParameterName.IS_ZERO_COUPON_SCREEN to "1"
                    )
                )
                launchFragment(CustomCouponsFragment.newInstance(mStaticText), true)
            }
            createCouponTextView?.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_CREATE_COUPON,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PATH to AFInAppEventParameterName.COUPON_LIST_SCREEN,
                        AFInAppEventParameterName.IS_ZERO_COUPON_SCREEN to "0"
                    )
                )
                launchFragment(CustomCouponsFragment.newInstance(mStaticText), true)
            }
            activeTextView?.id -> {
                if (Constants.MODE_ACTIVE == mPromoCodeMode && 1 == mPromoCodePageNumber) {
                    couponsListRecyclerView?.smoothSnapToPosition(0)
                } else {
                    mPromoCodePageNumber = 1
                    mPromoCodeMode = Constants.MODE_ACTIVE
                    mService.getAllMerchantPromoCodes(GetPromoCodeRequest(mPromoCodeMode, mPromoCodePageNumber))
                }
                activeBottomSelectedView?.visibility = View.VISIBLE
                inActiveBottomSelectedView?.visibility = View.GONE
                mActivity?.let { context ->
                    activeTextView?.setTextColor(ContextCompat.getColor(context, R.color.black))
                    inActiveTextView?.setTextColor(ContextCompat.getColor(context, R.color.default_text_light_grey))
                }
            }
            inActiveTextView?.id -> {
                if (Constants.MODE_INACTIVE == mPromoCodeMode && 1 == mPromoCodePageNumber) {
                    couponsListRecyclerView?.smoothSnapToPosition(0)
                } else {
                    mPromoCodePageNumber = 1
                    mPromoCodeMode = Constants.MODE_INACTIVE
                    mService.getAllMerchantPromoCodes(GetPromoCodeRequest(mPromoCodeMode, mPromoCodePageNumber))
                }
                activeBottomSelectedView?.visibility = View.GONE
                inActiveBottomSelectedView?.visibility = View.VISIBLE
                mActivity?.let { context ->
                    activeTextView?.setTextColor(ContextCompat.getColor(context, R.color.default_text_light_grey))
                    inActiveTextView?.setTextColor(ContextCompat.getColor(context, R.color.black))
                }
            }
        }
    }

    private fun onReloadPage() {
        showProgressDialog(mActivity)
        mService.getAllMerchantPromoCodes(GetPromoCodeRequest(mPromoCodeMode, mPromoCodePageNumber))
    }

    override fun onPromoCodePageInfoException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onPromoCodePageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val pageInfoResponse = Gson().fromJson<PromoCodePageInfoResponse>(response.mCommonDataStr, PromoCodePageInfoResponse::class.java)
                mStaticText = pageInfoResponse?.mStaticText
                zeroCouponLayout = mContentView?.findViewById(R.id.zeroCouponLayout)
                couponsListLayout = mContentView?.findViewById(R.id.couponsListLayout)
                if (pageInfoResponse?.mZeroPromoCodePageResponse?.mIsActiveFlagEnable == true) {
                    zeroCouponLayout?.visibility = View.VISIBLE
                    couponsListLayout?.visibility = View.GONE
                    val createCouponsTextView: TextView? = mContentView?.findViewById(R.id.createCouponsTextView)
                    val headingTextView: TextView? = mContentView?.findViewById(R.id.headingTextView)
                    val imageBackground: ImageView? = mContentView?.findViewById(R.id.imageBackground)
                    PrefsManager.storeStringDataInSharedPref(Constants.STORE_NAME, pageInfoResponse.mStoreName)
                    mStaticText?.let { staticText ->
                        headingTextView?.setHtmlData(staticText.heading_bold_create_and_share)
                        createCouponsTextView?.text = staticText.text_create_coupon
                        mActivity?.let { context -> imageBackground?.let { view -> Glide.with(context).load(pageInfoResponse.mZeroPromoCodePageResponse?.mCdnLink).into(view) } }
                    }
                } else {
                    mService.getAllMerchantPromoCodes(GetPromoCodeRequest(mPromoCodeMode, mPromoCodePageNumber))
                    couponsListLayout?.visibility = View.VISIBLE
                    zeroCouponLayout?.visibility = View.GONE
                    val activeTextView: TextView? = mContentView?.findViewById(R.id.activeTextView)
                    val inActiveTextView: TextView? = mContentView?.findViewById(R.id.inActiveTextView)
                    val heading: TextView? = mContentView?.findViewById(R.id.heading)
                    val createCouponTextView: TextView? = mContentView?.findViewById(R.id.createCouponTextView)
                    couponsListRecyclerView = mContentView?.findViewById(R.id.couponsListRecyclerView)
                    setupRecyclerView()
                    inActiveBottomSelectedView = mContentView?.findViewById(R.id.inActiveBottomSelectedView)
                    activeBottomSelectedView = mContentView?.findViewById(R.id.activeBottomSelectedView)
                    mStaticText?.let { staticText ->
                        createCouponTextView?.text = staticText.text_create_coupon
                        activeTextView?.text = staticText.active_text
                        inActiveTextView?.text = staticText.inactive_text
                        heading?.text = staticText.heading_discount_coupon
                    }
                }
            }
        }
    }

    override fun onPromoCodeDetailResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val promoCodeDetailResponse = Gson().fromJson<PromoCodeDetailResponse>(response.mCommonDataStr, PromoCodeDetailResponse::class.java)
                showPromoCouponDetailBottomSheet(promoCodeDetailResponse)
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onPromoCodeShareResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val promoCodeShareResponse = Gson().fromJson<PromoCodeShareResponse>(response.mCommonDataStr, PromoCodeShareResponse::class.java)
                promoCodeShareResponse?.let { response ->
                    mShareText = response.mCouponText ?: ""
                    mShareCdn = response.mCouponCdn ?: ""
                    shareCouponsWithImage(mShareText, mShareCdn)
                }
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
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
    
    private fun showPromoCouponDetailBottomSheet(promoCodeDetailResponse: PromoCodeDetailResponse?) {
        mActivity?.run {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_promo_coupon_detail, findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                setCancelable(false)
                view.run {
                    val salesGeneratedValueTextView: TextView = findViewById(R.id.salesGeneratedValueTextView)
                    val timeUsedValueTextView: TextView = findViewById(R.id.timeUsedValueTextView)
                    val checkBox: CheckBox = findViewById(R.id.setting2CheckBox)
                    val descriptionTextView: TextView = findViewById(R.id.descriptionTextView)
                    val minOrderValueTextView: TextView = findViewById(R.id.minOrderValueTextView)
                    val useCodeTextView: TextView = findViewById(R.id.useCodeTextView)
                    val setting2Message: TextView = findViewById(R.id.setting2Message)
                    val setting2Heading: TextView = findViewById(R.id.setting2Heading)
                    val setting1Heading: TextView = findViewById(R.id.setting1Heading)
                    val salesGeneratedHeadingTextView: TextView = findViewById(R.id.salesGeneratedHeadingTextView)
                    val timeUsedHeadingTextView: TextView = findViewById(R.id.timeUsedHeadingTextView)
                    val couponSettingHeadingTextView: TextView = findViewById(R.id.couponSettingHeadingTextView)
                    val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                    val activeCouponSwitch: SwitchMaterial = findViewById(R.id.activeCouponSwitch)
                    bottomSheetClose.setOnClickListener {
                        onReloadPage()
                        bottomSheetDialog.dismiss()
                    }
                    promoCodeDetailResponse?.let { response ->
                        couponSettingHeadingTextView.text = response.mStaticText?.text_coupon_settings
                        timeUsedHeadingTextView.text = response.mStaticText?.text_times_uesed
                        salesGeneratedHeadingTextView.text = response.mStaticText?.text_sales_generated
                        setting1Heading.text = response.mStaticText?.text_active_coupon
                        setting2Heading.text = response.mStaticText?.text_show_this_coupon_my_website
                        setting2Message.text = response.mStaticText?.message_allow_customer_see_coupon
                        timeUsedValueTextView.text = "${response.mAnalytics?.timesUsed?.toInt()}"
                        salesGeneratedValueTextView.text = "${response.mAnalytics?.salesGenerated}"
                        val promoCode = "${response.mStaticText?.text_use_code} ${response.mPromoCoupon?.promoCode}"
                        useCodeTextView.text = promoCode
                        val minOrderAmount =  "${mStaticText?.text_min_order_amount} ₹${response.mPromoCoupon?.minOrderPrice?.toInt()}"
                        minOrderValueTextView.text = minOrderAmount
                        activeCouponSwitch.isChecked = (Constants.MODE_PROMO_CODE_ACTIVE == response.mPromoCoupon?.status)
                        activeCouponSwitch.isSelected = (Constants.MODE_PROMO_CODE_ACTIVE == response.mPromoCoupon?.status)
                        val discountStr = if (Constants.MODE_COUPON_TYPE_FLAT == response.mPromoCoupon?.discountType) {
                            "${mStaticText?.text_flat} ₹${response.mPromoCoupon?.discount?.toInt()} ${mStaticText?.text_off_all_caps}"
                        } else {
                            "${response.mPromoCoupon?.discount?.toInt()}% ${mStaticText?.text_off_all_caps} ${mStaticText?.text_upto_capital} ₹${response.mPromoCoupon?.maxDiscount?.toInt()}"
                        }
                        descriptionTextView.text = discountStr
                        checkBox.isChecked = response.mPromoCoupon?.isWebsiteVisible ?: false
                        checkBox.setOnCheckedChangeListener { _, isChecked ->
                            showCancellableProgressDialog(mActivity)
                            CoroutineScopeUtils().runTaskOnCoroutineBackground {
                                try {
                                    val activeCouponResponse = RetrofitApi().getServerCallObject()?.updatePromoCodeStatus(
                                        UpdatePromoCodeRequest(
                                            promoCode = promoCodeDetailResponse.mPromoCoupon?.promoCode ?: "",
                                            status = if (activeCouponSwitch.isChecked) Constants.MODE_PROMO_CODE_ACTIVE else Constants.MODE_PROMO_CODE_DE_ACTIVE,
                                            isWebsiteVisible = isChecked
                                        )
                                    )
                                    activeCouponResponse?.let {
                                        if (it.isSuccessful) {
                                            it.body()?.let {
                                                stopProgress()
                                            }
                                        } else {
                                            stopProgress()
                                            val errorResponseBody = it.errorBody()
                                            errorResponseBody?.let {
                                                val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                                                showToast(errorResponse.mMessage)
                                                bottomSheetDialog.dismiss()
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    Sentry.captureException(e, "showPromoCouponDetailBottomSheet: exception")
                                    exceptionHandlingForAPIResponse(e)
                                }
                            }
                        }
                        activeCouponSwitch.setOnClickListener {
                            val isActive = activeCouponSwitch.isSelected
                            showCancellableProgressDialog(mActivity)
                            CoroutineScopeUtils().runTaskOnCoroutineBackground {
                                try {
                                    AppEventsManager.pushAppEvents(
                                        eventName = AFInAppEventType.EVENT_SHOW_COUPON,
                                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                        data = mapOf(
                                            AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                            AFInAppEventParameterName.COUPON_ID to promoCodeDetailResponse.mPromoCoupon?.promoCode,
                                            AFInAppEventParameterName.STATUS to if (isActive) "1" else "0",
                                            AFInAppEventParameterName.PATH to AFInAppEventParameterName.COUPON_DETAIL_SCREEN
                                        )
                                    )
                                    AppEventsManager.pushAppEvents(
                                        eventName = AFInAppEventType.EVENT_ACTIVE_COUPON,
                                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                        data = mapOf(
                                            AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                            AFInAppEventParameterName.COUPON_ID to promoCodeDetailResponse.mPromoCoupon?.promoCode,
                                            AFInAppEventParameterName.STATUS to if (isActive) "1" else "0"
                                        )
                                    )
                                    val activeCouponResponse = RetrofitApi().getServerCallObject()?.updatePromoCodeStatus(
                                            UpdatePromoCodeRequest(
                                                promoCode = promoCodeDetailResponse.mPromoCoupon?.promoCode ?: "",
                                                status = if (isActive) Constants.MODE_PROMO_CODE_DE_ACTIVE else Constants.MODE_PROMO_CODE_ACTIVE,
                                                isWebsiteVisible = checkBox.isChecked
                                            )
                                        )
                                    activeCouponResponse?.let {
                                        if (it.isSuccessful) {
                                            it.body()?.let {
                                                stopProgress()
                                            }
                                        } else {
                                            stopProgress()
                                            val errorResponseBody = it.errorBody()
                                            errorResponseBody?.let {
                                                val errorResponse = Gson().fromJson(errorResponseBody.string(), CommonApiResponse::class.java)
                                                showToast(errorResponse.mMessage)
                                            }
                                            bottomSheetDialog.dismiss()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Sentry.captureException(e, "showPromoCouponDetailBottomSheet: exception")
                                    exceptionHandlingForAPIResponse(e)
                                }
                            }
                        }
                    }
                }
            }.show()
        }
    }

    override fun onGetPromoCodeListResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val promoCodeListResponse = Gson().fromJson<PromoCodeListResponse>(response.mCommonDataStr, PromoCodeListResponse::class.java)
                mIsNextPage = promoCodeListResponse?.mIsNextPage ?: false
                if (1 == mPromoCodePageNumber)
                    mPromoCodeList.clear()
                mPromoCodeList.addAll(promoCodeListResponse?.mPromoCodeList ?: ArrayList())
                mAdapter?.setList(mPromoCodeList, mPromoCodeMode)
                if (isEmpty(mPromoCodeList)) {
                    zeroCouponAvailableLayout?.visibility = View.VISIBLE
                    zeroCouponAvailableTextView?.text = if (Constants.MODE_PROMO_CODE_ACTIVE == mPromoCodeMode) mStaticText?.text_no_coupons_are_active else mStaticText?.text_no_coupons_are_inactive
                } else zeroCouponAvailableLayout?.visibility = View.GONE
            }
        }
    }

    private fun setupRecyclerView() {
        mAdapter = PromoCodeAdapter(mStaticText, mActivity, mPromoCodeList, object : IPromoCodeItemClickListener {

            override fun onPromoCodeDetailClickListener(position: Int) {
                if (position < 0) return
                if (position >= mPromoCodeList.size) return
                val item = mPromoCodeList[position]
                showProgressDialog(mActivity)
                mService.getCouponDetails(item.promoCode)
            }

            override fun onPromoCodeShareClickListener(position: Int) {
                if (position < 0) return
                if (position >= mPromoCodeList.size) return
                val item = mPromoCodeList[position]
                showProgressDialog(mActivity)
                mService.shareCoupon(item.promoCode)
            }
        })
        couponsListRecyclerView?.apply {
            itemAnimator = DefaultItemAnimator()
            mLayoutManager = LinearLayoutManager(mActivity)
            layoutManager = mLayoutManager
            adapter = mAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (!recyclerView.canScrollVertically(1) && RecyclerView.SCROLL_STATE_IDLE == newState) {
                        if (mIsNextPage) {
                            mPromoCodePageNumber++
                            onReloadPage()
                        }
                    }
                }
            })

        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == Constants.STORAGE_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> Log.d(TAG, "User interaction was cancelled.")
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> shareCouponsWithImage(mShareText, mShareCdn)
                else -> showShortSnackBar("Permission was denied", true, R.drawable.ic_close_red)
            }
        }
    }
    
}