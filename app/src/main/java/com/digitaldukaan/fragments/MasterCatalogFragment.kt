package com.digitaldukaan.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.R
import com.digitaldukaan.adapters.MasterCatalogItemsAdapter
import com.digitaldukaan.adapters.MasterCatalogItemsConfirmationAdapter
import com.digitaldukaan.adapters.SubCategoryAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.ExploreCategoryService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IExploreCategoryServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.layout_master_catalog_fragment.*

class MasterCatalogFragment: BaseFragment(), IExploreCategoryServiceInterface, IOnToolbarIconClick {

    private val mService = ExploreCategoryService()
    private var mStaticData: ExploreCategoryStaticTextResponse? = null
    private var mExploreCategoryItem: ExploreCategoryItemResponse? = null
    private var mSubCategoryItemList: ArrayList<ExploreCategoryItemResponse>? = null
    private var mSubCategoryAdapter: SubCategoryAdapter? = null
    private var mMasterCatalogAdapter: MasterCatalogItemsAdapter? = null
    private lateinit var mLinearLayoutManager: LinearLayoutManager
    private var mCurrentItems = 0
    private var mTotalItems = 0
    private var mScrollOutItems = 0
    private var mPageCount = 1
    private var mCategoryId = 0
    private var mIsRecyclerViewScrolling = false
    private var mIsMoreItemsAvailable = false
    private var mCategoryItemsList: ArrayList<MasterCatalogItemResponse>? = ArrayList()
    private val mSelectedProductsHashMap: HashMap<Int?, MasterCatalogItemResponse?> = HashMap()
    private var mSubCategoryLimit: Int = 0
    private var mSubCategoryLimitMap: HashMap<Int, Int> = HashMap()

    companion object {
        fun newInstance(exploreCategoryPageInfoResponse: ExploreCategoryPageInfoResponse?, item: ExploreCategoryItemResponse?): MasterCatalogFragment {
            val fragment =  MasterCatalogFragment()
            fragment.mStaticData = exploreCategoryPageInfoResponse?.staticText
            fragment.mExploreCategoryItem = item
            fragment.mSubCategoryLimit = exploreCategoryPageInfoResponse?.subCategoryLimit ?: 0
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "MasterCatalogFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_master_catalog_fragment, container, false)
        mService.setServiceInterface(this)
        mLinearLayoutManager = LinearLayoutManager(mActivity)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.apply { hideToolBar(mActivity, true) }
        val txtSpannable = SpannableString(mStaticData?.text_explore + " " + mExploreCategoryItem?.categoryName)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, mStaticData?.text_explore?.length ?: 6, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        exploreTextView?.text = txtSpannable
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        showProgressDialog(mActivity)
        mService.getMasterSubCategories(mExploreCategoryItem?.categoryId)
        try {
            masterCatalogRecyclerView?.apply {
                layoutManager = mLinearLayoutManager
                mActivity?.let {
                    mMasterCatalogAdapter = MasterCatalogItemsAdapter(it, mCategoryItemsList, this@MasterCatalogFragment, mStaticData)
                    adapter = mMasterCatalogAdapter
                    addOnScrollListener(object : RecyclerView.OnScrollListener() {

                        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                            super.onScrollStateChanged(recyclerView, newState)
                            if (AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == newState) mIsRecyclerViewScrolling = true
                        }

                        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                            super.onScrolled(recyclerView, dx, dy)
                            mCurrentItems = mLinearLayoutManager.childCount
                            mTotalItems = mMasterCatalogAdapter?.itemCount ?: 0
                            mScrollOutItems = mLinearLayoutManager.findFirstVisibleItemPosition()
                            if (mIsRecyclerViewScrolling && (mCurrentItems + mScrollOutItems == mTotalItems)) {
                                mIsRecyclerViewScrolling = false
                                if (mIsMoreItemsAvailable) {
                                    mPageCount++
                                    mService.getMasterItems(mCategoryId, mPageCount)
                                }
                            }
                        }
                    })
                }
            }
        } catch (e: Exception) {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "exception" to e.toString(),
                    "CategoryID" to mCategoryId.toString(),
                    "PageCount" to mPageCount.toString()
                )
            )
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            backImageView?.id -> mActivity?.onBackPressed()
            searchImageView?.id -> showShortSnackBar()
            addProductTextView?.id -> showConfirmationBottomSheet()
        }
    }

    override fun onExploreCategoryResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val listType = object : TypeToken<List<ExploreCategoryItemResponse>>() {}.type
                mSubCategoryItemList = Gson().fromJson<ArrayList<ExploreCategoryItemResponse>>(response.mCommonDataStr, listType)
                if (isNotEmpty(mSubCategoryItemList)) {
                    mSubCategoryItemList?.get(0)?.isSelected = true
                    mCategoryId = mSubCategoryItemList?.get(0)?.categoryId ?: 0
                    mService.getMasterItems(mCategoryId, 1)
                    subCategoryRecyclerView?.apply {
                        mActivity?.let {
                            layoutManager = LinearLayoutManager(it, LinearLayoutManager.HORIZONTAL, false)
                            mSubCategoryAdapter = SubCategoryAdapter(it, mSubCategoryItemList, this@MasterCatalogFragment)
                            adapter = mSubCategoryAdapter
                        }
                    }
                }
            }
        }
    }

    override fun onBuildCatalogResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                mActivity?.onBackPressed()
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onSubCategoryItemsResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val categoryItems = Gson().fromJson(response.mCommonDataStr, MasterCatalogResponse::class.java)
                val messageStr = "${categoryItems?.totalItems} ${mStaticData?.text_tap_to_select}"
                productCountTextView?.text = messageStr
                mIsMoreItemsAvailable = categoryItems.isNext
                val prevSelectedMapCount = mSubCategoryLimitMap[mCategoryId]
                if (null == prevSelectedMapCount) {
                    mSubCategoryLimitMap[mCategoryId] = categoryItems?.totalSelectedItems ?: 0
                }
                if (isNotEmpty(categoryItems?.itemList)) mCategoryItemsList?.addAll(categoryItems.itemList)
                mMasterCatalogAdapter?.setMasterCatalogList(
                    mCategoryItemsList,
                    mSelectedProductsHashMap,
                    mSubCategoryLimitMap,
                    mSubCategoryLimit,
                    mCategoryId
                )
            }
        }
    }

    override fun onCategoryItemsClickResponse(response: MasterCatalogItemResponse?) = Unit

    override fun onCategoryItemsImageClick(response: MasterCatalogItemResponse?) = showImageDialog(response?.imageUrl)

    override fun onCategoryItemsSetPriceClick(position: Int, response: MasterCatalogItemResponse?) = showSetPriceBottomSheet(response, position)

    override fun onCategoryCheckBoxClick(position: Int, response: MasterCatalogItemResponse?, isChecked: Boolean) {
        var currentCategoryCount = mSubCategoryLimitMap[mCategoryId]
        if (isChecked) {
            mSelectedProductsHashMap[response?.itemId] = response
            mSelectedProductsHashMap[response?.itemId]?.parentCategoryIdForRequest = mCategoryId
            if (null != currentCategoryCount) mSubCategoryLimitMap[mCategoryId] = (++currentCategoryCount)
        } else {
            if (null != currentCategoryCount) mSubCategoryLimitMap[mCategoryId] = (--currentCategoryCount)
            mSelectedProductsHashMap.remove(response?.itemId)
        }
        if (mSelectedProductsHashMap.isNotEmpty()) {
            addProductTextView?.visibility = View.VISIBLE
            refreshCountView()
            AppEventsManager.pushAppEvents(eventName = AFInAppEventType.EVENT_CATALOG_BUILDER_PRODUCT_SELECT, isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true, data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    AFInAppEventParameterName.CATEGORY_NAME to response?.itemName,
                    AFInAppEventParameterName.PRODUCTS_ADDED to "${mSelectedProductsHashMap.size}"
                ))
        } else addProductTextView?.visibility = View.GONE
    }

    private fun refreshCountView() {
        val size = mSelectedProductsHashMap.size
        addProductTextView?.text = if (1 == size) "${mStaticData?.text_add} 1 ${mStaticData?.text_product}" else "${mStaticData?.text_add} $size ${mStaticData?.text_products}"
    }

    private fun showSetPriceBottomSheet(response: MasterCatalogItemResponse?, position: Int) {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_set_price, it.findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
                view.run {
                    val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                    val imageView: ImageView = findViewById(R.id.imageView)
                    val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                    val titleTextView: TextView = findViewById(R.id.titleTextView)
                    val priceLayout: TextInputLayout = findViewById(R.id.priceLayout)
                    val setPriceTextView: TextView = findViewById(R.id.setPriceTextView)
                    val priceEditText: EditText = findViewById(R.id.priceEditText)
                    bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                    bottomSheetHeadingTextView.text = mStaticData?.bottom_sheet_set_price_below
                    Glide.with(this@MasterCatalogFragment).load(response?.imageUrl).into(imageView)
                    titleTextView.text = response?.itemName
                    priceLayout.hint = mStaticData?.hint_price
                    setPriceTextView.text = mStaticData?.bottom_sheet_set_price
                    setPriceTextView.setOnClickListener {
                        val price = priceEditText.text.toString()
                        mCategoryItemsList?.get(position)?.isSelected = true
                        mCategoryItemsList?.get(position)?.price = if (isEmpty(price)) 0.0 else price.toDouble()
                        mSelectedProductsHashMap[response?.itemId] = mCategoryItemsList?.get(position)
                        mSelectedProductsHashMap[response?.itemId]?.parentCategoryIdForRequest = mCategoryId
                        var currentCategoryCount = mSubCategoryLimitMap[mCategoryId]
                        if (null != currentCategoryCount) mSubCategoryLimitMap[mCategoryId] = (++currentCategoryCount)
                        refreshCountView()
                        bottomSheetDialog.dismiss()
                        mMasterCatalogAdapter?.notifyItemChanged(position)
                    }
                }
            }.show()
        }
    }

    private fun showConfirmationBottomSheet() {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_set_price_confirmation, it.findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
                view.run {
                    val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                    val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                    val setPriceTextView: TextView = findViewById(R.id.setPriceTextView)
                    val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
                    bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                    bottomSheetHeadingTextView.text = mStaticData?.bottom_sheet_confirm_selection
                    val size = mSelectedProductsHashMap.size
                    setPriceTextView.text = if (1 == size) "${mStaticData?.text_add} 1 ${mStaticData?.text_product}" else "${mStaticData?.text_add} $size ${mStaticData?.text_products}"
                    val addMasterCatalogConfirmProductsList = ArrayList(mSelectedProductsHashMap.values)
                    recyclerView.apply {
                        layoutManager = LinearLayoutManager(mActivity)
                        adapter = MasterCatalogItemsConfirmationAdapter(it, mStaticData, addMasterCatalogConfirmProductsList)
                    }
                    setPriceTextView.setOnClickListener {
                        if (!isInternetConnectionAvailable(mActivity)) {
                            showNoInternetConnectionDialog()
                            return@setOnClickListener
                        }
                        mService.buildCatalog(addMasterCatalogConfirmProductsList)
                        bottomSheetDialog.dismiss()
                    }
                }
            }.show()
        }
    }

    override fun onExploreCategoryItemClick(response: ExploreCategoryItemResponse?) {
        val newEmptyArrayList: ArrayList<MasterCatalogItemResponse> = ArrayList()
        mCategoryItemsList = newEmptyArrayList
        var position = 0
        mSubCategoryItemList?.forEachIndexed { pos, itemResponse -> itemResponse.isSelected = false
            if (response?.categoryId == itemResponse.categoryId) position = pos
        }
        mSubCategoryItemList?.get(position)?.isSelected = true
        mSubCategoryAdapter?.setSubCategoryList(mSubCategoryItemList)
        subCategoryRecyclerView.scrollToPosition(position)
        showProgressDialog(mActivity)
        mPageCount = 1
        mCategoryId = response?.categoryId ?: 0
        mService.getMasterItems(mCategoryId, mPageCount)
    }

    override fun onExploreCategoryServerException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onToolbarSideIconClicked() = Unit
}