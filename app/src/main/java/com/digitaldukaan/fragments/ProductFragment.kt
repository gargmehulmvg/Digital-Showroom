package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.AddProductsChipsAdapter
import com.digitaldukaan.adapters.DeleteCategoryAdapter
import com.digitaldukaan.adapters.SharePDFAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IChipItemClickListener
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.request.DeleteCategoryRequest
import com.digitaldukaan.models.request.UpdateCategoryRequest
import com.digitaldukaan.models.request.UpdateStockRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.ProductService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProductServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.product_fragment.*
import org.json.JSONObject

class ProductFragment : BaseFragment(), IProductServiceInterface, IOnToolbarIconClick,
    PopupMenu.OnMenuItemClickListener {

    private lateinit var mService: ProductService
    private var mShareStorePDFResponse: ShareStorePDFDataItemResponse? = null
    private var mOptionsMenuResponse: ArrayList<TrendingListResponse>? = null
    private var mShareDataOverWhatsAppText = ""
    private var mUserCategoryResponse: AddProductStoreCategory? = null
    private var addProductBannerStaticDataResponse: AddProductBannerTextResponse? = null
    private var addProductChipsAdapter: AddProductsChipsAdapter? = null
    private var mSelectedCategoryItem: AddStoreCategoryItem? = null
    private var mDeleteCategoryItemList: ArrayList<DeleteCategoryItemResponse?>? = null
    private val mTempProductCategoryList: ArrayList<AddStoreCategoryItem> = ArrayList()


    companion object {
        private var addProductStaticData: AddProductStaticText? = null

        private const val TAG = "ProductFragment"
        fun newInstance(): ProductFragment {
            return ProductFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mService = ProductService()
        mService.setOrderDetailServiceListener(this)
        mService.getDeleteCategoryItem()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.product_fragment, container, false)
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else mService.getProductPageInfo()
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            onBackPressed(this@ProductFragment)
            setSecondSideIconVisibility(false)
            setSideIconVisibility(true)
            setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_options_menu), this@ProductFragment)
        }
        hideBottomNavigationView(false)
        WebViewBridge.mWebViewListener = this
        updateNavigationBarState(R.id.menuProducts)
        mService.getUserCategories()
        return mContentView
    }

    override fun onAddProductBannerStaticDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            addProductBannerStaticDataResponse = Gson().fromJson<AddProductBannerTextResponse>(commonResponse.mCommonDataStr, AddProductBannerTextResponse::class.java)
            addProductBannerStaticDataResponse?.run { showMaterCatalogBottomSheet(addProductBannerStaticDataResponse, addProductStaticData, Constants.MODE_PRODUCT_LIST) }
        }
    }

    override fun onProductResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val productResponse = Gson().fromJson(commonResponse.mCommonDataStr, ProductPageResponse::class.java)
            bottomContainer?.visibility = if (productResponse?.isZeroProduct == true) View.GONE else View.VISIBLE
            addProductStaticData = productResponse?.static_text
            var url: String
            ToolBarManager.getInstance().setHeaderTitle(productResponse?.static_text?.product_page_heading)
            mOptionsMenuResponse = productResponse?.optionMenuList
            commonWebView?.apply {
                settings.javaScriptEnabled = true
                addJavascriptInterface(WebViewBridge(), "Android")
                url = BuildConfig.WEB_VIEW_URL + productResponse?.product_page_url + "?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"
                loadUrl(url)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        stopProgress()
                    }
                }
            }
            productResponse.shareShop.run {
                shareButtonTextView?.text = this.mText
                if (mCDN != null && mCDN.isNotEmpty() && shareButtonImageView != null) Picasso.get().load(mCDN).into(shareButtonImageView)
            }
            productResponse.addProduct.run {
                addProductTextView?.text = this.mText
                if (mCDN != null &&mCDN.isNotEmpty() && addProductImageView != null) Picasso.get().load(mCDN).into(addProductImageView)
            }
        }
        stopProgress()
    }

    override fun onShareStorePdfDataResponse(response: CommonApiResponse) {
        mShareStorePDFResponse = Gson().fromJson<ShareStorePDFDataItemResponse>(response.mCommonDataStr, ShareStorePDFDataItemResponse::class.java)
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showPDFShareBottomSheet(mShareStorePDFResponse)
        }
    }

    override fun onProductShareStorePDFDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            val sharePDF = Gson().fromJson<ShareStorePDFDataItemResponse>(commonResponse.mCommonDataStr, ShareStorePDFDataItemResponse::class.java)
            showPDFShareBottomSheet(sharePDF)
        }
    }

    override fun onProductPDFGenerateResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showShortSnackBar(response.mMessage, true, if (response.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red)
        }
    }

    override fun onProductShareStoreWAResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mShareDataOverWhatsAppText = Gson().fromJson<String>(commonResponse.mCommonDataStr, String::class.java)
            shareDataOnWhatsApp(mShareDataOverWhatsAppText)
        }
    }

    override fun onUserCategoryResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                mUserCategoryResponse = Gson().fromJson<AddProductStoreCategory>(commonResponse.mCommonDataStr, AddProductStoreCategory::class.java)
            }
        }
    }

    override fun onDeleteCategoryInfoResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                val listType = object : TypeToken<List<DeleteCategoryItemResponse?>>() {}.type
                mDeleteCategoryItemList = Gson().fromJson<ArrayList<DeleteCategoryItemResponse?>>(commonResponse.mCommonDataStr, listType)
            }
        }
    }

    override fun onUpdateCategoryResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mService.getProductPageInfo()
            mService.getUserCategories()
            showShortSnackBar(commonResponse.mMessage, true, if (commonResponse.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red)
        }
    }

    override fun onDeleteCategoryResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mService.getProductPageInfo()
            mService.getUserCategories()
            showShortSnackBar(commonResponse.mMessage, true, if (commonResponse.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red)
        }
    }

    override fun onUpdateStockResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mService.getProductPageInfo()
            showShortSnackBar(commonResponse.mMessage, true, if (commonResponse.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red)
        }
    }

    override fun onGenerateStorePdfResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showShortSnackBar(
                response.mMessage,
                true,
                if (response.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red
            )
        }
    }

    private fun showPDFShareBottomSheet(response: ShareStorePDFDataItemResponse?) {
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
                if (response?.imageUrl?.isNotEmpty() == true) Picasso.get().load(response.imageUrl).into(bottomSheetUpperImageView)
                bottomSheetUpperImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_share_pdf_whatsapp))
                bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                bottomSheetHeadingTextView.text = response?.heading
                verifyTextView.text = response?.subHeading
                verifyTextView.setOnClickListener{
                    showProgressDialog(mActivity)
                    mService.generateProductStorePdf()
                    bottomSheetDialog.dismiss()
                }
                referAndEarnRecyclerView.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    adapter = SharePDFAdapter(response?.howItWorks)
                }
            }
        }.show()
    }

    override fun onProductException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onClick(view: View?) {
        when(view?.id) {
            addProductContainer.id -> launchFragment(AddProductFragment.newInstance(0, true), true)
            shareProductContainer.id -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_STORE_SHARE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.IS_CATALOG to "true"
                    )
                )
                if (mShareDataOverWhatsAppText.isNotEmpty()) shareDataOnWhatsApp(mShareDataOverWhatsAppText) else if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                } else {
                    showProgressDialog(mActivity)
                    mService.getProductShareStoreData()
                }
            }
        }
    }

    override fun onToolbarSideIconClicked() {
        val sideView:View = mActivity.findViewById(R.id.sideIconToolbar)
        val optionsMenu = PopupMenu(mActivity, sideView)
        optionsMenu.inflate(R.menu.menu_product_fragment)
        mOptionsMenuResponse?.forEachIndexed { position, response ->
            optionsMenu.menu?.add(Menu.NONE, position, Menu.NONE, response.mText)
        }
        optionsMenu.setOnMenuItemClickListener(this)
        optionsMenu.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val optionMenuItemStr = mOptionsMenuResponse?.get(item?.itemId ?: 0)
        if (optionMenuItemStr?.mPage?.isNotEmpty() == true) {
            openWebViewFragment(this, "", BuildConfig.WEB_VIEW_URL + optionMenuItemStr.mPage)
        } else {
            if (mShareStorePDFResponse != null) showPDFShareBottomSheet(mShareStorePDFResponse) else if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
            } else {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_STORE_SHARE,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.IS_CATALOG to "true"
                    )
                )
                showProgressDialog(mActivity)
                mService.getShareStorePdfText()
            }
        }
        return true
    }

    override fun sendData(data: String) {
        Log.d(TAG, "sendData: $data")
        val jsonData = JSONObject(data)
        if (jsonData.optBoolean("catalogBuilderBannerClick")) {
            if (addProductBannerStaticDataResponse == null) {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                showProgressDialog(mActivity)
                mService.getAddOrderBottomSheetData()
            } else showMaterCatalogBottomSheet(addProductBannerStaticDataResponse, addProductStaticData, Constants.MODE_PRODUCT_LIST)
        }
        else if (jsonData.optBoolean("catalogCategoryEdit")) {
            val jsonDataObject = JSONObject(jsonData.optString("data"))
            showUpdateCategoryBottomSheet(jsonDataObject.optString("name"), jsonDataObject.optInt("id"))
        } else if (jsonData.optBoolean("catalogItemEdit")) {
            val jsonDataObject = JSONObject(jsonData.optString("data"))
            launchFragment(AddProductFragment.newInstance(jsonDataObject.optInt("id"), false), true)
        } else if (jsonData.optBoolean("catalogAddItem")) {
            launchFragment(AddProductFragment.newInstance(0, true), true)
        } else if (jsonData.optBoolean("viewShopAsCustomer")) {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_VIEW_AS_CUSTOMER,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    AFInAppEventParameterName.CHANNEL to "isCatalog"
                )
            )
            val isPremiumEnable = (jsonData.optInt("isPremium") == 1)
            launchFragment(ViewAsCustomerFragment.newInstance(jsonData.optString("domain"), isPremiumEnable, addProductStaticData), true)
        } else if (jsonData.optBoolean("catalogStockUpdate")) {
            val jsonDataObject = JSONObject(jsonData.optString("data"))
            val isAvailable = jsonDataObject.optInt("available")
            if (isAvailable == 1) {
                if (Constants.TEXT_YES == PrefsManager.getStringDataFromSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK)) {
                    val request = UpdateStockRequest(jsonDataObject.optInt("id"), 0)
                    mService.updateStock(request)
                } else showOutOfStockDialog(jsonDataObject)
            } else {
                val request = UpdateStockRequest(jsonDataObject.optInt("id"), if (jsonDataObject.optInt("available") == 0) 1 else 0)
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                }
                showProgressDialog(mActivity)
                mService.updateStock(request)
            }
        } else if (jsonData.optBoolean("trackEventData")) {
            val eventName = jsonData.optString("eventName")
            val additionalData = jsonData.optString("additionalData")
            val map = Gson().fromJson<HashMap<String, String>>(additionalData.toString(), HashMap::class.java)
            Log.d(TAG, "sendData: working $map")
            AppEventsManager.pushAppEvents(
                eventName = eventName,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = map
            )
        }
    }


    private fun showOutOfStockDialog(jsonDataObject: JSONObject) {
        val builder = AlertDialog.Builder(mActivity)
        val view: View = layoutInflater.inflate(R.layout.dont_show_again_dialog, null)
        var isCheckBoxVisible = "" == PrefsManager.getStringDataFromSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK) || Constants.TEXT_NO == PrefsManager.getStringDataFromSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK)
        builder.apply {
            setMessage(getString(R.string.out_of_stock_message))
            if (isCheckBoxVisible) setView(view)
            setPositiveButton(getString(R.string.txt_yes)) { dialogInterface, _ ->
                dialogInterface.dismiss()
                storeStringDataInSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK, if (isCheckBoxVisible) Constants.TEXT_YES else Constants.TEXT_NO)
                val request = UpdateStockRequest(jsonDataObject.optInt("id"), 0)
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                } else {
                    showProgressDialog(mActivity)
                    mService.updateStock(request)
                }
            }
            setNegativeButton(getString(R.string.text_no)) { dialogInterface, _ ->
                run {
                    dialogInterface.dismiss()
                    if (isCheckBoxVisible) storeStringDataInSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN, Constants.TEXT_NO)
                }
            }
            view.run {
                val checkBox: CheckBox = view.findViewById(R.id.checkBox)
                checkBox.text = getString(R.string.dont_show_again_message)
                isCheckBoxVisible = false
                checkBox.setOnCheckedChangeListener { _, isChecked -> isCheckBoxVisible = isChecked }
            }
        }.show()
    }

    private fun showUpdateCategoryBottomSheet(categoryName: String?, categoryId: Int) {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_edit_category,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            view.run {
                val categoryChipRecyclerView: RecyclerView = findViewById(R.id.categoryChipRecyclerView)
                val deleteCategoryTextView: TextView = findViewById(R.id.deleteCategoryTextView)
                val editCategoryTextView: TextView = findViewById(R.id.editCategoryTextView)
                val categoryNameTextView: TextView = findViewById(R.id.categoryNameTextView)
                val saveTextView: TextView = findViewById(R.id.saveTextView)
                val categoryNameEditText: EditText = findViewById(R.id.categoryNameEditText)
                val categoryNameInputLayout: TextInputLayout = findViewById(R.id.categoryNameInputLayout)
                editCategoryTextView.text = addProductStaticData?.bottom_sheet_heading_edit_category
                categoryNameTextView.text = addProductStaticData?.bottom_sheet_category_name
                categoryNameInputLayout.hint = addProductStaticData?.bottom_sheet_hint_category_name
                deleteCategoryTextView.text = addProductStaticData?.bottom_sheet_delete_category
                saveTextView.text = addProductStaticData?.bottom_sheet_save
                categoryNameEditText.setText(categoryName)
                mUserCategoryResponse?.storeCategoriesList?.run {
                    categoryChipRecyclerView.apply {
                        layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
                        mTempProductCategoryList.clear()
                        val list = mUserCategoryResponse?.storeCategoriesList
                        list?.forEachIndexed { _, categoryItem ->
                            if (categoryItem.name?.isNotEmpty() == true) mTempProductCategoryList.add(categoryItem)
                        }
                        mTempProductCategoryList.forEachIndexed { _, categoryItem -> categoryItem.isSelected = (categoryItem.id == categoryId) }
                        if (mTempProductCategoryList.isNotEmpty())
                            addProductChipsAdapter = AddProductsChipsAdapter(mTempProductCategoryList, object : IChipItemClickListener {
                                    override fun onChipItemClickListener(position: Int) {
                                        mTempProductCategoryList.forEachIndexed { _, categoryItem -> categoryItem.isSelected = false }
                                        mTempProductCategoryList[position].isSelected = true
                                        categoryNameEditText.setText(mTempProductCategoryList[position].name)
                                        mSelectedCategoryItem = mTempProductCategoryList[position]
                                        addProductChipsAdapter?.setAddProductStoreCategoryList(mTempProductCategoryList)
                                    }
                                })
                        adapter = addProductChipsAdapter
                    }
                }
                saveTextView.setOnClickListener {
                    val categoryNameInputByUser = categoryNameEditText.text.toString()
                    if (categoryNameInputByUser.trim().isEmpty()) {
                        categoryNameEditText.apply {
                            error = addProductStaticData?.error_mandatory_field
                            requestFocus()
                        }
                        return@setOnClickListener
                    }
                    val request = if (categoryNameInputByUser.equals(mSelectedCategoryItem?.name, true)) {
                        UpdateCategoryRequest(categoryId, mSelectedCategoryItem?.name)
                    } else {
                        UpdateCategoryRequest(categoryId, categoryNameInputByUser)
                    }
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return@setOnClickListener
                    }
                    bottomSheetDialog.dismiss()
                    showProgressDialog(mActivity)
                    mService.updateCategory(request)
                }
                deleteCategoryTextView.setOnClickListener {
                    bottomSheetDialog.dismiss()
                    showDeleteCategoryBottomSheet(categoryName, categoryId)
                }
            }
        }.show()
    }
    private fun showDeleteCategoryBottomSheet(categoryName: String?, categoryId: Int) {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_delete_category,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            view.run {
                val deleteCategoryRecyclerView: RecyclerView = findViewById(R.id.deleteCategoryRecyclerView)
                val deleteCategoryTextView: TextView = findViewById(R.id.deleteCategoryTextView)
                val categoryNameTextView: TextView = findViewById(R.id.categoryNameTextView)
                deleteCategoryTextView.text = addProductStaticData?.bottom_sheet_delete_category
                categoryNameTextView.text = categoryName
                deleteCategoryRecyclerView.apply {
                    layoutManager = LinearLayoutManager(mActivity)
                    adapter = DeleteCategoryAdapter(mDeleteCategoryItemList, object : IChipItemClickListener {
                        override fun onChipItemClickListener(position: Int) {
                            if (mDeleteCategoryItemList?.get(position)?.action?.isEmpty() == true) {
                                bottomSheetDialog.dismiss()
                            } else {
                                val request = DeleteCategoryRequest(categoryId, mDeleteCategoryItemList?.get(position)?.action == "true")
                                bottomSheetDialog.dismiss()
                                showProgressDialog(mActivity)
                                mService.deleteCategory(request)
                            }
                        }
                    })
                }
            }
        }.show()
    }

}