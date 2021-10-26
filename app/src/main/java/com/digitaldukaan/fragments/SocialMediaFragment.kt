package com.digitaldukaan.fragments

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.adapters.SocialMediaCategoryAdapter
import com.digitaldukaan.adapters.SocialMediaTemplateAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.interfaces.ISocialMediaTemplateItemClickListener
import com.digitaldukaan.models.request.SocialMediaTemplateFavouriteRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.SocialMediaService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.ISocialMediaServiceInterface
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_edit_social_media_template_fragment.*
import kotlinx.android.synthetic.main.layout_home_fragment.*
import kotlinx.android.synthetic.main.layout_social_media.*
import kotlinx.android.synthetic.main.layout_social_media.screenshotContainer

class SocialMediaFragment : BaseFragment(), ISocialMediaServiceInterface, IOnToolbarIconClick,
    ISocialMediaTemplateItemClickListener {

    private var mService: SocialMediaService? = null
    private var mSocialMediaCategoryAdapter: SocialMediaCategoryAdapter? = null
    private var mSocialMediaTemplateAdapter: SocialMediaTemplateAdapter? = null
    private var mSocialMediaTemplateList: ArrayList<SocialMediaTemplateListItemResponse?>? = ArrayList()
    private var mPageNumber: Int = 1
    private var mSelectedCategoryId: String = "0"
    private var mFavoriteCategoryId: Int = 1
    private var mIsUnFavoriteTemplateClicked: Boolean = false
    private var mIsNextPage: Boolean = false
    private var mMarketingPageInfoResponse: MarketingPageInfoResponse? = null

    companion object {
        private const val TAG = "SocialMediaFragment"
        private var sSocialMediaPageInfoResponse: SocialMediaPageInfoResponse? = null
        private var sSocialMediaCategoriesList: ArrayList<SocialMediaCategoryItemResponse?>? = ArrayList()

        fun newInstance(marketingPageInfoResponse: MarketingPageInfoResponse?): SocialMediaFragment {
            val fragment =  SocialMediaFragment()
            fragment.mMarketingPageInfoResponse = marketingPageInfoResponse
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_social_media, container, false)
        hideBottomNavigationView(true)
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            setSideIconVisibility(false)
            onBackPressed(this@SocialMediaFragment)
        }
        mService = SocialMediaService()
        mService?.setSocialMediaServiceInterface(this)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        if (null == sSocialMediaPageInfoResponse) {
            showProgressDialog(mActivity)
            mService?.getSocialMediaPageInfo()
        } else {
            setupSocialMediaUIFromResponse()
        }
        Log.d(TAG, "onViewCreated: called :: page number :: $mPageNumber :: Category Id :: $mSelectedCategoryId")
        mService?.getSocialMediaTemplateList(mSelectedCategoryId, mPageNumber)
        templateRecyclerView?.apply {
            setHasFixedSize(true)
            mSocialMediaTemplateAdapter = SocialMediaTemplateAdapter(
                this@SocialMediaFragment,
                null,
                this@SocialMediaFragment
            )
            layoutManager = LinearLayoutManager(mActivity)
            adapter = mSocialMediaTemplateAdapter
        }
        scrollingLayout?.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, _, scrollY, _, _ ->
            screenshotOuterContainer?.visibility = View.GONE
            if (scrollY == (v.getChildAt(0).measuredHeight - v.measuredHeight) && mIsNextPage) {
                Log.d(TAG, "onViewCreated: scrollingLayout called :: mIsNextPage :: $mIsNextPage :: page number :: $mPageNumber :: Category Id :: $mSelectedCategoryId")
                mPageNumber++
                mService?.getSocialMediaTemplateList(mSelectedCategoryId, mPageNumber)
            }
        })
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            showMoreCategoryTextView?.id -> {
                if (showMoreCategoryTextView?.text == sSocialMediaPageInfoResponse?.socialMediaStaticTextResponse?.text_show_more_categories) {
                    showMoreCategoryTextView?.text = sSocialMediaPageInfoResponse?.socialMediaStaticTextResponse?.text_hide
                    mSocialMediaCategoryAdapter?.setListSize(sSocialMediaPageInfoResponse?.socialMediaCategoriesList?.size ?: 0)
                } else {
                    showMoreCategoryTextView?.text = sSocialMediaPageInfoResponse?.socialMediaStaticTextResponse?.text_show_more_categories
                    mSocialMediaCategoryAdapter?.setListSize(sSocialMediaPageInfoResponse?.categoryShowCount ?: 0)
                }
            }
        }
    }

    override fun onSocialMediaPageInfoResponse(commonApiResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonApiResponse.mIsSuccessStatus) {
                sSocialMediaPageInfoResponse = Gson().fromJson<SocialMediaPageInfoResponse>(commonApiResponse.mCommonDataStr, SocialMediaPageInfoResponse::class.java)
                setupSocialMediaUIFromResponse()
            }
        }
    }

    private fun setupSocialMediaUIFromResponse() {
        ToolBarManager.getInstance()?.apply {
            headerTitle = sSocialMediaPageInfoResponse?.socialMediaStaticTextResponse?.heading_social_media
            setupHelpPageUI(sSocialMediaPageInfoResponse?.socialMediaHelpPage)
        }
        mFavoriteCategoryId = sSocialMediaPageInfoResponse?.favouriteCategoryId ?: 1
        showMoreCategoryTextView?.text = sSocialMediaPageInfoResponse?.socialMediaStaticTextResponse?.text_show_more_categories
        categoryRecyclerView?.apply {
            sSocialMediaCategoriesList = sSocialMediaPageInfoResponse?.socialMediaCategoriesList
            itemAnimator = DefaultItemAnimator()
            mSocialMediaCategoryAdapter = SocialMediaCategoryAdapter(mActivity, sSocialMediaCategoriesList, sSocialMediaPageInfoResponse?.categoryShowCount ?: 0, this@SocialMediaFragment)
            layoutManager = GridLayoutManager(mActivity, 3)
            adapter = mSocialMediaCategoryAdapter
        }
    }

    override fun onSocialMediaTemplateListResponse(commonApiResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonApiResponse.mIsSuccessStatus) {
                val response = Gson().fromJson<SocialMediaTemplateListResponse>(commonApiResponse.mCommonDataStr, SocialMediaTemplateListResponse::class.java)
                mIsNextPage = response?.isNextPage ?: false
                Log.d(TAG, "onSocialMediaTemplateListResponse: mIsNextPage :: $mIsNextPage :: List Size :: ${response?.templateList?.size}")
                setDataToSocialMediaTemplateList(response?.templateList)
            }
        }
    }

    override fun onSocialMediaTemplateFavouriteResponse(commonApiResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            showShortSnackBar(commonApiResponse.mMessage, true, if (commonApiResponse.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red_small)
            if (!mIsUnFavoriteTemplateClicked) {
                Log.d(TAG, "onViewCreated: onSocialMediaTemplateFavouriteResponse called :: page number :: $mPageNumber :: Category Id :: $mSelectedCategoryId")
                mService?.getSocialMediaTemplateList("$mFavoriteCategoryId", mPageNumber)
            }
        }
    }

    private fun setupHelpPageUI(marketingHelpPage: HelpPageResponse?) {
        ToolBarManager.getInstance()?.apply {
            if (marketingHelpPage?.mIsActive == true) {
                setSideIconVisibility(true)
                mActivity?.let { setSideIcon(ContextCompat.getDrawable(it, R.drawable.ic_setting_toolbar), this@SocialMediaFragment) }
            } else {
                setSideIconVisibility(false)
            }
        }
    }

    private fun setDataToSocialMediaTemplateList(templateList: ArrayList<SocialMediaTemplateListItemResponse?>?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (1 == mPageNumber) {
                mSocialMediaTemplateList = ArrayList()
                mSocialMediaTemplateList = templateList
            } else {
                templateList?.let { list -> mSocialMediaTemplateList?.addAll(list) }
            }
            Log.d(TAG, "setDataToSocialMediaTemplateList: List data :: ${mSocialMediaTemplateList?.size}")
            if (0 == mSocialMediaTemplateList?.size) {
                templateRecyclerView?.visibility = View.GONE
            } else {
                templateRecyclerView?.visibility = View.VISIBLE
                mSocialMediaTemplateAdapter?.setListToAdapter(mSocialMediaTemplateList)
            }
        }
    }

    override fun onSocialMediaException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onToolbarSideIconClicked() = openWebViewFragment(this, getString(R.string.help), WebViewUrls.WEB_VIEW_HELP, Constants.SETTINGS)

    override fun onSocialMediaTemplateCategoryItemClickListener(position: Int, item: SocialMediaCategoryItemResponse?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (mSelectedCategoryId == (item?.id ?: "0")) return@runTaskOnCoroutineMain
            sSocialMediaCategoriesList?.forEachIndexed { pos, itemResponse ->
                itemResponse?.isSelected = (pos == position)
            }
            mSocialMediaCategoryAdapter?.notifyDataSetChanged()
            showProgressDialog(mActivity)
            mPageNumber = 1
            mSelectedCategoryId = item?.id ?: "0"
            Log.d(TAG, "onViewCreated: onSocialMediaTemplateCategoryItemClickListener called :: page number :: $mPageNumber :: Category Id :: $mSelectedCategoryId")
            mService?.getSocialMediaTemplateList(mSelectedCategoryId, mPageNumber)
        }
    }

    override fun onSocialMediaTemplateFavItemClickListener(position: Int, item: SocialMediaTemplateListItemResponse?) {
        val request = SocialMediaTemplateFavouriteRequest(
            templateId = item?.id?.toInt() ?: 0,
            isFavourite = !(item?.isFavourite ?: false)
        )
        mIsUnFavoriteTemplateClicked = request.isFavourite
        mService?.setSocialMediaFavourite(request)
        item?.isFavourite = !(item?.isFavourite ?: false)
        mSocialMediaTemplateAdapter?.notifyItemChanged(position)
    }

    override fun onSocialMediaTemplateEditItemClickListener(position: Int, item: SocialMediaTemplateListItemResponse?) {
        launchFragment(EditSocialMediaTemplateFragment.newInstance(mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_edit_and_share, item, false, mMarketingPageInfoResponse), true)
    }

    override fun onSocialMediaTemplateShareItemClickListener(position: Int, item: SocialMediaTemplateListItemResponse?) {
        setupUIForScreenShot(item)
        screenshotContainer?.let { v ->
            Handler(Looper.getMainLooper()).postDelayed({
                val originalBitmap = getBitmapFromView(v, mActivity)
                originalBitmap?.let { bitmap -> shareOnWhatsApp("Order From - ${mMarketingPageInfoResponse?.marketingStoreInfo?.domain}", bitmap) }
            }, Constants.TIMER_INTERVAL)
        }
    }

    override fun onSocialMediaTemplateWhatsappItemClickListener(position: Int, item: SocialMediaTemplateListItemResponse?) {
        setupUIForScreenShot(item)
        screenshotContainer?.let { v ->
            Handler(Looper.getMainLooper()).postDelayed({
                val originalBitmap = getBitmapFromView(v, mActivity)
                originalBitmap?.let { bitmap -> shareOnWhatsApp("Order From - ${mMarketingPageInfoResponse?.marketingStoreInfo?.domain}", bitmap) }
            }, Constants.TIMER_INTERVAL)
        }
    }

    private fun setupUIForScreenShot(item: SocialMediaTemplateListItemResponse?) {
        screenshotOuterContainer?.visibility = View.VISIBLE
        mActivity?.let { context ->
            val screenshotImageView: ImageView? = mContentView?.findViewById(R.id.screenshotImageView)
            screenshotImageView?.let { view -> Glide.with(context).load(item?.coverImage).into(view) }
        }
        val screenshotStoreNameTextView: TextView? = mContentView?.findViewById(R.id.screenshotStoreNameTextView)
        val domain = mMarketingPageInfoResponse?.marketingStoreInfo?.domain
        getQRCodeBitmap(mActivity, domain)?.let { b ->
            val screenshotQRImageView: ImageView? = mContentView?.findViewById(R.id.screenshotQRImageView)
            screenshotQRImageView?.setImageBitmap(b)
        }
        screenshotStoreNameTextView?.text = domain
        storeNameWithTextColorTextView?.apply {
            text = mMarketingPageInfoResponse?.marketingStoreInfo?.name
            if (isNotEmpty(item?.textColor)) setTextColor(Color.parseColor(item?.textColor))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        sSocialMediaPageInfoResponse = null
    }

}