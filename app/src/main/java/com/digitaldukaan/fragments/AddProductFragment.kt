package com.digitaldukaan.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.digitaldukaan.R
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.models.request.AddProductRequest
import com.digitaldukaan.models.response.AddProductBannerTextResponse
import com.digitaldukaan.models.response.AddProductResponse
import com.digitaldukaan.models.response.AddProductStaticText
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
    private var addProductStaticData: AddProductStaticText? = null
    private var mItemId = 0

    companion object {
        fun newInstance(itemId:Int): AddProductFragment {
            val fragment = AddProductFragment()
            fragment.mItemId = itemId
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mService = AddProductService()
        mService.setServiceListener(this)
        showProgressDialog(mActivity)
        mService.getItemInfo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), mItemId)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.add_product_fragment, container, false)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            onBackPressed(this@AddProductFragment)
        }
        hideBottomNavigationView(true)
        val discountPriceEditText: EditText = mContentView.findViewById(R.id.discountedPriceEditText)
        val priceEditText: EditText = mContentView.findViewById(R.id.priceEditText)
        val originalPriceTextView: TextView = mContentView.findViewById(R.id.originalPriceTextView)
        val discountedPriceTextView: TextView = mContentView.findViewById(R.id.discountedPriceTextView)
        val pricePercentageOffTextView: TextView = mContentView.findViewById(R.id.pricePercentageOffTextView)
        discountPriceEditText.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(s: Editable?) {
                val str = s?.toString()
                if (str?.isNotEmpty() == true) {
                    val priceStr = priceEditText.text.toString()
                    when {
                        priceStr.isEmpty() -> {
                            priceEditText.error = addProductStaticData?.error_mandatory_field
                            priceEditText.requestFocus()
                            discountPriceEditText.text = null
                            return
                        }
                        priceStr.toDouble() < str.toDouble() -> {
                            discountPriceEditText.error = addProductStaticData?.error_discount_price_less_then_original_price
                            discountPriceEditText.text = null
                            discountPriceEditText.requestFocus()
                        }
                        else -> {
                            discountedPriceTextView.visibility = View.VISIBLE
                            originalPriceTextView.text = "${addProductStaticData?.text_rupees_symbol}${priceStr.toDouble()}"
                            discountedPriceTextView.text = "${addProductStaticData?.text_rupees_symbol}${str.toDouble()}"
                            originalPriceTextView.showStrikeOffText()
                            val percentage = (str.toDouble() / priceStr.toDouble()) * 100
                            pricePercentageOffTextView.text = "$percentage% OFF"
                        }
                    }
                } else {
                    originalPriceTextView.text = null
                    discountedPriceTextView.visibility = View.INVISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })
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
            continueTextView.id -> {
                if (checkValidation()) {
                    val nameStr = nameEditText.text.trim().toString()
                    val priceStr = priceEditText.text.trim().toString()
                    val discountedStr = discountedPriceEditText.text.trim().toString()
                    val descriptionStr = productDescriptionEditText.text.trim().toString()
                    if (!isInternetConnectionAvailable(mActivity)) return else {
                        showProgressDialog(mActivity)
                        val request = AddProductRequest(
                            0,
                            1,
                            priceStr.toDouble(),
                            discountedStr.toDouble(),
                            descriptionStr,
                            nameStr
                        )
                        mService.setItem(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
                    }
                }
            }
        }
    }

    private fun checkValidation(): Boolean {
        when {
            nameEditText.text.trim().isEmpty() -> {
                nameEditText.error = addProductStaticData?.error_mandatory_field
                nameEditText.requestFocus()
                return false
            }
            priceEditText.text.trim().isEmpty() -> {
                priceEditText.error = addProductStaticData?.error_mandatory_field
                priceEditText.requestFocus()
                return false
            }
            discountedPriceEditText.text.trim().isEmpty() -> {
                discountedPriceEditText.error = addProductStaticData?.error_mandatory_field
                discountedPriceEditText.requestFocus()
                return false
            }
            priceEditText.text.toString().toDouble() < discountedPriceEditText.text.toString().toDouble() -> {
                discountedPriceEditText.text = null
                discountedPriceEditText.error = addProductStaticData?.error_discount_price_less_then_original_price
                discountedPriceEditText.requestFocus()
                return false
            }
            else -> return true
        }
    }

    override fun onAddProductBannerStaticDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            addProductBannerStaticDataResponse = Gson().fromJson<AddProductBannerTextResponse>(commonResponse.mCommonDataStr, AddProductBannerTextResponse::class.java)
            addProductBannerStaticDataResponse?.run { showMaterCatalogBottomSheet() }
        }
    }

    override fun onGetAddProductDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            val addProductResponse = Gson().fromJson<AddProductResponse>(commonResponse.mCommonDataStr, AddProductResponse::class.java)
            addProductStaticData = addProductResponse?.addProductStaticText
            addProductStaticData?.run {
                ToolBarManager.getInstance().setHeaderTitle(heading_add_product_page)
                tryNowTextView.text = text_try_now
                textView2.text = heading_add_product_banner
                updateCameraTextView.text = text_upload_or_search_images
                nameInputLayout.hint = hint_item_name
                priceInputLayout.hint = hint_price
                discountedPriceInputLayout.hint = hint_discounted_price
                enterCategoryInputLayout.hint = hint_enter_category_optional
                addItemTextView.text = text_add_item_description
                continueTextView.text = text_add_item
            }
        }
    }

    override fun onAddProductDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_check_circle)
                mActivity.onBackPressed()
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
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
                headerTextView.setHtmlData(addProductBannerStaticDataResponse?.header)
                bodyTextView.text = addProductBannerStaticDataResponse?.body
                buttonTextView.text = addProductBannerStaticDataResponse?.button_text
                Picasso.get().load(addProductBannerStaticDataResponse?.image_url).into(bannerImageView)
                closeImageView.setOnClickListener { bottomSheetDialog.dismiss() }
                buttonTextView.setOnClickListener{
                    showToast()
                }
            }
        }.show()
    }

}