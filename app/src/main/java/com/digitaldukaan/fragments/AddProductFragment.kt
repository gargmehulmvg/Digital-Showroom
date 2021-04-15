package com.digitaldukaan.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.digitaldukaan.R
import com.digitaldukaan.adapters.AddProductsChipsAdapter
import com.digitaldukaan.adapters.AddProductsImagesAdapter
import com.digitaldukaan.adapters.ImagesSearchAdapter
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.CoroutineScopeUtils
import com.digitaldukaan.constants.ToolBarManager
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.interfaces.IChipItemClickListener
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.models.request.AddProductImageItem
import com.digitaldukaan.models.request.AddProductItemCategory
import com.digitaldukaan.models.request.AddProductRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.AddProductService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IAddProductServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import kotlinx.android.synthetic.main.layout_add_product_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File


class AddProductFragment : BaseFragment(), IAddProductServiceInterface, IAdapterItemClickListener,
    IChipItemClickListener, IOnToolbarIconClick, PopupMenu.OnMenuItemClickListener {

    private lateinit var mService: AddProductService
    private var addProductBannerStaticDataResponse: AddProductBannerTextResponse? = null
    private var addProductStaticData: AddProductStaticText? = null
    private var mItemId = 0
    private val mImagesListStr: ArrayList<String> = ArrayList()
    private lateinit var imagePickBottomSheet: BottomSheetDialog
    private var imageAdapter = ImagesSearchAdapter()
    private var mImageChangePosition = 0
    private var mAddProductStoreCategoryList: ArrayList<AddStoreCategoryItem>? = ArrayList()
    private lateinit var addProductChipsAdapter: AddProductsChipsAdapter
    private var mOptionsMenuResponse: ArrayList<TrendingListResponse>? = null

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
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_add_product_fragment, container, false)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            onBackPressed(this@AddProductFragment)
            setSideIconVisibility(true)
            setSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_options_menu), this@AddProductFragment)
        }
        hideBottomNavigationView(true)
        val discountPriceEditText: EditText = mContentView.findViewById(R.id.discountedPriceEditText)
        val priceEditText: EditText = mContentView.findViewById(R.id.priceEditText)
        val originalPriceTextView: TextView = mContentView.findViewById(R.id.originalPriceTextView)
        val discountedPriceTextView: TextView = mContentView.findViewById(R.id.discountedPriceTextView)
        val pricePercentageOffTextView: TextView = mContentView.findViewById(R.id.pricePercentageOffTextView)
        discountPriceEditText.addTextChangedListener(object : TextWatcher {
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
                        str.toDouble() == 0.0 -> {
                            discountedPriceTextView.visibility = View.INVISIBLE
                            originalPriceTextView.text = null
                            discountedPriceTextView.text = null
                            pricePercentageOffTextView.text = null
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
        showProgressDialog(mActivity)
        mImagesListStr.add(0, "")
        mService.getItemInfo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), mItemId)
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
                    mService.getAddOrderBottomSheetData()
                } else showMaterCatalogBottomSheet(addProductBannerStaticDataResponse, addProductStaticData)
            }
            continueTextView.id -> {
                if (checkValidation()) {
                    val nameStr = nameEditText.text.toString()
                    val priceStr = priceEditText.text.trim().toString()
                    val discountedStr = discountedPriceEditText.text.trim().toString()
                    val descriptionStr = productDescriptionEditText.text.toString()
                    val categoryStr = enterCategoryEditText.text.toString()
                    if (!isInternetConnectionAvailable(mActivity)) return else {
                        showProgressDialog(mActivity)
                        val imageListRequest:ArrayList<AddProductImageItem> = ArrayList()
                        mImagesListStr.forEachIndexed { _, str -> imageListRequest.add(
                            AddProductImageItem(0, str, 1)
                        ) }
                        mImagesListStr.removeAt(0)
                        val request = AddProductRequest(
                            0,
                            1,
                            priceStr.toDouble(),
                            discountedStr.toDouble(),
                            descriptionStr,
                            AddProductItemCategory(0, categoryStr),
                            imageListRequest,
                            nameStr
                        )
                        mService.setItem(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
                    }
                }
            }
            updateCameraImageView.id -> {
                showAddProductImagePickerBottomSheet(0)
            }
            updateCameraTextView.id -> {
                showAddProductImagePickerBottomSheet(0)
            }
        }
    }

    private fun showAddProductImagePickerBottomSheet(position: Int) {
        imagePickBottomSheet = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_image_pick, mActivity.findViewById(R.id.bottomSheetContainer))
        imagePickBottomSheet.apply {
            setContentView(view)
            //setBottomSheetCommonProperty()
            view?.run {
                val bottomSheetUploadImageCloseImageView: ImageView = findViewById(R.id.bottomSheetUploadImageCloseImageView)
                val bottomSheetUploadImageHeading: TextView = findViewById(R.id.bottomSheetUploadImageHeading)
                val bottomSheetUploadImageCameraTextView: TextView = findViewById(R.id.bottomSheetUploadImageCameraTextView)
                val bottomSheetUploadImageGalleryTextView: TextView = findViewById(R.id.bottomSheetUploadImageGalleryTextView)
                val bottomSheetUploadImageSearchHeading: TextView = findViewById(R.id.bottomSheetUploadImageSearchHeading)
                val bottomSheetUploadImageRemovePhotoTextView: TextView = findViewById(R.id.bottomSheetUploadImageRemovePhotoTextView)
                val searchImageEditText: EditText = findViewById(R.id.searchImageEditText)
                val searchImageImageView: View = findViewById(R.id.searchImageImageView)
                val bottomSheetUploadImageRemovePhoto: View = findViewById(R.id.bottomSheetUploadImageRemovePhoto)
                val searchImageRecyclerView: RecyclerView = findViewById(R.id.searchImageRecyclerView)
                bottomSheetUploadImageGalleryTextView.text = addProductStaticData?.bottom_sheet_add_from_gallery
                bottomSheetUploadImageSearchHeading.text = addProductStaticData?.bottom_sheet_you_can_add_upto_4_images
                bottomSheetUploadImageRemovePhotoTextView.text = addProductStaticData?.bottom_sheet_remove_image
                bottomSheetUploadImageHeading.text = addProductStaticData?.bottom_sheet_add_image
                bottomSheetUploadImageCameraTextView.text = addProductStaticData?.bottom_sheet_take_a_photo
                searchImageEditText.hint = addProductStaticData?.bottom_sheet_hint_search_for_images_here
                bottomSheetUploadImageCloseImageView.setOnClickListener { if (imagePickBottomSheet.isShowing) imagePickBottomSheet.dismiss() }
                bottomSheetUploadImageRemovePhotoTextView.visibility = if (position == 0) View.GONE else View.VISIBLE
                bottomSheetUploadImageRemovePhoto.visibility = if (position == 0) View.GONE else View.VISIBLE
                bottomSheetUploadImageCameraTextView.setOnClickListener {
                    imagePickBottomSheet.dismiss()
                    openCamera()
                }
                bottomSheetUploadImageCameraTextView.setOnClickListener {
                    imagePickBottomSheet.dismiss()
                    openCamera()
                }
                bottomSheetUploadImageGalleryTextView.setOnClickListener {
                    imagePickBottomSheet.dismiss()
                    openGallery()
                }
                bottomSheetUploadImageGalleryTextView.setOnClickListener {
                    imagePickBottomSheet.dismiss()
                    openGallery()
                }
                bottomSheetUploadImageRemovePhoto.setOnClickListener {
                    imagePickBottomSheet.dismiss()
                    onImageSelectionResultFile(null)
                }
                bottomSheetUploadImageRemovePhotoTextView.setOnClickListener {
                    imagePickBottomSheet.dismiss()
                    onImageSelectionResultFile(null)
                }
                imageAdapter.setSearchImageListener(this@AddProductFragment)
                searchImageImageView.setOnClickListener {
                    if (searchImageEditText.text.trim().toString().isEmpty()) {
                        searchImageEditText.error = getString(R.string.mandatory_field_message)
                        searchImageEditText.requestFocus()
                        return@setOnClickListener
                    }
                    showProgressDialog(mActivity)
                    CoroutineScopeUtils().runTaskOnCoroutineBackground {
                        val response = RetrofitApi().getServerCallObject()?.searchImagesFromBing(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), searchImageEditText.text.trim().toString(), getStringDataFromSharedPref(Constants.STORE_ID))
                        response?.let {
                            if (it.isSuccessful) {
                                it.body()?.let {
                                    withContext(Dispatchers.Main) {
                                        stopProgress()
                                        val list = it.mImagesList
                                        searchImageRecyclerView.apply {
                                            layoutManager = GridLayoutManager(mActivity, 3)
                                            adapter = imageAdapter
                                            imageAdapter.setSearchImageList(list)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }.show()
    }

    override fun onImageSelectionResultFile(file: File?) {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        if (file == null) {
            showShortSnackBar("Something went wrong", true, R.drawable.ic_close_red)
            return
        }
        showProgressDialog(mActivity)
        val fileRequestBody = MultipartBody.Part.createFormData("image", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
        val imageTypeRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), Constants.BASE64_STORE_ITEMS)
        mService.generateCDNLink(imageTypeRequestBody, fileRequestBody)
        if (::imagePickBottomSheet.isInitialized) imagePickBottomSheet.dismiss()
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
            addProductBannerStaticDataResponse?.run { showMaterCatalogBottomSheet(addProductBannerStaticDataResponse, addProductStaticData) }
        }
    }

    override fun onGetAddProductDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            val addProductResponse = Gson().fromJson<AddProductResponse>(commonResponse.mCommonDataStr, AddProductResponse::class.java)
            addProductStaticData = addProductResponse?.addProductStaticText
            addProductResponse?.storeItem?.run {
                nameEditText.setText(name)
                priceEditText.setText(price.toString())
                discountedPriceEditText.setText(discountedPrice.toString())
                if (addProductResponse.storeItem.imagesList?.isNotEmpty() == true) {
                    noImagesLayout.visibility = View.GONE
                    imagesRecyclerView.apply {
                        layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                        val list:ArrayList<String> = ArrayList()
                        addProductResponse.storeItem.imagesList?.forEachIndexed { _, imagesResponse ->
                            if (imagesResponse.status != 0) list.add(imagesResponse.imageUrl)
                        }
                        adapter = AddProductsImagesAdapter(list ,addProductStaticData?.text_images_added, this@AddProductFragment)
                    }
                } else {
                    imagesRecyclerView.visibility = View.GONE
                    noImagesLayout.visibility = View.VISIBLE
                }
                if (description.isNotEmpty()) {
                    addItemTextView.visibility = View.GONE
                    productDescriptionInputLayout.visibility = View.VISIBLE
                    productDescriptionEditText.setText(description)
                }
            }
            addProductResponse?.addProductStoreCategories?.run {
                chipGroupRecyclerView.apply {
                    layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
                    mAddProductStoreCategoryList = addProductResponse.addProductStoreCategories?.storeCategoriesList
                    if (mAddProductStoreCategoryList?.isNotEmpty() == true)
                        //mAddProductStoreCategoryList?.get(1)?.isSelected = true
                    addProductChipsAdapter = AddProductsChipsAdapter(mAddProductStoreCategoryList, this@AddProductFragment)
                    adapter = addProductChipsAdapter
                }
            }
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
                val count = addProductResponse?.storeItem?.imagesList?.size ?: 0
                imagesLeftTextView.text = "${if (count == 0) count else count - 1}/4 $text_images_added"
            }
            mOptionsMenuResponse = addProductResponse?.addProductStoreOptionsMenu
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

    override fun onConvertFileToLinkResponse(commonResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_check_circle)
                val base64Str = Gson().fromJson<String>(commonResponse.mCommonDataStr, String::class.java)
                if (mImagesListStr.size == 1 || mImageChangePosition == 0) mImagesListStr.add(base64Str) else if (mImageChangePosition != 0) {
                    mImagesListStr.removeAt(mImageChangePosition)
                    mImagesListStr.add(mImageChangePosition, base64Str)
                }
                noImagesLayout.visibility = View.GONE
                imagesRecyclerView.visibility = View.VISIBLE
                imagesRecyclerView.apply {
                    layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                    val addImageAdapter = AddProductsImagesAdapter(
                        mImagesListStr,
                        addProductStaticData?.text_images_added,
                        this@AddProductFragment
                    )
                    adapter = addImageAdapter
                    addImageAdapter.setListToAdapter(mImagesListStr)
                }
                imagesLeftTextView.text = "${mImagesListStr?.size -1}/4 ${addProductStaticData?.text_images_added}"
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onDeleteItemResponse(commonResponse: CommonApiResponse) {
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

    override fun onAdapterItemClickListener(position: Int) {
        mImageChangePosition = position
        showAddProductImagePickerBottomSheet(position)
    }

    override fun onSearchImageItemClicked(photoStr: String) {
        super.onSearchImageItemClicked(photoStr)
        Log.d(AddProductFragment::class.java.simpleName, "onSearchImageItemClicked: do nothing")
    }

    override fun onChipItemClickListener(position: Int) {
        mAddProductStoreCategoryList?.forEachIndexed { _, categoryItem -> categoryItem.isSelected = false }
        mAddProductStoreCategoryList?.get(position)?.isSelected = true
        enterCategoryEditText.setText(mAddProductStoreCategoryList?.get(position)?.name)
        addProductChipsAdapter.setAddProductStoreCategoryList(mAddProductStoreCategoryList)
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
        if (0 == item?.itemId) openUrlInBrowser(mOptionsMenuResponse?.get(0)?.mPage)
        return true
    }
}