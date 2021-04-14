package com.digitaldukaan.fragments

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.SharePDFAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.constants.openWebViewFragment
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.response.*
import com.digitaldukaan.services.ProductService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IProductServiceInterface
import com.digitaldukaan.webviews.WebViewBridge
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.product_fragment.*
import org.json.JSONObject

class ProductFragment : BaseFragment(), IProductServiceInterface, IOnToolbarIconClick,
    PopupMenu.OnMenuItemClickListener {

    private lateinit var mService: ProductService
    private var mOptionsMenuResponse: ArrayList<TrendingListResponse>? = null
    private var mShareDataOverWhatsAppText = ""
    private var addProductBannerStaticDataResponse: AddProductBannerTextResponse? = null

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
        WebViewBridge.mWebViewListener = this
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.product_fragment, container, false)
        if (!isInternetConnectionAvailable(mActivity)) showNoInternetConnectionDialog() else mService.getProductPageInfo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            onBackPressed(this@ProductFragment)
            setSideIconVisibility(true)
            setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_options_menu), this@ProductFragment)
        }
        hideBottomNavigationView(false)
        return mContentView
    }

    override fun onAddProductBannerStaticDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            addProductBannerStaticDataResponse = Gson().fromJson<AddProductBannerTextResponse>(commonResponse.mCommonDataStr, AddProductBannerTextResponse::class.java)
            addProductBannerStaticDataResponse?.run { showMaterCatalogBottomSheet(addProductBannerStaticDataResponse, addProductStaticData) }
        }
    }

    override fun onProductResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            val productResponse = Gson().fromJson(commonResponse.mCommonDataStr, ProductPageResponse::class.java)
            addProductStaticData = productResponse?.static_text
            var url: String
            ToolBarManager.getInstance().setHeaderTitle(productResponse?.static_text?.product_page_heading)
            mOptionsMenuResponse = productResponse?.optionMenuList
            commonWebView.apply {
                webViewClient = CommonWebViewFragment.WebViewController()
                settings.javaScriptEnabled = true
                addJavascriptInterface(WebViewBridge(), "Android")
                url = BuildConfig.WEB_VIEW_URL + productResponse.product_page_url + "?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&" + "&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"
                loadUrl(url)
            }
            productResponse.shareShop.run {
                shareButtonTextView.text = this.mText
            }
            productResponse.addProduct.run {
                addProductTextView.text = this.mText
            }
            showCancellableProgressDialog(mActivity)
            Log.d(ProductFragment::class.simpleName, "onViewCreated: $url")
            Handler(Looper.getMainLooper()).postDelayed({ stopProgress() }, Constants.TIMER_INTERVAL)
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
                    mService.generateProductStorePdf(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
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
            addProductContainer.id -> launchFragment(AddProductFragment.newInstance(0), true)
            shareProductContainer.id -> {
                if (mShareDataOverWhatsAppText.isNotEmpty()) shareDataOnWhatsApp(mShareDataOverWhatsAppText) else if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return
                } else {
                    showProgressDialog(mActivity)
                    mService.getProductShareStoreData(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
                }
            }
        }
    }

    override fun onToolbarSideIconClicked() {
        val sideView:View = mActivity.findViewById(R.id.sideIconToolbar)
        val optionsMenu = PopupMenu(mActivity, sideView)
        optionsMenu.inflate(R.menu.menu_product_fragment)
        mOptionsMenuResponse?.forEachIndexed { position, response ->
            val menuOption = optionsMenu.menu?.add(Menu.NONE, position, Menu.NONE, response.mText)
            /*if (response.mCDN?.isNotEmpty() == true) {
                Picasso.get().load(response.mCDN).into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        bitmap?.let {
                            val drawableIcon: Drawable = BitmapDrawable(resources, bitmap)
                            menuOption?.icon = drawableIcon
                        }
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                        Log.d("TAG", "onPrepareLoad: ")
                    }

                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                        Log.d("TAG", "onBitmapFailed: ")
                    }
                })
            }*/
        }
        optionsMenu.setOnMenuItemClickListener(this)
        optionsMenu.show()
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId) {
            0 -> openWebViewFragment(this, "", BuildConfig.WEB_VIEW_URL + mOptionsMenuResponse?.get(0)?.mPage)
            1 -> openWebViewFragment(this, "", BuildConfig.WEB_VIEW_URL + mOptionsMenuResponse?.get(1)?.mPage)
            2 -> {
                if (!isInternetConnectionAvailable(mActivity)) {
                    showNoInternetConnectionDialog()
                    return true
                }
                showProgressDialog(mActivity)
                mService.getProductSharePDFTextData(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
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
            } else showMaterCatalogBottomSheet(addProductBannerStaticDataResponse, addProductStaticData)
        }
    }

}