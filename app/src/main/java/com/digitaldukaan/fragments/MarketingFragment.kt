package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.GridLayoutManager.SpanSizeLookup
import com.digitaldukaan.R
import com.digitaldukaan.adapters.MarketingCardAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.Constants.Companion.SPAN_TYPE_FULL_WIDTH
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.constants.openWebViewFragment
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.request.StoreLogoRequest
import com.digitaldukaan.models.response.AppShareDataResponse
import com.digitaldukaan.models.response.MarketingCardsItemResponse
import com.digitaldukaan.models.response.MarketingCardsResponse
import com.digitaldukaan.services.MarketingService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IMarketingServiceInterface
import kotlinx.android.synthetic.main.marketing_fragment.*

class MarketingFragment : BaseFragment(), IOnToolbarIconClick, IMarketingServiceInterface {

    companion object {
        private lateinit var service: MarketingService
        private val mMarketingStaticData = mStaticData.mStaticData.mMarketingStaticData

        fun newInstance(): MarketingFragment {
            return MarketingFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        service = MarketingService()
        service.setMarketingServiceListener(this)
        mContentView = inflater.inflate(R.layout.marketing_fragment, container, false)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            setHeaderTitle(mMarketingStaticData.pageHeading)
            setSideIconVisibility(true)
            hideBackPressFromToolBar(mActivity, false)
            onBackPressed(this@MarketingFragment)
            setSideIcon(
                ContextCompat.getDrawable(mActivity, R.drawable.ic_setting_toolbar),
                this@MarketingFragment
            )
        }
        showBottomNavigationView(false)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        service.getMarketingCardsData()
    }

    override fun onToolbarSideIconClicked() = openWebViewFragment(this, getString(R.string.help), Constants.WEB_VIEW_HELP, Constants.SETTINGS)

    override fun onMarketingErrorResponse(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onMarketingResponse(response: MarketingCardsResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            marketingCardRecyclerView.apply {
                val list = response.mMarketingItemList
                val gridLayoutManager = GridLayoutManager(mActivity, 2)
                layoutManager = gridLayoutManager
                gridLayoutManager.spanSizeLookup = object : SpanSizeLookup() {
                    override fun getSpanSize(position: Int): Int {
                        return if (list[position].type == SPAN_TYPE_FULL_WIDTH) 2 else 1
                    }
                }
                adapter = MarketingCardAdapter(list, this@MarketingFragment)
            }
        }
    }

    override fun onAppShareDataResponse(response: AppShareDataResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            shareDataOnWhatsApp(response.mWhatsAppText)
        }
    }

    override fun onMarketingItemClick(response: MarketingCardsItemResponse?) {
        showToast(response?.text)
        when (response?.action) {
            Constants.ACTION_BUSINESS_CREATIVE -> openWebViewFragment(this, "", Constants.WEB_VIEW_CREATIVE_LIST, Constants.SETTINGS)
            Constants.ACTION_SOCIAL_CREATIVE -> openWebViewFragment(this, "", Constants.WEB_VIEW_SOCIAL_CREATIVE_LIST, Constants.SETTINGS)
            Constants.ACTION_QR_DOWNLOAD -> openWebViewFragment(this, "", Constants.WEB_VIEW_QR_DOWNLOAD, Constants.SETTINGS)
            Constants.ACTION_SHARE_DATA -> {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                showProgressDialog(mActivity)
                val request = StoreLogoRequest(getStringDataFromSharedPref(Constants.STORE_ID).toInt(), "")
                service.getShareStoreData(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
            }
        }
    }
}