package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.layout_on_board_help_screen.*
import kotlinx.android.synthetic.main.layout_social_media.*

class SocialMediaFragment : BaseFragment(), ISocialMediaServiceInterface, IOnToolbarIconClick,
    ISocialMediaTemplateItemClickListener {

    private var mService: SocialMediaService? = null
    private var mSocialMediaPageInfoResponse: SocialMediaPageInfoResponse? = null
    private var socialMediaCategoryAdapter: SocialMediaCategoryAdapter? = null
    private var socialMediaTemplateAdapter: SocialMediaTemplateAdapter? = null
    private var mSocialMediaTemplateList: ArrayList<SocialMediaTemplateListItemResponse?>? = ArrayList()
    private var mPageNumber: Int = 1

    companion object {
        private const val TAG = "SocialMediaFragment"
        fun newInstance(): SocialMediaFragment {
            return SocialMediaFragment()
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
        showProgressDialog(mActivity)
        mService?.getSocialMediaPageInfo()
        mService?.getSocialMediaTemplateList("0", mPageNumber)
        templateRecyclerView?.apply {
            setHasFixedSize(true)
            socialMediaTemplateAdapter = SocialMediaTemplateAdapter(this@SocialMediaFragment, null, this@SocialMediaFragment)
            layoutManager = LinearLayoutManager(mActivity)
            adapter = socialMediaTemplateAdapter
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            showMoreCategoryTextView?.id -> {
                if (showMoreCategoryTextView?.text == mSocialMediaPageInfoResponse?.socialMediaStaticTextResponse?.text_show_more_categories) {
                    showMoreCategoryTextView?.text = mSocialMediaPageInfoResponse?.socialMediaStaticTextResponse?.text_hide
                    socialMediaCategoryAdapter?.setListSize(mSocialMediaPageInfoResponse?.socialMediaCategoriesList?.size ?: 0)
                } else {
                    showMoreCategoryTextView?.text = mSocialMediaPageInfoResponse?.socialMediaStaticTextResponse?.text_show_more_categories
                    socialMediaCategoryAdapter?.setListSize(mSocialMediaPageInfoResponse?.categoryShowCount ?: 0)
                }
            }
        }
    }

    override fun onSocialMediaPageInfoResponse(commonApiResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonApiResponse.mIsSuccessStatus) {
                mSocialMediaPageInfoResponse = Gson().fromJson<SocialMediaPageInfoResponse>(commonApiResponse.mCommonDataStr, SocialMediaPageInfoResponse::class.java)
                ToolBarManager.getInstance()?.apply {
                    setHeaderTitle(mSocialMediaPageInfoResponse?.socialMediaStaticTextResponse?.heading_social_media)
                    setupHelpPageUI(mSocialMediaPageInfoResponse?.socialMediaHelpPage)
                }
                showMoreCategoryTextView?.text = mSocialMediaPageInfoResponse?.socialMediaStaticTextResponse?.text_show_more_categories
                categoryRecyclerView?.apply {
                    itemAnimator = DefaultItemAnimator()
                    socialMediaCategoryAdapter = SocialMediaCategoryAdapter(mActivity, mSocialMediaPageInfoResponse?.socialMediaCategoriesList, mSocialMediaPageInfoResponse?.categoryShowCount ?: 0, this@SocialMediaFragment)
                    layoutManager = GridLayoutManager(mActivity, 3)
                    adapter = socialMediaCategoryAdapter
                }
            }
        }
    }

    override fun onSocialMediaTemplateListResponse(commonApiResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonApiResponse.mIsSuccessStatus) {
                val response = Gson().fromJson<SocialMediaTemplateListResponse>(commonApiResponse.mCommonDataStr, SocialMediaTemplateListResponse::class.java)
                setupTemplateListRecyclerView(response?.templateList)
            }
        }
    }

    override fun onSocialMediaTemplateFavouriteResponse(commonApiResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            showShortSnackBar(commonApiResponse.mMessage, true, if (commonApiResponse.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red_small)
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

    private fun setupTemplateListRecyclerView(templateList: ArrayList<SocialMediaTemplateListItemResponse?>?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mSocialMediaTemplateList = ArrayList()
            mSocialMediaTemplateList = templateList
            socialMediaTemplateAdapter?.setListToAdapter(mSocialMediaTemplateList)
        }
    }

    override fun onSocialMediaException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onToolbarSideIconClicked() = openWebViewFragment(this, getString(R.string.help), WebViewUrls.WEB_VIEW_HELP, Constants.SETTINGS)

    override fun onSocialMediaTemplateCategoryItemClickListener(position: Int, item: SocialMediaCategoryItemResponse?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            showProgressDialog(mActivity)
            mPageNumber = 1
            mService?.getSocialMediaTemplateList(item?.id ?: "0", mPageNumber)
        }
    }

    override fun onSocialMediaTemplateFavItemClickListener(position: Int, item: SocialMediaTemplateListItemResponse?) {
        val request = SocialMediaTemplateFavouriteRequest(
            templateId = item?.id?.toInt() ?: 0,
            isFavourite = !(item?.isFavourite ?: false)
        )
        mService?.setSocialMediaFavourite(request)
        item?.isFavourite = !(item?.isFavourite ?: false)
        socialMediaTemplateAdapter?.notifyItemChanged(position)
    }

    override fun onSocialMediaTemplateEditItemClickListener(
        position: Int,
        item: SocialMediaTemplateListItemResponse?
    ) {
        showToast("Edit $position")
    }

    override fun onSocialMediaTemplateShareItemClickListener(
        position: Int,
        item: SocialMediaTemplateListItemResponse?
    ) {
        showToast("Share $position")
    }

    override fun onSocialMediaTemplateWhatsappItemClickListener(
        position: Int,
        item: SocialMediaTemplateListItemResponse?
    ) {
        showToast("Whatsapp $position")
    }

}