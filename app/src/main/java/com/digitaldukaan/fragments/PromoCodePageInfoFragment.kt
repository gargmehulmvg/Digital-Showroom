package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.adapters.PromoCodeAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.PrefsManager
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.request.GetPromoCodeRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.CustomCouponsService
import com.digitaldukaan.services.serviceinterface.IPromoCodePageInfoServiceInterface
import com.google.gson.Gson
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_promo_code_page_info_fragment, container, false)
        hideBottomNavigationView(true)
        ToolBarManager.getInstance()?.hideToolBar(mActivity, true)
        mService.setPromoPageInfoServiceListener(this)
        showProgressDialog(mActivity)
        mService.getPromoCodePageInfo()
        return mContentView
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            backButtonToolbar?.id -> mActivity?.onBackPressed()
            createCouponsTextView?.id -> launchFragment(CustomCouponsFragment.newInstance(mStaticText), true)
            createCouponTextView?.id -> launchFragment(CustomCouponsFragment.newInstance(mStaticText), true)
            activeTextView?.id -> {
                if (Constants.MODE_ACTIVE == mPromoCodeMode && 1 == mPromoCodePageNumber) {
                    // TODO
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
                    // TODO
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
                        headingTextView?.setHtmlData(staticText.heading_create_and_share)
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

    override fun onGetPromoCodeListResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val promoCodeListResponse = Gson().fromJson<PromoCodeListResponse>(response.mCommonDataStr, PromoCodeListResponse::class.java)
                if (1 == mPromoCodePageNumber)
                    mPromoCodeList.clear()
                mPromoCodeList.addAll(promoCodeListResponse?.mPromoCodeList ?: ArrayList())
                couponsListRecyclerView?.apply {
                    mLayoutManager = LinearLayoutManager(mActivity)
                    layoutManager = mLayoutManager
                    adapter = PromoCodeAdapter(mStaticText, mActivity, mPromoCodeList, null)
                }
            }
        }
    }

}