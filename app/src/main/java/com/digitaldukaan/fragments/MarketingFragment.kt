package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.MarketingCardAdapter
import com.digitaldukaan.adapters.SharePDFAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.constants.openWebViewFragment
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.response.AppShareDataResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.MarketingCardsItemResponse
import com.digitaldukaan.models.response.ShareStorePDFDataResponse
import com.digitaldukaan.services.MarketingService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IMarketingServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.marketing_fragment.*
import okhttp3.ResponseBody


class MarketingFragment : BaseFragment(), IOnToolbarIconClick, IMarketingServiceInterface {

    companion object {
        private lateinit var service: MarketingService
        private var mShareStorePDFResponse: ShareStorePDFDataResponse? = null
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
        updateNavigationBarState(R.id.menuMarketing)
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
        WebViewBridge.mWebViewListener = this
    }

    override fun onToolbarSideIconClicked() = openWebViewFragment(this, getString(R.string.help), Constants.WEB_VIEW_HELP, Constants.SETTINGS)

    override fun onMarketingErrorResponse(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onMarketingResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val listType = object : TypeToken<List<MarketingCardsItemResponse>>() {}.type
                val list = Gson().fromJson<ArrayList<MarketingCardsItemResponse>>(response.mCommonDataStr, listType)
                marketingCardRecyclerView.apply {
                    val gridLayoutManager = GridLayoutManager(mActivity, 2)
                    layoutManager = gridLayoutManager
                    gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return if (list[position].type == Constants.SPAN_TYPE_FULL_WIDTH) 2 else 1
                        }
                    }
                    adapter = MarketingCardAdapter(list, this@MarketingFragment)
                }
            }

        }
    }

    override fun onAppShareDataResponse(response: AppShareDataResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            shareDataOnWhatsApp(response.mWhatsAppText)
        }
    }

    override fun onGenerateStorePdfResponse(response: ResponseBody) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
        }
    }

    override fun onShareStorePdfDataResponse(response: ShareStorePDFDataResponse) {
        mShareStorePDFResponse = response
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showPDFShareBottomSheet(mShareStorePDFResponse)
        }
    }

    override fun onMarketingItemClick(response: MarketingCardsItemResponse?) {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        when (response?.action) {
            Constants.ACTION_BUSINESS_CREATIVE -> openWebViewFragment(this, "", Constants.WEB_VIEW_CREATIVE_LIST, Constants.SETTINGS)
            Constants.ACTION_SOCIAL_CREATIVE -> openWebViewFragment(this, "", Constants.WEB_VIEW_SOCIAL_CREATIVE_LIST, Constants.SETTINGS)
            Constants.ACTION_QR_DOWNLOAD -> openWebViewFragment(this, "", Constants.WEB_VIEW_QR_DOWNLOAD, Constants.SETTINGS)
            Constants.ACTION_SHARE_DATA -> {
                showProgressDialog(mActivity)
                service.getShareStoreData(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
            }
            Constants.ACTION_CATALOG_WHATSAPP -> {
                if (mShareStorePDFResponse == null) {
                    showProgressDialog(mActivity)
                    service.getShareStorePdfText(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
                } else {
                    showPDFShareBottomSheet(mShareStorePDFResponse)
                }
            }
        }
    }

    private fun showPDFShareBottomSheet(response: ShareStorePDFDataResponse?) {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_refer_and_earn,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            view.run {
                val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                val bottomSheetUpperImageView: ImageView = findViewById(R.id.bottomSheetUpperImageView)
                val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                val verifyTextView: TextView = findViewById(R.id.verifyTextView)
                val referAndEarnRecyclerView: RecyclerView = findViewById(R.id.referAndEarnRecyclerView)
                //Picasso.get().load(R.drawable.ic_share_pdf_whatsapp).into(bottomSheetUpperImageView)
                bottomSheetUpperImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_share_pdf_whatsapp))
                bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                bottomSheetHeadingTextView.text = response?.mShareStorePDFDataItem?.heading
                verifyTextView.text = response?.mShareStorePDFDataItem?.subHeading
                verifyTextView.setOnClickListener{
                    showProgressDialog(mActivity)
                    service.generateStorePdf(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
                    bottomSheetDialog.dismiss()
                }
                referAndEarnRecyclerView.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    adapter = SharePDFAdapter(response?.mShareStorePDFDataItem?.howItWorks)
                }
            }
        }.show()
    }

    override fun onNativeBackPressed() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.onBackPressed()
        }
    }
}