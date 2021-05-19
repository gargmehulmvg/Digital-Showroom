package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ExploreCategoryAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.models.response.AddProductStaticText
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.models.response.ExploreCategoryItemResponse
import com.digitaldukaan.models.response.MasterCatalogItemResponse
import com.digitaldukaan.services.ExploreCategoryService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IExploreCategoryServiceInterface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_explore_category_fragment.*

class ExploreCategoryFragment: BaseFragment(), IExploreCategoryServiceInterface {

    private val mService = ExploreCategoryService()
    private var addProductStaticData: AddProductStaticText? = null

    companion object {
        private const val TAG = "ExploreCategoryFragment"
        fun newInstance(addProductStaticData: AddProductStaticText?): ExploreCategoryFragment {
            val fragment =  ExploreCategoryFragment()
            fragment.addProductStaticData = addProductStaticData
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
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
        collapsingToolbar?.title = addProductStaticData?.heading_explore_categories_page
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
                val listType = object : TypeToken<List<ExploreCategoryItemResponse>>() {}.type
                val list = Gson().fromJson<ArrayList<ExploreCategoryItemResponse>>(response.mCommonDataStr, listType)
                exploreCategoryRecyclerView?.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    adapter = ExploreCategoryAdapter(list, this@ExploreCategoryFragment)
                }
            }
        }
    }

    override fun onBuildCatalogResponse(response: CommonApiResponse) {
        Log.d(TAG, "onBuildCatalogResponse: do nothing")
    }

    override fun onSubCategoryItemsResponse(response: CommonApiResponse) {
        Log.d(TAG, "onCategoryItemsResponse: do nothing")
    }

    override fun onCategoryItemsClickResponse(response: MasterCatalogItemResponse?) {
        Log.d(TAG, "onCategoryItemsClickResponse: do nothing")
    }

    override fun onCategoryItemsImageClick(response: MasterCatalogItemResponse?) {
        Log.d(TAG, "onCategoryItemsImageClickResponse: do nothing")
    }

    override fun onCategoryItemsSetPriceClick(position: Int, response: MasterCatalogItemResponse?) {
        Log.d(TAG, "onCategoryItemsSetPriceClickResponse: do nothing")
    }

    override fun onCategoryCheckBoxClick(position: Int, response: MasterCatalogItemResponse?, isChecked: Boolean) {
        Log.d(TAG, "onCategoryCheckBoxClick: do nothing")
    }

    override fun onExploreCategoryItemClick(response: ExploreCategoryItemResponse?) {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_CATALOG_BUILDER_CATEGORY_SELECT,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.CATEGORY_NAME to response?.categoryName
            )
        )
        launchFragment(MasterCatalogFragment.newInstance(addProductStaticData, response), true)
    }

    override fun onExploreCategoryServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }
}