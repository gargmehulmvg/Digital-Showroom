package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.PrefsManager
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.PromoCodePageInfoResponse
import com.digitaldukaan.models.response.PromoCodePageStaticTextResponse
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
    private var zeroCouponLayout: View? = null
    private var mStaticText: PromoCodePageStaticTextResponse? = null

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
        }
    }

    override fun onPromoCodePageInfoException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onPromoCodePageInfoResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val pageInfoResponse = Gson().fromJson<PromoCodePageInfoResponse>(response.mCommonDataStr, PromoCodePageInfoResponse::class.java)
                mStaticText = pageInfoResponse?.mStaticText
                if (pageInfoResponse?.mZeroPromoCodePageResponse?.mIsActiveFlagEnable == true) {
                    zeroCouponLayout = mContentView?.findViewById(R.id.zeroCouponLayout)
                    val createCouponsTextView: TextView? = mContentView?.findViewById(R.id.createCouponsTextView)
                    val headingTextView: TextView? = mContentView?.findViewById(R.id.headingTextView)
                    val imageBackground: ImageView? = mContentView?.findViewById(R.id.imageBackground)
                    zeroCouponLayout?.visibility = View.VISIBLE
                    PrefsManager.storeStringDataInSharedPref(Constants.STORE_NAME, pageInfoResponse.mStoreName)
                    mStaticText?.let { staticText ->
                        headingTextView?.setHtmlData(staticText.heading_create_and_share)
                        createCouponsTextView?.text = staticText.text_create_coupon
                        mActivity?.let { context -> imageBackground?.let { view -> Glide.with(context).load(pageInfoResponse.mZeroPromoCodePageResponse?.mCdnLink).into(view) } }
                    }
                }
            }
        }
    }

}