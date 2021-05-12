package com.digitaldukaan.fragments

import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.layout_master_catelog_fragment.*

class MasterCatalogFragment: BaseFragment(), IExploreCategoryServiceInterface, IOnToolbarIconClick {

    private val mService = ExploreCategoryService()
    private var addProductStaticData: AddProductStaticText? = null
    private var mExploreCategoryItem: ExploreCategoryItemResponse? = null
    private var subCategoryItemList: ArrayList<ExploreCategoryItemResponse>? = null
    private var subCategoryAdapter: SubCategoryAdapter? = null
    private var masterCatalogAdapter: MasterCatalogItemsAdapter? = null
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

    companion object {
        private const val TAG = "MasterCatalogFragment"
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
        mLinearLayoutManager = LinearLayoutManager(mActivity)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        ToolBarManager.getInstance()?.apply { hideToolBar(mActivity, true) }
        val txtSpannable = SpannableString(addProductStaticData?.text_explore + " " + mExploreCategoryItem?.categoryName)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, addProductStaticData?.text_explore?.length ?: 6, txtSpannable.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
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
                masterCatalogAdapter = MasterCatalogItemsAdapter(mActivity, mCategoryItemsList, this@MasterCatalogFragment, addProductStaticData)
                adapter = masterCatalogAdapter

                addOnScrollListener(object : RecyclerView.OnScrollListener() {

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == newState) mIsRecyclerViewScrolling = true
                    }

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        mCurrentItems = mLinearLayoutManager.childCount
                        mTotalItems = masterCatalogAdapter?.itemCount ?: 0
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
            backImageView?.id -> mActivity.onBackPressed()
            searchImageView?.id -> showShortSnackBar()
            addProductTextView?.id -> showConfirmationBottomSheet()
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
                    mCategoryId = subCategoryItemList?.get(0)?.categoryId ?: 0
                    mService.getMasterItems(mCategoryId, 1)
                    subCategoryRecyclerView?.apply {
                        layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                        subCategoryAdapter = SubCategoryAdapter(mActivity, subCategoryItemList, this@MasterCatalogFragment)
                        adapter = subCategoryAdapter
                    }
                }
            }
        }
    }

    override fun onBuildCatalogResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                showShortSnackBar(response.mMessage, true, R.drawable.ic_check_circle)
                mActivity.onBackPressed()
            } else showShortSnackBar(response.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onSubCategoryItemsResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (response.mIsSuccessStatus) {
                val categoryItems = Gson().fromJson<MasterCatalogResponse>(response.mCommonDataStr, MasterCatalogResponse::class.java)
                productCountTextView?.text = "${categoryItems?.totalItems} ${addProductStaticData?.text_tap_to_select}"
                mIsMoreItemsAvailable = categoryItems.isNext
                if (categoryItems?.itemList?.isNotEmpty() == true) mCategoryItemsList?.addAll(categoryItems.itemList)
                masterCatalogAdapter?.setMasterCatalogList(mCategoryItemsList, mSelectedProductsHashMap)
            }
        }
    }

    override fun onCategoryItemsClickResponse(response: MasterCatalogItemResponse?) {
        Log.d(TAG, "onCategoryItemsClickResponse: ${response?.itemName}")
    }

    override fun onCategoryItemsImageClick(response: MasterCatalogItemResponse?) {
        showImageDialog(response?.imageUrl)
    }

    override fun onCategoryItemsSetPriceClick(position: Int, response: MasterCatalogItemResponse?) {
        showSetPriceBottomSheet(response, position)
    }

    override fun onCategoryCheckBoxClick(position: Int, response: MasterCatalogItemResponse?, isChecked: Boolean) {
        if (isChecked) {
            mSelectedProductsHashMap[response?.itemId] = response
            mSelectedProductsHashMap[response?.itemId]?.parentCategoryIdForRequest = mCategoryId
        } else mSelectedProductsHashMap.remove(response?.itemId)
        if (mSelectedProductsHashMap.isNotEmpty()) {
            addProductTextView?.visibility = View.VISIBLE
            val size = mSelectedProductsHashMap.size
            addProductTextView?.text = if (size == 1) "${addProductStaticData?.text_add} 1 ${addProductStaticData?.text_product}" else "${addProductStaticData?.text_add} $size ${addProductStaticData?.text_products}"
            AppEventsManager.pushAppEvents(eventName = AFInAppEventType.EVENT_CATALOG_BUILDER_PRODUCT_SELECT, isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true, data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    AFInAppEventParameterName.CATEGORY_NAME to response?.itemName,
                    AFInAppEventParameterName.PRODUCTS_ADDED to "$size"
                ))
        } else {
            addProductTextView?.visibility = View.GONE
        }
    }

    private fun showSetPriceBottomSheet(response: MasterCatalogItemResponse?, position: Int) {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_set_price,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
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
                bottomSheetHeadingTextView.text = addProductStaticData?.bottom_sheet_set_price_below
                imageView?.let {
                    try {
                        Picasso.get().load(response?.imageUrl).into(it)
                    } catch (e: Exception) {
                        Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                    }
                }
                titleTextView.text = response?.itemName
                priceLayout.hint = addProductStaticData?.hint_price
                setPriceTextView.text = addProductStaticData?.bottom_sheet_set_price
                setPriceTextView.setOnClickListener {
                    val price = priceEditText.text.toString()
                    bottomSheetDialog.dismiss()
                    mCategoryItemsList?.get(position)?.isSelected = true
                    mCategoryItemsList?.get(position)?.price = if (price.isEmpty()) 0.0 else price.toDouble()
                    masterCatalogAdapter?.notifyItemChanged(position)
                }
            }
        }.show()
    }

    private fun showConfirmationBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_set_price_confirmation,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            view.run {
                val bottomSheetClose: View = findViewById(R.id.bottomSheetClose)
                val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                val setPriceTextView: TextView = findViewById(R.id.setPriceTextView)
                val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
                bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                bottomSheetHeadingTextView.text = addProductStaticData?.bottom_sheet_confirm_selection
                val size = mSelectedProductsHashMap.size
                setPriceTextView.text = if (size == 1) "${addProductStaticData?.text_add} 1 ${addProductStaticData?.text_product}" else "${addProductStaticData?.text_add} $size ${addProductStaticData?.text_products}"
                val addMasterCatalogConfirmProductsList = ArrayList(mSelectedProductsHashMap.values)
                recyclerView.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    adapter = MasterCatalogItemsConfirmationAdapter(mActivity, addProductStaticData, addMasterCatalogConfirmProductsList)
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

    override fun onExploreCategoryItemClick(response: ExploreCategoryItemResponse?) {
        val newEmptyArrayList: ArrayList<MasterCatalogItemResponse> = ArrayList()
        mCategoryItemsList = newEmptyArrayList
        var position = 0
        subCategoryItemList?.forEachIndexed { pos, itemResponse -> itemResponse.isSelected = false
            if (response?.categoryId == itemResponse.categoryId) position = pos
        }
        subCategoryItemList?.get(position)?.isSelected = true
        subCategoryAdapter?.setSubCategoryList(subCategoryItemList)
        subCategoryRecyclerView.scrollToPosition(position)
        showProgressDialog(mActivity)
        mPageCount = 1
        mCategoryId = response?.categoryId ?: 0
        mService.getMasterItems(mCategoryId, mPageCount)
    }

    override fun onExploreCategoryServerException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    override fun onToolbarSideIconClicked() {
        Log.d(TAG, "onToolbarSideIconClicked: do nothing")
    }
}