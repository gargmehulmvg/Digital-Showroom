package com.digitaldukaan.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.CategoryProductAdapter
import com.digitaldukaan.adapters.TemplateBackgroundAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.interfaces.IProductItemClickListener
import com.digitaldukaan.models.request.SaveSocialMediaPostRequest
import com.digitaldukaan.models.request.SearchCatalogItemsRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.EditSocialMediaTemplateService
import com.digitaldukaan.services.serviceinterface.IEditSocialMediaTemplateServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.bottom_sheet_show_products_with_category.view.*
import kotlinx.android.synthetic.main.layout_add_product_fragment.*
import kotlinx.android.synthetic.main.layout_edit_premium_fragment.*
import kotlinx.android.synthetic.main.layout_edit_social_media_template_fragment.*
import java.net.URLEncoder
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.ceil

class EditSocialMediaTemplateFragment : BaseFragment(), IEditSocialMediaTemplateServiceInterface, IProductItemClickListener {

    private var mSocialMediaTemplateResponse: SocialMediaTemplateListItemResponse? = null
    private var mService: EditSocialMediaTemplateService? = null
    private var mIsOpenBottomSheet = false
    private var mIsTemplateEdited = false
    private var mHeadingStr = ""
    private var mTemplateTypeStr = ""
    private var mMarketingPageInfoResponse: MarketingPageInfoResponse? = null
    private var mAddProductStoreCategoryList: ArrayList<StoreCategoryItem>? = ArrayList()
    private var mCategoryBottomSheetDialog: BottomSheetDialog? = null
    private var mProductCategoryCombineList: ArrayList<ProductCategoryCombineResponse>? = ArrayList()
    private var mTemplateBackgroundList: ArrayList<TemplateBackgroundItemResponse>? = ArrayList()
    private var mWebViewUrl = ""
    private var mSelectedBackgroundItem: TemplateBackgroundItemResponse? = null
    private var editTemplateWebView: WebView? = null
    private var mIsItemSelectedFromBottomSheet = false
    private var mSelectedProductFromBottomSheet: ProductResponse? = null

    companion object {
        private const val EDIT_TEMPLATE_WEB_VIEW_URL = BuildConfig.WEB_VIEW_URL + Constants.WEB_VIEW_URL_EDIT_SOCIAL_MEDIA_POST
        private var sIsWhatsAppIconClicked = false

        fun newInstance(templateType: String, heading: String?, item: SocialMediaTemplateListItemResponse?, isOpenBottomSheet: Boolean = false, marketingPageInfoResponse: MarketingPageInfoResponse?): EditSocialMediaTemplateFragment {
            val fragment = EditSocialMediaTemplateFragment()
            fragment.mSocialMediaTemplateResponse = item
            fragment.mHeadingStr = heading ?: ""
            fragment.mTemplateTypeStr = templateType
            fragment.mIsOpenBottomSheet = isOpenBottomSheet
            fragment.mMarketingPageInfoResponse = marketingPageInfoResponse
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "EditSocialMediaTemplateFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_edit_social_media_template_fragment, container, false)
        hideBottomNavigationView(true)
        WebViewBridge.mWebViewListener = this
        mService = EditSocialMediaTemplateService()
        mService?.setEditSocialMediaTemplateServiceListener(this)
        editTemplateWebView = mContentView?.findViewById(R.id.webView)
        return mContentView
    }

    private fun setupEditAndShareTemplateUI() {
        templateLayout?.visibility = View.VISIBLE
        noTemplateLayout?.visibility = View.GONE
        editTemplateWebView?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(WebViewBridge(), Constants.KEY_ANDROID)
            mWebViewUrl = "$EDIT_TEMPLATE_WEB_VIEW_URL?store_name=${mMarketingPageInfoResponse?.marketingStoreInfo?.name}&html=${URLEncoder.encode(Gson().toJson(mSocialMediaTemplateResponse?.html), "utf-8")}"
            Log.d(TAG, "onViewCreated: mWebViewUrl :: $mWebViewUrl")
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Log.d(TAG, "onPageFinished: called")
                    stopProgress()
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean { return onCommonWebViewShouldOverrideUrlLoading(url, view) }
            }
            loadUrl(mWebViewUrl)
            val domain = mMarketingPageInfoResponse?.marketingStoreInfo?.domain
            screenshotStoreNameTextView?.text = domain
            getQRCodeBitmap(mActivity, domain)?.let { b -> screenshotQRImageView?.setImageBitmap(b) }
        }
        setupBottomNavViewFromStaticText()
    }

    private fun setupBottomNavViewFromStaticText() {
        mMarketingPageInfoResponse?.marketingStaticTextResponse?.let { staticText ->
            if (ToolBarManager.getInstance().headerTitle.equals(mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_edit_and_share, true)) {
                backgroundTextView?.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_background, 0, 0)
                backgroundTextView?.text = staticText.text_background
                editTextTextView?.text = staticText.text_edit_text
                editTextTextView?.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_edit_text, 0, 0)
            } else {
                backgroundTextView?.visibility = View.GONE
                backgroundTextView?.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_background, 0, 0)
                editTextTextView?.text = staticText.text_change_product
                editTextTextView?.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_change_product, 0, 0)
            }
            shareTextView?.text = staticText.text_share
            shareTextView?.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_social_media_template_share, 0, 0)
            whatsappTextView?.text = staticText.text_whatsapp
            whatsappTextView?.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.ic_social_media_template_whatsapp, 0, 0)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: mMarketingPageInfoResponse :: $mMarketingPageInfoResponse")
        Log.d(TAG, "onViewCreated: mSocialMediaTemplateResponse :: $mSocialMediaTemplateResponse")
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            headerTitle = mHeadingStr
            setSideIconVisibility(false)
            onBackPressed(this@EditSocialMediaTemplateFragment)
        }
        if (mIsOpenBottomSheet) {
            showProgressDialog(mActivity)
            if (true == mMarketingPageInfoResponse?.marketingStoreInfo?.isStoreItemLimitExceeds) mService?.getProductCategories() else mService?.getItemsBasicDetailsByStoreId()
        } else {
            qrCodeScreenshotView?.visibility = View.VISIBLE
            setupEditAndShareTemplateUI()
        }
    }

    override fun onEditSocialMediaTemplateErrorResponse(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onItemsBasicDetailsByStoreIdResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val listType = object : TypeToken<List<ProductCategoryCombineResponse>>() {}.type
            mProductCategoryCombineList = ArrayList()
            mProductCategoryCombineList = Gson().fromJson<ArrayList<ProductCategoryCombineResponse>>(response.mCommonDataStr, listType)
            Log.d(TAG, "onItemsBasicDetailsByStoreIdResponse: productCategoryCombineList :: $mProductCategoryCombineList")
            if (isEmpty(mProductCategoryCombineList))
                setupNoTemplateUI()
            else {
                setupEditAndShareTemplateUI()
                val productCategoryTempCombineList: ArrayList<ProductCategoryCombineResponse> = ArrayList()
                mProductCategoryCombineList?.forEachIndexed { _, categoryCombineResponse -> productCategoryTempCombineList.add(categoryCombineResponse) }
                mProductCategoryCombineList = ArrayList()
                productCategoryTempCombineList.forEachIndexed { _, categoryResponse ->
                    val productsList: ArrayList<ProductResponse> = ArrayList()
                    categoryResponse.productsList?.forEachIndexed { _, productsResponse ->
                        if (ToolBarManager.getInstance().headerTitle.equals(mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_product_discount, true)) {
                            if (productsResponse.discountedPrice != productsResponse.price) productsList.add(productsResponse)
                        } else {
                            productsList.add(productsResponse)
                        }
                    }
                    if (isNotEmpty(productsList)) {
                        mProductCategoryCombineList?.add(ProductCategoryCombineResponse(categoryResponse.category, productsList))
                    }
                }
                showProductsWithCategoryBottomSheet()
            }
        }
        stopProgress()
    }

    override fun onProductCategoryResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val productCategoryResponse = Gson().fromJson<AddProductStoreCategory>(response.mCommonDataStr, AddProductStoreCategory::class.java)
                mAddProductStoreCategoryList = productCategoryResponse?.storeCategoriesList
                Log.d(TAG, "onProductCategoryResponse: $productCategoryResponse")
                if (isEmpty(mAddProductStoreCategoryList))
                    setupNoTemplateUI()
                else {
                    Log.d(TAG, "onProductCategoryResponse: mAddProductStoreCategoryList :: $mAddProductStoreCategoryList")
                    mProductCategoryCombineList = ArrayList()
                    mAddProductStoreCategoryList?.forEachIndexed { position, categoryItem ->
                        initiateProductsApiCall(categoryItem, position, mAddProductStoreCategoryList?.size ?: 0)
                    }
                }
            }
        }
        stopProgress()
    }

    private fun setupNoTemplateUI() {
        noTemplateLayout?.visibility = View.VISIBLE
        templateLayout?.visibility = View.GONE
        addProductTextView?.text = mMarketingPageInfoResponse?.marketingStaticTextResponse?.cta_text_add_products
        messageTextView?.text = when (ToolBarManager.getInstance().headerTitle) {
            mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_product_discount -> mMarketingPageInfoResponse?.marketingStaticTextResponse?.message_product_discount_zero_screen
            mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_new_launches_and_bestsellers -> mMarketingPageInfoResponse?.marketingStaticTextResponse?.message_bestseller_zero_screen
            else -> ""
        }
    }

    private fun initiateProductsApiCall(item: StoreCategoryItem, position: Int, count: Int) {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val response = RetrofitApi().getServerCallObject()?.getProductsByCategoryId(item.id)
                response?.let {
                    if (it.isSuccessful) {
                        it.body()?.let {
                            CoroutineScopeUtils().runTaskOnCoroutineMain {
                                if (it.mIsSuccessStatus) {
                                    val listType = object : TypeToken<ArrayList<ProductResponse>>() {}.type
                                    val tempProductsList: ArrayList<ProductResponse> = ArrayList()
                                    val productsList = Gson().fromJson<ArrayList<ProductResponse>>(it.mCommonDataStr, listType)
                                    if (ToolBarManager.getInstance().headerTitle.equals(mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_product_discount, true)) {
                                        productsList?.forEachIndexed { _, productResponse -> if (productResponse.discountedPrice != productResponse.price && isNotEmpty(productResponse.imageUrl)) tempProductsList.add(productResponse) }
                                    } else {
                                        productsList?.forEachIndexed { _, productResponse -> tempProductsList.add(productResponse) }
                                    }
                                    if (isNotEmpty(tempProductsList))
                                        mProductCategoryCombineList?.add(ProductCategoryCombineResponse(item, tempProductsList))
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "initiateProductsApiCall: ${e.message}", e)
            }
            Log.d(TAG, "initiateProductsApiCall: comparison :: position :: $position , count :: $count")
            if (position == (count - 1)) {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    if (isNotEmpty(mProductCategoryCombineList)) showProductsWithCategoryBottomSheet() else setupNoTemplateUI()
                }
            }
        }
    }

    override fun onSocialMediaTemplateBackgroundsResponse(response: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val listType = object : TypeToken<List<TemplateBackgroundItemResponse>>() {}.type
                mTemplateBackgroundList = ArrayList()
                mTemplateBackgroundList = Gson().fromJson<ArrayList<TemplateBackgroundItemResponse>>(response.mCommonDataStr, listType)
                showBackgroundBottomSheet()
            }
        }
    }

    override fun onSaveSocialMediaPostResponse(commonApiResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            showShortSnackBar(commonApiResponse.mMessage, true, if (commonApiResponse.mIsSuccessStatus) R.drawable.ic_check_circle else R.drawable.ic_close_red_small)
        }
    }

    private fun showProductsWithCategoryBottomSheet() {
        setupBottomNavViewFromStaticText()
        if (isEmpty(mProductCategoryCombineList)) {
            setupNoTemplateUI()
        } else {
            noTemplateLayout?.visibility = View.GONE
            templateLayout?.visibility = View.VISIBLE
            mActivity?.let {
                mCategoryBottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_show_products_with_category, it.findViewById(R.id.bottomSheetContainer))
                mCategoryBottomSheetDialog?.apply {
                    behavior.skipCollapsed = true
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
                    setOnDismissListener {
                        Handler(Looper.getMainLooper()).postDelayed({ hideSoftKeyboard() }, Constants.TIMER_DELAY)
                        if (!mIsItemSelectedFromBottomSheet) mActivity?.onBackPressed()
                    }
                    setContentView(view)
                    view.run {
                        val headingTextView: TextView = findViewById(R.id.headingTextView)
                        val editText: EditText = findViewById(R.id.editText)
                        val searchProductRecyclerView: RecyclerView = findViewById(R.id.searchProductRecyclerView)
                        if (ToolBarManager.getInstance().headerTitle.equals(mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_product_discount, true)) {
                            val headingStr = mMarketingPageInfoResponse?.marketingStaticTextResponse?.message_please_note ?: ""
                            exclamationImageView?.visibility = View.VISIBLE
                            headingTextView.visibility = View.VISIBLE
                            headingTextView.setHtmlData(headingStr)
                        } else {
                            exclamationImageView?.visibility = View.GONE
                            headingTextView.visibility = View.GONE
                        }
                        editText.hint = mMarketingPageInfoResponse?.marketingStaticTextResponse?.hint_search_product
                        val categoryProductAdapter = CategoryProductAdapter(mActivity, mMarketingPageInfoResponse?.marketingStaticTextResponse, mProductCategoryCombineList, this@EditSocialMediaTemplateFragment)
                        editText.addTextChangedListener(object : TextWatcher {

                            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                            override fun afterTextChanged(editable: Editable?) {
                                val str = editable?.toString() ?: ""
                                when {
                                    str.length >= (mActivity?.resources?.getInteger(R.integer.catalog_search_char_count) ?: 3) -> {
                                        CoroutineScopeUtils().runTaskOnCoroutineBackground {
                                            try {
                                                val response = RetrofitApi().getServerCallObject()?.searchItems(SearchCatalogItemsRequest(1, str))
                                                response?.let { res ->
                                                    if (res.isSuccessful) {
                                                        res.body()?.let { body ->
                                                            CoroutineScopeUtils().runTaskOnCoroutineMain {
                                                                if (body.mIsSuccessStatus) {
                                                                    val tempProductsList: ArrayList<ProductResponse> = ArrayList()
                                                                    val searchProductsResponse = Gson().fromJson<SearchProductsResponse>(body.mCommonDataStr, SearchProductsResponse::class.java)
                                                                    val productsList = searchProductsResponse?.productList
                                                                    if (ToolBarManager.getInstance().headerTitle.equals(mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_product_discount, true)) {
                                                                        productsList?.forEachIndexed { _, productResponse -> if (productResponse.discountedPrice != productResponse.price && isNotEmpty(productResponse.imageUrl)) tempProductsList.add(productResponse) }
                                                                    } else {
                                                                        productsList?.forEachIndexed { _, productResponse -> tempProductsList.add(productResponse) }
                                                                    }
                                                                    val productCategoryCombineResponseList: ArrayList<ProductCategoryCombineResponse> = ArrayList()
                                                                    productCategoryCombineResponseList.add(ProductCategoryCombineResponse(StoreCategoryItem(0, mActivity?.getString(R.string.search_results), false), tempProductsList))
                                                                    categoryProductAdapter.setProductCategoryList(productCategoryCombineResponseList)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } catch (e: Exception) {
                                                Log.e(TAG, "initiateProductsApiCall: ${e.message}", e)
                                            }
                                        }
                                    }
                                    isEmpty(str) -> categoryProductAdapter.setProductCategoryList(mProductCategoryCombineList)
                                    else -> categoryProductAdapter.setProductCategoryList(null)
                                }
                            }

                        })
                        searchProductRecyclerView.apply {
                            layoutManager = LinearLayoutManager(mActivity)
                            isNestedScrollingEnabled = false
                            adapter = categoryProductAdapter
                        }
                    }
                }?.show()
            }
        }
    }

    private fun showEditTemplateBottomSheet() {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_edit_product_category_details, it.findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
                view.run {
                    val closeImageView: View = findViewById(R.id.closeImageView)
                    val editTextTextView: TextView = findViewById(R.id.editTextTextView)
                    val text1InputLayout: TextInputLayout = findViewById(R.id.text1InputLayout)
                    val text2InputLayout: TextInputLayout = findViewById(R.id.text2InputLayout)
                    val saveChangesTextView: TextView = findViewById(R.id.saveChangesTextView)
                    val text1EditText: EditText = findViewById(R.id.text1EditText)
                    val text2EditText: EditText = findViewById(R.id.text2EditText)
                    text1EditText.setMaxLength(mSocialMediaTemplateResponse?.html?.htmlDefaults?.text1?.maxLength ?: 0)
                    text2EditText.setMaxLength(mSocialMediaTemplateResponse?.html?.htmlDefaults?.text2?.maxLength ?: 0)
                    text1EditText.setText(mSocialMediaTemplateResponse?.html?.htmlDefaults?.text1?.name)
                    text2EditText.setText(mSocialMediaTemplateResponse?.html?.htmlDefaults?.text2?.name)
                    saveChangesTextView.text = mMarketingPageInfoResponse?.marketingStaticTextResponse?.text_save_changes
                    editTextTextView.text = mMarketingPageInfoResponse?.marketingStaticTextResponse?.text_edit_text
                    text1InputLayout.hint = mMarketingPageInfoResponse?.marketingStaticTextResponse?.text_line_1
                    text2InputLayout.hint = mMarketingPageInfoResponse?.marketingStaticTextResponse?.text_line_2
                    closeImageView.setOnClickListener { bottomSheetDialog.dismiss() }
                    text2InputLayout.visibility = if (null == mSocialMediaTemplateResponse?.html?.htmlDefaults?.text2) View.GONE else View.VISIBLE
                    saveChangesTextView.setOnClickListener {
                        val text1Str = text1EditText.text.toString().trim()
                        val text2Str = text2EditText.text.toString().trim()
                        mSocialMediaTemplateResponse?.html?.htmlDefaults?.text1?.name = text1Str
                        mSocialMediaTemplateResponse?.html?.htmlDefaults?.text2?.name = text2Str
                        mWebViewUrl = "$EDIT_TEMPLATE_WEB_VIEW_URL?store_name=${mMarketingPageInfoResponse?.marketingStoreInfo?.name}&html=${URLEncoder.encode(Gson().toJson(mSocialMediaTemplateResponse?.html), "utf-8")}"
                        if (null != mSelectedBackgroundItem) {
                            mWebViewUrl += "&background=${URLEncoder.encode(Gson().toJson(mSelectedBackgroundItem), "utf-8")}"
                        }
                        Log.d(TAG, "showEditTemplateBottomSheet :: mWebViewUrl :: $mWebViewUrl")
                        editTemplateWebView?.loadUrl(mWebViewUrl)
                        mIsTemplateEdited = true
                        bottomSheetDialog.dismiss()
                    }
                }
            }.show()
        }
    }

    private fun showBackgroundBottomSheet() {
        mActivity?.let {
            val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_social_media_template_background, it.findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                view.run {
                    val closeImageView: View = findViewById(R.id.closeImageView)
                    val editBackgroundTextView: TextView = findViewById(R.id.editBackgroundTextView)
                    val backgroundColorRecyclerView: RecyclerView = findViewById(R.id.backgroundColorRecyclerView)
                    editBackgroundTextView.text = mMarketingPageInfoResponse?.marketingStaticTextResponse?.text_edit_background
                    closeImageView.setOnClickListener { bottomSheetDialog.dismiss() }
                    backgroundColorRecyclerView.apply {
                        layoutManager = GridLayoutManager(mActivity, 3)
                        adapter = TemplateBackgroundAdapter(mActivity, mTemplateBackgroundList, object : IAdapterItemClickListener {

                            override fun onAdapterItemClickListener(position: Int) {
                                mTemplateBackgroundList?.forEachIndexed { _, itemResponse -> itemResponse.isSelected = false }
                                mSelectedBackgroundItem = null
                                mSelectedBackgroundItem = mTemplateBackgroundList?.get(position)
                                mSelectedBackgroundItem?.isSelected = true
                                val newWebViewUrlWithBg = "$mWebViewUrl&background=${URLEncoder.encode(Gson().toJson(mSelectedBackgroundItem))}"
                                Log.d(TAG, "showBackgroundBottomSheet :: onAdapterItemClickListener: newWebViewUrlWithBg :: $newWebViewUrlWithBg")
                                editTemplateWebView?.loadUrl(newWebViewUrlWithBg)
                                mIsTemplateEdited = true
                                bottomSheetDialog.dismiss()
                            }
                        })
                    }
                }
            }.show()
        }
    }

    override fun onProductItemClickListener(productItem: ProductResponse?) {
        mIsItemSelectedFromBottomSheet = true
        mCategoryBottomSheetDialog?.dismiss()
        productShareScreenshotView?.visibility = View.VISIBLE
        qrCodeScreenshotView?.visibility = View.GONE
        val orderAtStr = "${mMarketingPageInfoResponse?.marketingStaticTextResponse?.text_order_at} : ${mMarketingPageInfoResponse?.marketingStoreInfo?.domain}"
        storeNameTextView?.text = mMarketingPageInfoResponse?.marketingStoreInfo?.name
        storeLinkTextView?.text = orderAtStr
        mSelectedProductFromBottomSheet = productItem
        productNameTextView?.text = productItem?.name
        productDescriptionTextView?.text = productItem?.description
        val productImageView: ImageView? = mContentView?.findViewById(R.id.productImageView)
        val promoCodeTextView: TextView? = mContentView?.findViewById(R.id.promoCodeTextView)
        val discountedPriceTextView: TextView? = mContentView?.findViewById(R.id.discountedPriceTextView)
        val originalPriceTextView: TextView? = mContentView?.findViewById(R.id.originalPriceTextView)
        val discount = ceil(((((productItem?.price ?: 0.0) - (productItem?.discountedPrice ?: 0.0)) / (productItem?.price ?: 0.0)) * 100)).toInt()
        val discountStr = if (productItem?.price == productItem?.discountedPrice) null else "$discount% OFF"
        promoCodeTextView?.text = discountStr
        var priceStr: String
        priceStr = "???${productItem?.discountedPrice}"
        originalPriceTextView?.text = priceStr
        discountedPriceTextView?.apply {
            if (isNotEmpty(discountStr)) {
                visibility = View.VISIBLE
                showStrikeOffText()
                priceStr = "???${productItem?.price}"
                text = priceStr
            } else visibility = View.GONE
        }
        mActivity?.let { context ->
            productImageView?.let { view -> Glide.with(context).load(productItem?.imageUrl).into(view) }
        }
        val saleImageView: ImageView? = mContentView?.findViewById(R.id.saleImageView)
        if (ToolBarManager.getInstance().headerTitle == mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_product_discount) {
            with(View.VISIBLE) {
                saleImageView?.visibility = this
                percentageTextView?.visibility = this
                offTextView?.visibility = this
                saleTextView?.visibility = this
            }
            bestsellerTextView?.visibility = View.GONE
            mActivity?.let { context -> saleImageView?.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_sale_background)) }
            val discountPercentStr = "$discount%"
            percentageTextView?.text = discountPercentStr
        } else {
            with(View.GONE) {
                saleImageView?.visibility = this
                percentageTextView?.visibility = this
                offTextView?.visibility = this
                saleTextView?.visibility = this
            }
            bestsellerTextView?.visibility = View.VISIBLE
        }
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            editTextTextView?.id -> {
                if (editTextTextView?.text == mMarketingPageInfoResponse?.marketingStaticTextResponse?.text_change_product) {
                    showProductsWithCategoryBottomSheet()
                } else {
                    showEditTemplateBottomSheet()
                }
            }
            backgroundTextView?.id -> {
                if (isEmpty(mTemplateBackgroundList)) {
                    showProgressDialog(mActivity)
                    mService?.getSocialMediaTemplateBackgrounds(mSocialMediaTemplateResponse?.id ?: "")
                } else showBackgroundBottomSheet()
            }
            addProductTextView?.id -> launchFragment(AddProductFragment.newInstance(0, true), true)
            whatsappTextView?.id -> {
                if (StaticInstances.sIsShareStoreLocked) {
                    getLockedStoreShareDataServerCall(if (mIsOpenBottomSheet) Constants.MODE_SHARE_PRODUCTS else Constants.MODE_SHARE_TEMPLATE)
                    return
                }
                sIsWhatsAppIconClicked = true
                showCancellableProgressDialog(mActivity)
                Handler(Looper.getMainLooper()).postDelayed({
                    nestedScrollView?.scrollTo(0, 150)
                    stopProgress()
                }, Constants.TIMER_AUTO_DISMISS_PROGRESS_DIALOG)
                screenshotContainer?.let { v ->
                    val originalBitmap = getBitmapFromView(v, mActivity)
                    originalBitmap?.let { bitmap -> shareOnWhatsApp("Order From - ${mMarketingPageInfoResponse?.marketingStoreInfo?.domain}", bitmap) }
                }
                val request = SaveSocialMediaPostRequest(
                    shareType       = Constants.SOCIAL_MEDIA_SHARE_TYPE_WHATSAPP,
                    htmlDefaults    = mSocialMediaTemplateResponse?.html?.htmlDefaults,
                    templateId      = if (Constants.SOCIAL_MEDIA_TEMPLATE_TYPE_BUSINESS == mTemplateTypeStr) mSocialMediaTemplateResponse?.id?.toInt() ?: 0 else  mSelectedProductFromBottomSheet?.id ?: 0,
                    templateType    = mTemplateTypeStr,
                    isEdited        = mIsTemplateEdited
                )
                mService?.saveSocialMediaPost(request)
            }
            shareTextView?.id -> {
                if (StaticInstances.sIsShareStoreLocked) {
                    getLockedStoreShareDataServerCall(if (mIsOpenBottomSheet) Constants.MODE_SHARE_PRODUCTS else Constants.MODE_SHARE_TEMPLATE)
                    return
                }
                sIsWhatsAppIconClicked = false
                showCancellableProgressDialog(mActivity)
                Handler(Looper.getMainLooper()).postDelayed({
                    nestedScrollView?.scrollTo(0, 150)
                    stopProgress()
                }, Constants.TIMER_AUTO_DISMISS_PROGRESS_DIALOG)
                screenshotContainer?.let { v ->
                    val originalBitmap = getBitmapFromView(v, mActivity)
                    originalBitmap?.let { bitmap -> shareData("Order From - ${mMarketingPageInfoResponse?.marketingStoreInfo?.domain}", bitmap) }
                }
                val request = SaveSocialMediaPostRequest(
                    shareType       = Constants.SOCIAL_MEDIA_SHARE_TYPE_SHARE,
                    htmlDefaults    = mSocialMediaTemplateResponse?.html?.htmlDefaults,
                    templateId      = if (Constants.SOCIAL_MEDIA_TEMPLATE_TYPE_BUSINESS == mTemplateTypeStr) mSocialMediaTemplateResponse?.id?.toInt() ?: 0 else  mSelectedProductFromBottomSheet?.id ?: 0,
                    templateType    = mTemplateTypeStr,
                    isEdited        = mIsTemplateEdited
                )
                mService?.saveSocialMediaPost(request)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        Log.i(TAG, "onRequestPermissionResult")
        if (Constants.REQUEST_CODE_STORAGE == requestCode) {
            when {
                grantResults.isEmpty() -> Log.d(TAG, "User interaction was cancelled.")
                PackageManager.PERMISSION_GRANTED == grantResults[0] -> {
                    if (sIsWhatsAppIconClicked) whatsappTextView?.callOnClick() else shareTextView?.callOnClick()
                }
                else -> showShortSnackBar("Permission was denied", true, R.drawable.ic_close_red)
            }
        }
    }

    override fun onLockedStoreShareSuccessResponse(lockedShareResponse: LockedStoreShareResponse) = showLockedStoreShareBottomSheet(lockedShareResponse)

}