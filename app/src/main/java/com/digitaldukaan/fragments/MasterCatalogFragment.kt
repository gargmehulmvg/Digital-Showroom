package com.digitaldukaan.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.MasterCatalogItemsAdapter
import com.digitaldukaan.adapters.SubCategoryAdapter
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.ExploreCategoryService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IExploreCategoryServiceInterface
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_master_catelog_fragment.*

class MasterCatalogFragment: BaseFragment(), IExploreCategoryServiceInterface, IOnToolbarIconClick {

    private val mService = ExploreCategoryService()
    private var addProductStaticData: AddProductStaticText? = null
    private var mExploreCategoryItem: ExploreCategoryItemResponse? = null
    private var subCategoryItemList: ArrayList<ExploreCategoryItemResponse>? = null
    private var subCategoryAdapter: SubCategoryAdapter? = null
    private var masterCatalogAdapter: MasterCatalogItemsAdapter? = null

    companion object {
        fun newInstance(addProductStaticData: AddProductStaticText?, item: ExploreCategoryItemResponse?): MasterCatalogFragment {
            val fragment =  MasterCatalogFragment()
            fragment.addProductStaticData = addProductStaticData
            fragment.mExploreCategoryItem = item
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mContentView = inflater.inflate(R.layout.layout_master_catelog_fragment, container, false)
        mService.setServiceInterface(this)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance().apply { hideToolBar(mActivity, true) }
        val txtSpannable = SpannableString(addProductStaticData?.text_explore + " " + mExploreCategoryItem?.categoryName)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, addProductStaticData?.text_explore?.length ?: 6, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        exploreTextView.text = txtSpannable
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        mService.getMasterSubCategories(mExploreCategoryItem?.categoryId)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            backImageView.id -> mActivity.onBackPressed()
            searchImageView.id -> showShortSnackBar()
        }
    }

    override fun onExploreCategoryResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val listType = object : TypeToken<List<ExploreCategoryItemResponse>>() {}.type
                subCategoryItemList = Gson().fromJson<ArrayList<ExploreCategoryItemResponse>>(response.mCommonDataStr, listType)
                if (subCategoryItemList?.isNotEmpty() == true) {
                    subCategoryItemList?.get(0)?.isSelected = true
                    mService.getMasterItems(subCategoryItemList?.get(0)?.categoryId, 1)
                    subCategoryRecyclerView.apply {
                        layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                        subCategoryAdapter = SubCategoryAdapter(mActivity, subCategoryItemList, this@MasterCatalogFragment)
                        adapter = subCategoryAdapter
                    }
                }
            }
        }
    }

    override fun onSubCategoryItemsResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val categoryItems = Gson().fromJson<MasterCatalogResponse>(response.mCommonDataStr, MasterCatalogResponse::class.java)
                productCountTextView.text = "${categoryItems?.totalItems} ${addProductStaticData?.text_products}, ${addProductStaticData?.text_tap_to_select}"
                masterCatalogRecyclerView.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    masterCatalogAdapter = MasterCatalogItemsAdapter(mActivity, categoryItems?.itemList, this@MasterCatalogFragment, addProductStaticData)
                    adapter = masterCatalogAdapter
                }
            }
        }
    }

    override fun onCategoryItemsClickResponse(response: MasterCatalogItemResponse?) {
        showToast()
    }

    override fun onExploreCategoryItemClickedResponse(response: ExploreCategoryItemResponse?) {
        var position = 0
        subCategoryItemList?.forEachIndexed { pos, itemResponse -> itemResponse.isSelected = false
            if (response?.categoryId == itemResponse.categoryId) position = pos
        }
        subCategoryItemList?.get(position)?.isSelected = true
        subCategoryAdapter?.setSubCategoryList(subCategoryItemList)
        subCategoryRecyclerView.scrollToPosition(position)
        showProgressDialog(mActivity)
        mService.getMasterItems(response?.categoryId, 1)
    }

    override fun onExploreCategoryServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onToolbarSideIconClicked() {
        showToast()
    }

}