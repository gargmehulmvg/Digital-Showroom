package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ExploreCategoryAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.ExploreCategoryService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IExploreCategoryServiceInterface
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_explore_category_fragment.*

class ExploreCategoryFragment: BaseFragment(), IExploreCategoryServiceInterface {

    private val mService = ExploreCategoryService()
    private var mExploreMasterCategoryResponse: ExploreCategoryPageInfoResponse? = null

    companion object {
        fun newInstance(): ExploreCategoryFragment = ExploreCategoryFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "ExploreCategoryFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_explore_category_fragment, container, false)
        mService.setServiceInterface(this)
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_CATALOG_BUILDER_EXPLORE,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
        )
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.apply { hideToolBar(mActivity, true) }
        hideBottomNavigationView(true)
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        mService.getMasterCategories()
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            backButton?.id -> mActivity?.onBackPressed()
        }
    }

    override fun onExploreCategoryResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                mExploreMasterCategoryResponse = Gson().fromJson(response.mCommonDataStr, ExploreCategoryPageInfoResponse::class.java)
                collapsingToolbar?.title = mExploreMasterCategoryResponse?.staticText?.heading_explore_categories_page
                exploreCategoryRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    adapter = ExploreCategoryAdapter(mExploreMasterCategoryResponse?.masterCategoriesList, this@ExploreCategoryFragment)
                }
            }
        }
    }

    override fun onBuildCatalogResponse(response: CommonApiResponse) = Unit

    override fun onSubCategoryItemsResponse(response: CommonApiResponse) = Unit

    override fun onCategoryItemsClickResponse(response: MasterCatalogItemResponse?) = Unit

    override fun onCategoryItemsImageClick(response: MasterCatalogItemResponse?) = Unit

    override fun onCategoryItemsSetPriceClick(position: Int, response: MasterCatalogItemResponse?) = Unit

    override fun onCategoryCheckBoxClick(position: Int, response: MasterCatalogItemResponse?, isChecked: Boolean) = Unit

    override fun onExploreCategoryItemClick(response: ExploreCategoryItemResponse?) {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_CATALOG_BUILDER_CATEGORY_SELECT,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.CATEGORY_NAME to response?.categoryName
            )
        )
        launchFragment(MasterCatalogFragment.newInstance(mExploreMasterCategoryResponse, response), true)
    }

    override fun onExploreCategoryServerException(e: Exception) = exceptionHandlingForAPIResponse(e)
}