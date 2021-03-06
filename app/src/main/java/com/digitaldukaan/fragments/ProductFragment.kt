package com.digitaldukaan.fragments

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.bumptech.glide.Glide
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
import com.digitaldukaan.models.request.UpdateItemInventoryRequest
import com.digitaldukaan.models.request.UpdateStockRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.ProductService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProductServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.product_fragment.*
import org.json.JSONObject

class ProductFragment : BaseFragment(), IProductServiceInterface, IOnToolbarIconClick,
    PopupMenu.OnMenuItemClickListener {

    private var mService: ProductService? = null
    private var mShareStorePDFResponse: ShareStorePDFDataItemResponse? = null
    private var mOptionsMenuResponse: ArrayList<TrendingListResponse>? = null
    private var mShareDataOverWhatsAppText: String? = ""
    private var mUserCategoryResponse: AddProductStoreCategory? = null
    private var addProductBannerStaticDataResponse: AddProductBannerTextResponse? = null
    private var addProductChipsAdapter: AddProductsChipsAdapter? = null
    private var mSelectedCategoryItem: StoreCategoryItem? = null
    private var mDeleteCategoryItemList: ArrayList<DeleteCategoryItemResponse?>? = null
    private val mTempProductCategoryList: ArrayList<StoreCategoryItem> = ArrayList()
    private var mIsDoublePressToExit = false
    private var mProductPageInfoResponse: ProductPageResponse? = null

    companion object {
        private var addProductStaticData: AddProductStaticText? = null

        fun newInstance(): ProductFragment = ProductFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mService = ProductService()
        mService?.setOrderDetailServiceListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "ProductFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.product_fragment, container, false)
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else {
            showCancellableProgressDialog(mActivity)
            mService?.getProductPageInfo()
        }
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            onBackPressed(this@ProductFragment)
            setSecondSideIconVisibility(false)
            setSideIconVisibility(true)
            mActivity?.let { setSideIcon(ContextCompat.getDrawable(it, R.drawable.ic_options_menu), this@ProductFragment) }
        }
        hideBottomNavigationView(false)
        WebViewBridge.mWebViewListener = this
        updateNavigationBarState(R.id.menuProducts)
        mService?.getDeleteCategoryItem()
        mService?.getUserCategories()
        return mContentView
    }

    override fun onAddProductBannerStaticDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            addProductBannerStaticDataResponse = Gson().fromJson(commonResponse.mCommonDataStr, AddProductBannerTextResponse::class.java)
            addProductBannerStaticDataResponse?.run { showMasterCatalogBottomSheet(addProductBannerStaticDataResponse, addProductStaticData, Constants.MODE_PRODUCT_LIST) }
        }
    }

    override fun onProductPageInfoResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mProductPageInfoResponse = Gson().fromJson(commonResponse.mCommonDataStr, ProductPageResponse::class.java)
            StaticInstances.sIsShareStoreLocked = mProductPageInfoResponse?.isShareStoreLocked ?: false
            bottomContainer?.visibility = if (mProductPageInfoResponse?.isZeroProduct == true) View.GONE else View.VISIBLE
            addProductStaticData = mProductPageInfoResponse?.staticText
            var url: String
            ToolBarManager.getInstance()?.headerTitle = mProductPageInfoResponse?.staticText?.product_page_heading
            mOptionsMenuResponse = mProductPageInfoResponse?.optionMenuList
            commonWebView?.apply {
                settings.javaScriptEnabled = true
                addJavascriptInterface(WebViewBridge(), Constants.KEY_ANDROID)
                url = BuildConfig.WEB_VIEW_URL + mProductPageInfoResponse?.productPageUrl + "?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&app_version=${BuildConfig.VERSION_NAME}&app_version_code=${BuildConfig.VERSION_CODE}"
                Log.d(TAG, "onProductResponse: WebView URL $url")
                loadUrl(url)
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String) {
                        stopProgress()
                    }
                }
            }
            mProductPageInfoResponse?.shareShop?.run {
                shareButtonTextView?.text = this.mText
                if (isNotEmpty(mCDN) && null != shareButtonImageView) {
                    Glide.with(this@ProductFragment).load(mCDN).into(shareButtonImageView)
                }
            }
            mProductPageInfoResponse?.addProduct?.run {
                addProductTextView?.text = this.mText
                if (isNotEmpty(mCDN) && null != addProductImageView) {
                    Glide.with(this@ProductFragment).load(mCDN).into(addProductImageView)
                }
            }
        }
        stopProgress()
    }

    override fun onShareStorePdfDataResponse(commonResponse: CommonApiResponse) {
        mShareStorePDFResponse = Gson().fromJson<ShareStorePDFDataItemResponse>(commonResponse.mCommonDataStr, ShareStorePDFDataItemResponse::class.java)
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

    override fun onProductPDFGenerateResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showShortSnackBar(commonResponse.mMessage, true, if (commonResponse.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red)
        }
    }

    override fun onProductShareStoreWAResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                if (isEmpty(commonResponse.mCommonDataStr)) return@runTaskOnCoroutineMain
                stopProgress()
                mShareDataOverWhatsAppText = Gson().fromJson<String>(commonResponse.mCommonDataStr, String::class.java)
                shareOnWhatsApp(mShareDataOverWhatsAppText)
            } catch (e: Exception) {
                Log.e(TAG, "onProductShareStoreWAResponse: ${e.message}", e)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        "Exception Point" to "onProductShareStoreWAResponse",
                        "Exception Message" to e.message,
                        "Exception Logs" to e.toString()
                    )
                )
            }
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
            mService?.getProductPageInfo()
            mService?.getUserCategories()
            showShortSnackBar(commonResponse.mMessage, true, if (commonResponse.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red)
        }
    }

    override fun onDeleteCategoryResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mService?.getProductPageInfo()
            mService?.getUserCategories()
            showShortSnackBar(commonResponse.mMessage, true, if (commonResponse.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red)
        }
    }

    override fun onUpdateStockResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mService?.getProductPageInfo()
            showShortSnackBar(commonResponse.mMessage, true, if (commonResponse.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red)
        }
    }

    override fun onQuickUpdateItemInventoryResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mService?.getProductPageInfo()
            showShortSnackBar(commonResponse.mMessage, true, if (commonResponse.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red)
        }
    }

    override fun onGenerateStorePdfResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showShortSnackBar(commonResponse.mMessage, true, if (commonResponse.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red)
        }
    }

    private fun showPDFShareBottomSheet(response: ShareStorePDFDataItemResponse?) {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(
                R.layout.bottom_sheet_refer_and_earn,
                it.findViewById(R.id.bottomSheetContainer)
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
                    if (isNotEmpty(response?.imageUrl)) { bottomSheetUpperImageView.let { view -> Glide.with(this@ProductFragment).load(response?.imageUrl).into(view) } }
                    bottomSheetUpperImageView.setImageDrawable(ContextCompat.getDrawable(it, R.drawable.ic_share_pdf_whatsapp))
                    bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                    bottomSheetHeadingTextView.text = response?.heading
                    verifyTextView.text = response?.subHeading
                    verifyTextView.setOnClickListener{
                        if (StaticInstances.sIsShareStoreLocked) {
                            getLockedStoreShareDataServerCall(Constants.MODE_GET_MY_CATALOGUE)
                            return@setOnClickListener
                        }
                        showProgressDialog(mActivity)
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_GET_PDF_CATALOG,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(
                                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                AFInAppEventParameterName.PATH to AFInAppEventParameterName.CATALOG
                            )
                        )
                        mService?.generateProductStorePdf()
                        bottomSheetDialog.dismiss()
                    }
                    referAndEarnRecyclerView.apply {
                        layoutManager = LinearLayoutManager(mActivity)
                        adapter = SharePDFAdapter(response?.howItWorks)
                    }
                }
            }.show()
        }
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
                        AFInAppEventParameterName.IS_CATALOG to AFInAppEventParameterName.TRUE
                    )
                )
                if (StaticInstances.sIsShareStoreLocked) {
                    getLockedStoreShareDataServerCall(Constants.MODE_SHARE_STORE)
                    return
                }
                if (isNotEmpty(mShareDataOverWhatsAppText)) shareOnWhatsApp(mShareDataOverWhatsAppText) else if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                } else {
                    showProgressDialog(mActivity)
                    mService?.getProductShareStoreData()
                }
            }
        }
    }

    override fun onToolbarSideIconClicked() {
        val wrapper = ContextThemeWrapper(mActivity, R.style.PopupMenuStyle)
        val sideView:View? = mActivity?.findViewById(R.id.sideIconToolbar)
        val optionsMenu = PopupMenu(wrapper, sideView)
        optionsMenu.inflate(R.menu.menu_product_fragment)
        mOptionsMenuResponse?.forEachIndexed { position, response ->
            Log.d(TAG, "onToolbarSideIconClicked: $response")
            optionsMenu.menu?.add(Menu.NONE, position, Menu.NONE, response.mText)
        }
        optionsMenu.setOnMenuItemClickListener(this)
        optionsMenu.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        val optionMenuItemStr = mOptionsMenuResponse?.get(item?.itemId ?: 0)
        if (isNotEmpty(optionMenuItemStr?.mPage)) {
            openWebViewFragment(this, "", BuildConfig.WEB_VIEW_URL + optionMenuItemStr?.mPage)
        } else {
            if (null != mShareStorePDFResponse) {
                showPDFShareBottomSheet(mShareStorePDFResponse)
            } else if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
            } else {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SHARE_PDF_CATALOG,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        AFInAppEventParameterName.PATH to AFInAppEventParameterName.CATALOG
                    )
                )
                showProgressDialog(mActivity)
                mService?.getShareStorePdfText()
            }
        }
        return true
    }

    override fun sendData(data: String) {
        Log.d(TAG, "sendData: $data")
        val jsonData = JSONObject(data)
        when {
            jsonData.optBoolean("catalogBuilderBannerClick") -> {
                if (addProductBannerStaticDataResponse == null) {
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return
                    }
                    showProgressDialog(mActivity)
                    mService?.getAddOrderBottomSheetData()
                } else showMasterCatalogBottomSheet(addProductBannerStaticDataResponse, addProductStaticData, Constants.MODE_PRODUCT_LIST)
            }
            jsonData.optBoolean("catalogItemEdit") -> {
                val jsonDataObject = JSONObject(jsonData.optString(WebViewBridge.DATA))
                launchFragment(AddProductFragment.newInstance(jsonDataObject.optInt(WebViewBridge.ID), false), true)
            }
            jsonData.optBoolean("catalogAddItem") -> {
                launchFragment(AddProductFragment.newInstance(0, true), true)
            }
            jsonData.optBoolean("unauthorizedAccess") -> {
                logoutFromApplication()
            }
            jsonData.optBoolean("stopLoader") -> {
                stopProgress()
            }
            jsonData.optBoolean("catalogCategoryEdit") -> {
                val jsonDataObject = JSONObject(jsonData.optString(WebViewBridge.DATA))
                showUpdateCategoryBottomSheet(jsonDataObject.optString("name"), jsonDataObject.optInt(WebViewBridge.ID))
            }
            jsonData.optBoolean("editProductTag") -> {
                openWebConsoleBottomSheet(mProductPageInfoResponse?.webConsoleBottomSheet)
            }
            jsonData.optBoolean("shareTextOnWhatsApp") -> {
                val text = jsonData.optString(WebViewBridge.DATA)
                val mobileNumber = jsonData.optString("mobileNumber")
                shareDataOnWhatsAppByNumber(mobileNumber, text)
            }
            jsonData.optBoolean("updateInventoryQuantity") -> {
                val itemJsonObject = JSONObject(jsonData.optString(WebViewBridge.DATA))
                val storeItemId = itemJsonObject.optInt(WebViewBridge.ID)
                val selectedVariantStr = jsonData.optString(WebViewBridge.SELECTED_VARIANT)
                if ("null".equals(selectedVariantStr, true) || isEmpty(selectedVariantStr)) {
                    showUpdateInventoryBottomSheet(storeItemId, itemJsonObject.optInt(WebViewBridge.ID), itemJsonObject.optInt(WebViewBridge.AVAILABLE_QUANTITY))
                } else {
                    val selectedVariantJsonObject = JSONObject(selectedVariantStr)
                     showUpdateInventoryBottomSheet(storeItemId, selectedVariantJsonObject.optInt(WebViewBridge.VARIANT_ID),selectedVariantJsonObject.optInt(WebViewBridge.AVAILABLE_QUANTITY) )
                }
            }
            jsonData.optBoolean("viewShopAsCustomer") -> {
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_VIEW_AS_CUSTOMER,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.CHANNEL to "isCatalog")
                )
                val isPremiumEnable = (1 == jsonData.optInt("isPremium"))
                launchFragment(ViewAsCustomerFragment.newInstance(jsonData.optString("domain"), isPremiumEnable, addProductStaticData), true)
            }
            jsonData.optBoolean("catalogStockUpdate") -> {
                val selectedItemObject = JSONObject(jsonData.optString(WebViewBridge.DATA))
                val selectedVariantStr = jsonData.optString(WebViewBridge.SELECTED_VARIANT)
                val isAvailable: Int
                val manageInventory: Int
                when {
                    isEmpty(selectedVariantStr) -> {
                        manageInventory = selectedItemObject.optInt(WebViewBridge.MANAGED_INVENTORY)
                        isAvailable = selectedItemObject.optInt(WebViewBridge.AVAILABLE)
                        if (1 == isAvailable) {
                            if (Constants.TEXT_YES == PrefsManager.getStringDataFromSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK)) {
                                val request = UpdateStockRequest(selectedItemObject.optInt(WebViewBridge.ID), 0, 0)
                                mService?.updateStock(request)
                            } else showOutOfStockDialog(selectedItemObject, 0, Constants.INVENTORY_ENABLE == manageInventory)
                        } else {
                            val request = UpdateStockRequest(selectedItemObject.optInt(WebViewBridge.ID), if (0 == selectedItemObject.optInt("available")) 1 else 0, 0)
                            if (!isInternetConnectionAvailable(mActivity)) {
                                showNoInternetConnectionDialog()
                                return
                            }
                            showProgressDialog(mActivity)
                            mService?.updateStock(request)
                        }
                    }
                    else -> {
                        val selectedVariantObject = JSONObject(selectedVariantStr)
                        manageInventory = selectedVariantObject.optInt(WebViewBridge.MANAGED_INVENTORY)
                        isAvailable = selectedVariantObject.optInt(WebViewBridge.AVAILABLE)
                        if (1 == isAvailable) {
                            if (Constants.TEXT_YES == PrefsManager.getStringDataFromSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK)) {
                                val request = UpdateStockRequest(selectedItemObject.optInt(WebViewBridge.ID), 0, selectedVariantObject.optInt(WebViewBridge.VARIANT_ID))
                                mService?.updateStock(request)
                            } else showOutOfStockDialog(selectedItemObject, selectedVariantObject.optInt(WebViewBridge.VARIANT_ID), Constants.INVENTORY_ENABLE == manageInventory)
                        } else {
                            if (Constants.INVENTORY_DISABLE == manageInventory) {
                                val request = UpdateStockRequest(selectedItemObject.optInt(WebViewBridge.ID), 1, selectedVariantObject.optInt(WebViewBridge.VARIANT_ID))
                                mService?.updateStock(request)
                            } else showUpdateInventoryBottomSheet(selectedItemObject.optInt(WebViewBridge.ID), selectedVariantObject.optInt(WebViewBridge.VARIANT_ID), selectedVariantObject.optInt(WebViewBridge.AVAILABLE_QUANTITY))
                        }

                    }
                }
            }
            jsonData.optBoolean("trackEventData") -> {
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
    }

    private fun showOutOfStockDialog(jsonDataObject: JSONObject, variantId: Int, manageInventory: Boolean) {
        mActivity?.let { context ->
            Dialog(context).apply {
                setCancelable(true)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setContentView(R.layout.out_of_stock_dialog)
                var isCheckBoxVisible = ("" == PrefsManager.getStringDataFromSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK) || Constants.TEXT_NO == PrefsManager.getStringDataFromSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK))
                val noTextView: TextView = findViewById(R.id.noTextView)
                val yesTextView: TextView = findViewById(R.id.yesTextView)
                val headingTextView: TextView = findViewById(R.id.headingTextView)
                val messageTextView: TextView = findViewById(R.id.messageTextView)
                headingTextView.text = if (manageInventory) addProductStaticData?.dialog_heading_mark_inventory else null
                headingTextView.visibility = if (manageInventory) View.VISIBLE else View.GONE
                messageTextView.text = if (manageInventory) addProductStaticData?.dialog_sub_heading_mark_inventory else addProductStaticData?.dialog_stock_message
                yesTextView.text = addProductStaticData?.text_yes
                noTextView.text = addProductStaticData?.text_no
                if (isCheckBoxVisible) {
                    val checkBox: CheckBox = findViewById(R.id.checkBox)
                    checkBox.visibility = View.VISIBLE
                    checkBox.text = addProductStaticData?.dialog_stock_dont_show_this_again
                    checkBox.setOnCheckedChangeListener { _, isChecked -> isCheckBoxVisible = isChecked }
                    isCheckBoxVisible = false
                }
                noTextView.setOnClickListener {
                    this.dismiss()
                }
                yesTextView.setOnClickListener {
                    this.dismiss()
                    storeStringDataInSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN_STOCK, if (isCheckBoxVisible) Constants.TEXT_YES else Constants.TEXT_NO)
                    val request = UpdateStockRequest(jsonDataObject.optInt(WebViewBridge.ID), 0, variantId)
                    if (!isInternetConnectionAvailable(context)) {
                        showNoInternetConnectionDialog()
                    } else {
                        showProgressDialog(context)
                        mService?.updateStock(request)
                    }
                }
            }.show()
        }
    }

    private fun showUpdateCategoryBottomSheet(categoryName: String?, categoryId: Int) {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(
                R.layout.bottom_sheet_edit_category,
                it.findViewById(R.id.bottomSheetContainer)
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
                                        mTempProductCategoryList.forEachIndexed { _, categoryItem ->
                                            categoryItem.isSelected = false
                                        }
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
                        if (isEmpty(categoryNameInputByUser.trim())) {
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
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_EDIT_CATEGORY,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                        )
                        mService?.updateCategory(request)
                    }
                    deleteCategoryTextView.setOnClickListener {
                        bottomSheetDialog.dismiss()
                        showDeleteCategoryBottomSheet(categoryName, categoryId)
                    }
                }
            }.show()
        }
    }

    private fun showDeleteCategoryBottomSheet(categoryName: String?, categoryId: Int) {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(
                R.layout.bottom_sheet_delete_category,
                it.findViewById(R.id.bottomSheetContainer)
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
                                if (isEmpty(mDeleteCategoryItemList?.get(position)?.action)) {
                                    bottomSheetDialog.dismiss()
                                } else {
                                    val request = DeleteCategoryRequest(categoryId, mDeleteCategoryItemList?.get(position)?.action == "true")
                                    bottomSheetDialog.dismiss()
                                    showProgressDialog(mActivity)
                                    AppEventsManager.pushAppEvents(
                                        eventName = AFInAppEventType.EVENT_DELETE_CATEGORY,
                                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                        data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                                    )
                                    mService?.deleteCategory(request)
                                }
                            }
                        })
                    }
                }
            }.show()
        }
    }

    override fun onBackPressed(): Boolean {
        Log.d(TAG, "onBackPressed: called")
        if (null != fragmentManager && 1 == fragmentManager?.backStackEntryCount) {
            if (true == StaticInstances.sPermissionHashMap?.get(Constants.PAGE_ORDER)) {
                clearFragmentBackStack()
                launchFragment(OrderFragment.newInstance(), true)
            } else {
                if (mIsDoublePressToExit) mActivity?.finish()
                showShortSnackBar(getString(R.string.msg_back_press))
                mIsDoublePressToExit = true
                Handler(Looper.getMainLooper()).postDelayed(
                    { mIsDoublePressToExit = false },
                    Constants.BACK_PRESS_INTERVAL
                )
            }
            return true
        }
        return false
    }

    private fun showUpdateInventoryBottomSheet(itemId: Int, variantId: Int, availableQuantity: Int?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.run {
                val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_update_inventory, findViewById(R.id.bottomSheetContainer))
                bottomSheetDialog.apply {
                    setContentView(view)
                    setBottomSheetCommonProperty()
                    view.run {
                        val cancelTextView: TextView = findViewById(R.id.cancelTextView)
                        val saveTextView: TextView = findViewById(R.id.saveTextView)
                        val headingTextView: TextView = findViewById(R.id.headingTextView)
                        val quantityContainer: TextInputLayout = findViewById(R.id.quantityContainer)
                        val quantityEdiText: EditText = findViewById(R.id.quantityTextView)
                        saveTextView.text = addProductStaticData?.text_save
                        cancelTextView.text = addProductStaticData?.text_cancel
                        headingTextView.text = addProductStaticData?.dialog_heading_add_inventory
                        quantityContainer.hint = addProductStaticData?.hint_available_quantity
                        cancelTextView.setOnClickListener { bottomSheetDialog.dismiss() }
                        if (0 != availableQuantity) quantityEdiText.setText("$availableQuantity")
                        saveTextView.setOnClickListener {
                            CoroutineScopeUtils().runTaskOnCoroutineMain {
                                val quantity = quantityEdiText.text?.toString()
                                if (isEmpty(quantity)) {
                                    quantityEdiText.error = addProductStaticData?.error_mandatory_field
                                    return@runTaskOnCoroutineMain
                                }
                                bottomSheetDialog.dismiss()
                                val request = UpdateItemInventoryRequest(
                                    storeItemId = itemId,
                                    variantId = variantId,
                                    managedInventory = 1,
                                    availableQuantity = quantity?.toInt() ?: 0
                                )
                                mService?.quickUpdateItemInventory(request)
                                AppEventsManager.pushAppEvents(
                                    eventName = AFInAppEventType.EVENT_INVENTORY_QUANTITY_MODIFY, isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                    data = mapOf(
                                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                        AFInAppEventParameterName.VARIANT_ID to "$variantId",
                                        AFInAppEventParameterName.QUANTITY to "$quantity",
                                        AFInAppEventParameterName.ITEM_ID to "$itemId"
                                    )
                                )
                            }
                        }
                    }
                }.show()
            }
        }
    }

    override fun onLockedStoreShareSuccessResponse(lockedShareResponse: LockedStoreShareResponse) = showLockedStoreShareBottomSheet(lockedShareResponse)
}