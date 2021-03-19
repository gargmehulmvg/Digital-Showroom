package com.digitaldukaan.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.response.AddProductBannerTextResponse
import com.digitaldukaan.models.response.CommonApiResponse
import com.digitaldukaan.services.AddProductService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IAddProductServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.add_product_fragment.*

class AddProductFragment : BaseFragment(), IAddProductServiceInterface {

    private lateinit var mService: AddProductService
    private var addProductBannerStaticDataResponse: AddProductBannerTextResponse? = null

    companion object {
        fun newInstance(): AddProductFragment {
            return AddProductFragment()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mService = AddProductService()
        mService.setServiceListener(this)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.add_product_fragment, container, false)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            onBackPressed(this@AddProductFragment)
        }
        hideBottomNavigationView(true)
        return mContentView
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            addItemTextView.id -> {
                addItemTextView.visibility = View.GONE
                productDescriptionInputLayout.visibility = View.VISIBLE
            }
            tryNowTextView.id -> {
                if (addProductBannerStaticDataResponse == null) {
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return
                    }
                    showProgressDialog(mActivity)
                    mService.getAddOrderBottomSheetData(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
                } else showMaterCatalogBottomSheet()
            }
        }
    }

    override fun onAddProductBannerStaticDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            addProductBannerStaticDataResponse = Gson().fromJson<AddProductBannerTextResponse>(commonResponse.mCommonDataStr, AddProductBannerTextResponse::class.java)
            addProductBannerStaticDataResponse?.run { showMaterCatalogBottomSheet() }
        }
    }

    override fun onAddProductException(e: Exception) {
        exceptionHandlingForAPIResponse(e)
    }

    private fun showMaterCatalogBottomSheet() {
        val bottomSheetDialog = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(
            R.layout.bottom_sheet_add_products_catalog_builder,
            mActivity.findViewById(R.id.bottomSheetContainer)
        )
        bottomSheetDialog.apply {
            setContentView(view)
            setBottomSheetCommonProperty()
            view.run {
                val closeImageView: View = findViewById(R.id.closeImageView)
                val offerTextView: TextView = findViewById(R.id.offerTextView)
                val headerTextView: TextView = findViewById(R.id.headerTextView)
                val bodyTextView: TextView = findViewById(R.id.bodyTextView)
                val bannerImageView: ImageView = findViewById(R.id.bannerImageView)
                val buttonTextView: TextView = findViewById(R.id.buttonTextView)
                offerTextView.text = addProductBannerStaticDataResponse?.offer
                headerTextView.text = addProductBannerStaticDataResponse?.header
                bodyTextView.text = addProductBannerStaticDataResponse?.body
                buttonTextView.text = addProductBannerStaticDataResponse?.button_text
                Picasso.get().load(addProductBannerStaticDataResponse?.image_url).into(bannerImageView)
                closeImageView.setOnClickListener { bottomSheetDialog.dismiss() }
                buttonTextView.setOnClickListener{
                    showToast()
                }
            }
        }.show()
        bottomSheetDialog.show()
    }

}