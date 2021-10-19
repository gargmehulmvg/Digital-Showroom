package com.digitaldukaan.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.R
import com.digitaldukaan.adapters.CategoryProductAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IProductItemClickListener
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.EditSocialMediaTemplateService
import com.digitaldukaan.services.serviceinterface.IEditSocialMediaTemplateServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_add_product_fragment.*
import kotlinx.android.synthetic.main.layout_edit_premium_fragment.*
import kotlinx.android.synthetic.main.layout_edit_social_media_template_fragment.*
import java.util.*
import kotlin.collections.ArrayList

class EditSocialMediaTemplateFragment : BaseFragment(), IEditSocialMediaTemplateServiceInterface, IProductItemClickListener {

    private var mSocialMediaTemplateResponse: SocialMediaTemplateListItemResponse? = null
    private var mService: EditSocialMediaTemplateService? = null
    private var mIsOpenBottomSheet = false
    private var mHeadingStr = ""
    private var mMarketingPageInfoResponse: MarketingPageInfoResponse? = null
    private var mAddProductStoreCategoryList: ArrayList<StoreCategoryItem>? = ArrayList()
    private var mIsStoreItemLimitExceeds = false
    private var mCategoryBottomSheetDialog: BottomSheetDialog? = null

    companion object {
        private const val TAG = "EditSocialMediaTemplateFragment"

        fun newInstance(heading: String?, item: SocialMediaTemplateListItemResponse?, isOpenBottomSheet: Boolean = false, marketingPageInfoResponse: MarketingPageInfoResponse?): EditSocialMediaTemplateFragment {
            val fragment = EditSocialMediaTemplateFragment()
            fragment.mSocialMediaTemplateResponse = item
            fragment.mHeadingStr = heading ?: ""
            fragment.mIsOpenBottomSheet = isOpenBottomSheet
            fragment.mMarketingPageInfoResponse = marketingPageInfoResponse
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_edit_social_media_template_fragment, container, false)
        hideBottomNavigationView(true)
        WebViewBridge.mWebViewListener = this
        mService = EditSocialMediaTemplateService()
        mService?.setEditSocialMediaTemplateServiceListener(this)
        return mContentView
    }

    private fun setupUIWithQRCode() {
        templateLayout?.visibility = View.VISIBLE
        noTemplateLayout?.visibility = View.GONE
        webView?.apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            addJavascriptInterface(WebViewBridge(), "Android")
            var url = mSocialMediaTemplateResponse?.html?.htmlText ?: ""
            url = url.replace("id=\"business_creative_storename\"> Store Name</div>", "id=\"business_creative_storename\">${mMarketingPageInfoResponse?.marketingStoreInfo?.name}</div>")
            Log.d(TAG, "onViewCreated: text :: $url")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    Log.d(TAG, "onPageFinished: called")
                    stopProgress()
                }

                override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean { return onCommonWebViewShouldOverrideUrlLoading(url, view) }
            }

            loadData(url, "text/html", "UTF-8")
            val domain = mMarketingPageInfoResponse?.marketingStoreInfo?.domain
            screenshotStoreNameTextView?.text = domain
            getQRCodeBitmap(mActivity, domain)?.let { b -> screenshotQRImageView?.setImageBitmap(b) }
        }
        mMarketingPageInfoResponse?.marketingStaticTextResponse?.let { staticText ->
            backgroundTextView?.text = staticText.text_background
            editTextTextView?.text = staticText.text_edit_text
            shareTextView?.text = staticText.text_share
            whatsappTextView?.text = staticText.text_whatsapp
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
//            if (true == mMarketingPageInfoResponse?.marketingStoreInfo?.isStoreItemLimitExceeds) mService?.getProductCategories() else mService?.getItemsBasicDetailsByStoreId()
            mService?.getProductCategories()
        } else {
            setupUIWithQRCode()
        }
    }

    override fun onEditSocialMediaTemplateErrorResponse(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onItemsBasicDetailsByStoreIdResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
        }
    }

    override fun onProductCategoryResponse(response: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (response.mIsSuccessStatus) {
                val productCategoryResponse = Gson().fromJson<AddProductStoreCategory>(response.mCommonDataStr, AddProductStoreCategory::class.java)
                mAddProductStoreCategoryList = productCategoryResponse?.storeCategoriesList
                Log.d(TAG, "onProductCategoryResponse: $productCategoryResponse")
                if (isEmpty(mAddProductStoreCategoryList)) {
                    noTemplateLayout?.visibility = View.VISIBLE
                    templateLayout?.visibility = View.GONE
                    addProductTextView?.text = mMarketingPageInfoResponse?.marketingStaticTextResponse?.cta_text_add_products
                    messageTextView?.text = when(ToolBarManager.getInstance().headerTitle) {
                        mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_product_discount -> mMarketingPageInfoResponse?.marketingStaticTextResponse?.message_product_discount_zero_screen
                        mMarketingPageInfoResponse?.marketingStaticTextResponse?.heading_new_launches_and_bestsellers -> mMarketingPageInfoResponse?.marketingStaticTextResponse?.message_bestseller_zero_screen
                        else -> ""
                    }
                } else showCategoryBottomSheet()
            }
        }
    }

    private fun showCategoryBottomSheet() {
        noTemplateLayout?.visibility = View.GONE
        templateLayout?.visibility = View.VISIBLE
        mActivity?.let {
            mCategoryBottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_category_product, it.findViewById(R.id.bottomSheetContainer))
            mCategoryBottomSheetDialog?.apply {
                setContentView(view)
                view.run {
                    val headingTextView: TextView = findViewById(R.id.headingTextView)
                    val editText: EditText = findViewById(R.id.editText)
                    val searchProductRecyclerView: RecyclerView = findViewById(R.id.searchProductRecyclerView)
                    headingTextView.setHtmlData(mMarketingPageInfoResponse?.marketingStaticTextResponse?.message_please_note)
                    editText.hint = mMarketingPageInfoResponse?.marketingStaticTextResponse?.hint_search_product
                    searchProductRecyclerView.apply {
                        layoutManager = LinearLayoutManager(mActivity)
                        adapter = CategoryProductAdapter(mActivity, mMarketingPageInfoResponse?.marketingStaticTextResponse, mAddProductStoreCategoryList, this@EditSocialMediaTemplateFragment)
                    }
                }
            }?.show()
        }
    }

    override fun onProductItemClickListener(productItem: ProductResponse?) {
        mCategoryBottomSheetDialog?.dismiss()
        showToast(productItem?.name)
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            addProductTextView?.id -> launchFragment(AddProductFragment.newInstance(0, true), true)
            whatsappTextView?.id -> {
                screenshotContainer?.let { v ->
                    val originalBitmap = getBitmapFromView(v, mActivity)
                    originalBitmap?.let { bitmap -> shareOnWhatsApp("Order From - ${mMarketingPageInfoResponse?.marketingStoreInfo?.domain}", bitmap) }
                }
            }
            shareTextView?.id -> {
                screenshotContainer?.let { v ->
                    val originalBitmap = getBitmapFromView(v, mActivity)
                    originalBitmap?.let { bitmap -> shareData("Order From - ${mMarketingPageInfoResponse?.marketingStoreInfo?.domain}", bitmap) }
                }
            }
        }
    }
}