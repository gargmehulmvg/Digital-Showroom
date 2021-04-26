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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.adapters.AddProductsChipsAdapter
import com.digitaldukaan.adapters.AddProductsImagesAdapter
import com.digitaldukaan.adapters.ImagesSearchAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.interfaces.IChipItemClickListener
import com.digitaldukaan.interfaces.IOnToolbarIconClick
import com.digitaldukaan.interfaces.IOnToolbarSecondIconClick
import com.digitaldukaan.models.request.AddProductImageItem
import com.digitaldukaan.models.request.AddProductItemCategory
import com.digitaldukaan.models.request.AddProductRequest
import com.digitaldukaan.models.request.DeleteItemRequest
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
import java.util.*


class AddProductFragment : BaseFragment(), IAddProductServiceInterface, IAdapterItemClickListener,
    IChipItemClickListener, IOnToolbarIconClick, PopupMenu.OnMenuItemClickListener,
    IOnToolbarSecondIconClick {

    private lateinit var mService: AddProductService
    private var addProductBannerStaticDataResponse: AddProductBannerTextResponse? = null
    private var addProductStaticData: AddProductStaticText? = null
    private var mItemId = 0
    private val mImagesStrList: ArrayList<AddProductImagesResponse> = ArrayList()
    private lateinit var imagePickBottomSheet: BottomSheetDialog
    private var imageAdapter = ImagesSearchAdapter()
    private var mImageChangePosition = 0
    private var mAddProductStoreCategoryList: ArrayList<AddStoreCategoryItem>? = ArrayList()
    private val mTempProductCategoryList: ArrayList<AddStoreCategoryItem> = ArrayList()
    private lateinit var addProductChipsAdapter: AddProductsChipsAdapter
    private var mOptionsMenuResponse: ArrayList<TrendingListResponse>? = null
    private var mImageAddAdapter: AddProductsImagesAdapter? = null
    private var mIsAddNewProduct: Boolean = false
    private var mProductNameStr: String? = ""
    private var mIsOrderEdited = false

    companion object {
        private const val TAG = "AddProductFragment"
        fun newInstance(itemId:Int, isAddNewProduct: Boolean): AddProductFragment {
            val fragment = AddProductFragment()
            fragment.mItemId = itemId
            fragment.mIsAddNewProduct = isAddNewProduct
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mService = AddProductService()
        mService.setServiceListener(this)
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_ADD_ITEM,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.IS_MERCHANT to "1"
            )
        )
    }

    override fun onStop() {
        super.onStop()
        ToolBarManager.getInstance().apply {
            setSecondSideIconVisibility(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mContentView = inflater.inflate(R.layout.layout_add_product_fragment, container, false)
        ToolBarManager.getInstance().apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            onBackPressed(this@AddProductFragment)
            setSideIconVisibility(true)
            setSecondSideIconVisibility(mItemId != 0)
            setSecondSideIcon(ContextCompat.getDrawable(mActivity, R.drawable.ic_delete), this@AddProductFragment)
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
                            originalPriceTextView.text = "${addProductStaticData?.text_rupees_symbol} ${priceStr.toDouble()}"
                            discountedPriceTextView.text = "${addProductStaticData?.text_rupees_symbol} ${str.toDouble()}"
                            originalPriceTextView.showStrikeOffText()
                            val priceDouble = priceStr.toDouble()
                            val discountedPriceDouble = str.toDouble()
                            val percentage = ((priceDouble - discountedPriceDouble) / priceDouble) * 100
                            pricePercentageOffTextView.text = "${percentage.toInt()}% OFF"
                        }
                    }
                } else {
                    originalPriceTextView.text = null
                    pricePercentageOffTextView.text = null
                    discountedPriceTextView.visibility = View.INVISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                showAddProductContainer()
            }

        })
        showProgressDialog(mActivity)
        mImagesStrList.add(0, AddProductImagesResponse(0,"", 0))
        mService.getItemInfo(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), mItemId)
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        handleVisibilityTextWatcher()
    }

    private fun handleVisibilityTextWatcher() {
        priceEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val str = s?.toString()
                if (str?.trim()?.isNotEmpty() == true) showAddProductContainer()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(TAG, "beforeTextChanged: priceEditText :: do nothing")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: priceEditText :: do nothing")
            }

        })
        nameEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mProductNameStr = s?.toString()
                val str = s?.toString()
                if (str?.trim()?.isNotEmpty() == true) showAddProductContainer()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(TAG, "beforeTextChanged: nameEditText :: do nothing")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: nameEditText :: do nothing")
            }

        })
        productDescriptionEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val str = s?.toString()
                if (str?.trim()?.isNotEmpty() == true) showAddProductContainer()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(TAG, "beforeTextChanged: productDescriptionEditText :: do nothing")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: productDescriptionEditText :: do nothing")
            }

        })
        enterCategoryEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val str = s?.toString()
                if (str?.trim()?.isNotEmpty() == true) showAddProductContainer()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                Log.d(TAG, "beforeTextChanged: enterCategoryEditText :: do nothing")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                Log.d(TAG, "onTextChanged: enterCategoryEditText :: do nothing")
            }

        })
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            addItemTextView.id -> {
                addItemTextView.visibility = View.GONE
                productDescriptionInputLayout.visibility = View.VISIBLE
                productDescriptionEditText.requestFocus()
                productDescriptionEditText.isFocusable = true
            }
            tryNowTextView.id -> {
                if (addProductBannerStaticDataResponse == null) {
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return
                    }
                    showProgressDialog(mActivity)
                    mService.getAddOrderBottomSheetData()
                } else showMaterCatalogBottomSheet(addProductBannerStaticDataResponse, addProductStaticData, Constants.MODE_ADD_PRODUCT)
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
                        val imageListRequest: ArrayList<AddProductImageItem> = ArrayList()
                        mImagesStrList.forEachIndexed { _, imageItem ->
                            if (imageItem.imageUrl.isNotEmpty()) {
                                imageListRequest.add(AddProductImageItem(imageItem.imageId, imageItem.imageUrl, 1))
                            }
                        }
                        val request = AddProductRequest(
                            mItemId,
                            1,
                            priceStr.toDouble(),
                            if (discountedStr.isNotEmpty()) discountedStr.toDouble() else 0.0,
                            descriptionStr,
                            if (categoryStr.trim().isEmpty()) AddProductItemCategory(0, "") else AddProductItemCategory(0, categoryStr),
                            imageListRequest,
                            nameStr
                        )
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_SAVE_ITEM,
                            isCleverTapEvent = true, isAppFlyerEvent = false, isServerCallEvent = true,
                            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                AFInAppEventParameterName.IMAGES to Gson().toJson(mImagesStrList),
                                AFInAppEventParameterName.NAME to mProductNameStr,
                                AFInAppEventParameterName.AVAILABLE to "1",
                                AFInAppEventParameterName.DESCRIPTION to descriptionStr,
                                AFInAppEventParameterName.CATEGORY to categoryStr,
                                AFInAppEventParameterName.DISCOUNTED_PRICE to discountedStr,
                                AFInAppEventParameterName.PRICE to priceStr
                            )
                        )
                        mService.setItem(getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN), request)
                    }
                }
            }
            updateCameraImageView.id -> {
                showAddProductContainer()
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
            view?.run {
                val bottomSheetUploadImageCloseImageView: ImageView = findViewById(R.id.bottomSheetUploadImageCloseImageView)
                val bottomSheetUploadImageHeading: TextView = findViewById(R.id.bottomSheetUploadImageHeading)
                val bottomSheetUploadImageCameraTextView: TextView = findViewById(R.id.bottomSheetUploadImageCameraTextView)
                val bottomSheetUploadImageCamera: View = findViewById(R.id.bottomSheetUploadImageCamera)
                val bottomSheetUploadImageGalleryTextView: TextView = findViewById(R.id.bottomSheetUploadImageGalleryTextView)
                val bottomSheetUploadImageGallery: View = findViewById(R.id.bottomSheetUploadImageGallery)
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
                mProductNameStr?.run { searchImageEditText.setText(mProductNameStr) }
                bottomSheetUploadImageCloseImageView.setOnClickListener { if (imagePickBottomSheet.isShowing) imagePickBottomSheet.dismiss() }
                bottomSheetUploadImageRemovePhotoTextView.visibility = if (position == 0) View.GONE else View.VISIBLE
                bottomSheetUploadImageRemovePhoto.visibility = if (position == 0) View.GONE else View.VISIBLE
                bottomSheetUploadImageCamera.setOnClickListener {
                    imagePickBottomSheet.dismiss()
                    openCamera()
                }
                bottomSheetUploadImageCameraTextView.setOnClickListener {
                    imagePickBottomSheet.dismiss()
                    openCamera()
                }
                bottomSheetUploadImageGallery.setOnClickListener {
                    imagePickBottomSheet.dismiss()
                    openGallery()
                }
                bottomSheetUploadImageGalleryTextView.setOnClickListener {
                    imagePickBottomSheet.dismiss()
                    openGallery()
                }
                bottomSheetUploadImageRemovePhoto.setOnClickListener {
                    imagePickBottomSheet.dismiss()
                    mImagesStrList.removeAt(mImageChangePosition)
                    mImageAddAdapter?.setListToAdapter(mImagesStrList)
                }
                bottomSheetUploadImageRemovePhotoTextView.setOnClickListener {
                    imagePickBottomSheet.dismiss()
                    mImagesStrList.removeAt(mImageChangePosition)
                    mImageAddAdapter?.setListToAdapter(mImagesStrList)
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
                        val response = RetrofitApi().getServerCallObject()?.searchImagesFromBing(searchImageEditText.text.trim().toString(), getStringDataFromSharedPref(Constants.STORE_ID))
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

    override fun onImageSelectionResultFile(file: File?, mode: String) {
        if (!isInternetConnectionAvailable(mActivity)) {
            showNoInternetConnectionDialog()
            return
        }
        if (null == file) {
            showShortSnackBar("Something went wrong", true, R.drawable.ic_close_red)
            return
        }
        showProgressDialog(mActivity)
        val fileRequestBody = MultipartBody.Part.createFormData("media", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
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
            discountedPriceEditText.text.toString().isNotEmpty() && (priceEditText.text.toString().toDouble() < discountedPriceEditText.text.toString().toDouble()) -> {
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
            addProductBannerStaticDataResponse?.run { showMaterCatalogBottomSheet(addProductBannerStaticDataResponse, addProductStaticData, Constants.MODE_ADD_PRODUCT) }
        }
    }

    override fun onGetAddProductDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            val addProductResponse = Gson().fromJson<AddProductResponse>(commonResponse.mCommonDataStr, AddProductResponse::class.java)
            Log.d(TAG, "onGetAddProductDataResponse: $addProductResponse")
            addProductStaticData = addProductResponse?.addProductStaticText
            addProductResponse?.storeItem?.run {
                nameEditText.setText(name)
                priceEditText.setText(if (price != 0.0) price.toString() else "")
                discountedPriceEditText.setText(if (discountedPrice != 0.0) discountedPrice.toString() else "")
                if (addProductResponse.storeItem?.imagesList?.isNotEmpty() == true) {
                    noImagesLayout.visibility = View.GONE
                    imagesRecyclerView.apply {
                        layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                        mImagesStrList.clear()
                        if (mImagesStrList.isEmpty()) mImagesStrList.add(AddProductImagesResponse(0, "", 0))
                        addProductResponse.storeItem?.imagesList?.forEachIndexed { _, imagesResponse ->
                            if (imagesResponse.status != 0) mImagesStrList.add(AddProductImagesResponse(imagesResponse.imageId, imagesResponse.imageUrl, 1))
                        }
                        mImageAddAdapter = AddProductsImagesAdapter(mImagesStrList ,addProductStaticData?.text_images_added, this@AddProductFragment)
                        adapter = mImageAddAdapter
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
                    if (mAddProductStoreCategoryList?.isNotEmpty() == true) {
                        mTempProductCategoryList.clear()
                        mAddProductStoreCategoryList?.forEachIndexed { _, categoryItem ->
                            if (categoryItem.name?.isNotEmpty() == true) mTempProductCategoryList.add(categoryItem)
                        }
                        mTempProductCategoryList.forEachIndexed { _, categoryItem ->
                            if (addProductResponse.storeItem?.category?.id == categoryItem.id) {
                                enterCategoryEditText.setText(categoryItem.name)
                                categoryItem.isSelected = true
                            } else categoryItem.isSelected = false
                        }
                        addProductChipsAdapter = AddProductsChipsAdapter(mTempProductCategoryList, this@AddProductFragment)
                        adapter = addProductChipsAdapter
                    }
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
                val count = mImagesStrList.size
                imagesLeftTextView.text = "${if (count == 0) count else count - 1}/4 $text_images_added"
            }
            mOptionsMenuResponse = addProductResponse?.addProductStoreOptionsMenu
            shareProductContainer.setOnClickListener {
                val sharingData = "ItemName: ${addProductResponse?.storeItem?.name}\nPrice:  ₹${addProductResponse?.storeItem?.price} \nDiscounted Price: ₹${addProductResponse.storeItem?.discountedPrice}\n\n\uD83D\uDED2 ORDER NOW, Click on the link below\n\n" + "${BuildConfig.WEB_VIEW_SHOW_ROOM_URL}${addProductResponse?.domain}/${addProductResponse?.storeItem?.id}/${addProductResponse.storeItem?.name?.replace(' ', '-')}"
                if (addProductResponse?.storeItem?.imageUrl?.isEmpty() == true) shareDataOnWhatsApp(sharingData) else shareDataOnWhatsAppWithImage(sharingData ,addProductResponse?.storeItem?.imageUrl)
            }
            shareProductContainer.visibility = if (mIsAddNewProduct) View.GONE else View.VISIBLE
            continueTextView.visibility = if (mIsAddNewProduct) View.VISIBLE else View.GONE
        }
    }

    override fun onAddProductDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            if (commonResponse.mIsSuccessStatus) {
                showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_check_circle)
                fragmentManager?.popBackStack()
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onConvertFileToLinkResponse(commonResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (commonResponse.mIsSuccessStatus) {
                showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_check_circle)
                val base64Str = Gson().fromJson<String>(commonResponse.mCommonDataStr, String::class.java)
                if (mImagesStrList.size == 1 || mImageChangePosition == 0) {
                    mImagesStrList.add(AddProductImagesResponse(0, base64Str, 1))
                } else if (mImageChangePosition != 0) {
                    val imageResponse = mImagesStrList[mImageChangePosition]
                    imageResponse.imageUrl = base64Str
                    mImagesStrList[mImageChangePosition] = imageResponse
                }
                noImagesLayout.visibility = View.GONE
                imagesRecyclerView.visibility = View.VISIBLE
                imagesRecyclerView.apply {
                    layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                    mImageAddAdapter = AddProductsImagesAdapter(
                        mImagesStrList,
                        addProductStaticData?.text_images_added,
                        this@AddProductFragment
                    )
                    adapter = mImageAddAdapter
                    mImageAddAdapter?.setListToAdapter(mImagesStrList)
                }
                imagesLeftTextView.text = "${mImagesStrList?.size -1}/4 ${addProductStaticData?.text_images_added}"
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
        showAddProductContainer()
        showAddProductImagePickerBottomSheet(position)
    }

    override fun onSearchImageItemClicked(photoStr: String) {
        super.onSearchImageItemClicked(photoStr)
        Log.d(AddProductFragment::class.java.simpleName, "onSearchImageItemClicked: do nothing")
    }

    override fun onChipItemClickListener(position: Int) {
        mTempProductCategoryList.forEachIndexed { _, categoryItem -> categoryItem.isSelected = false }
        mTempProductCategoryList[position].isSelected = true
        enterCategoryEditText.setText(mTempProductCategoryList[position].name)
        addProductChipsAdapter.setAddProductStoreCategoryList(mTempProductCategoryList)
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
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_BULK_UPLOAD_ITEMS,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.CHANNEL to "Catalog"
            )
        )
        if (0 == item?.itemId) openUrlInBrowser(mOptionsMenuResponse?.get(0)?.mPage)
        return true
    }

    override fun onToolbarSecondIconClicked() {
        showDeleteConfirmationDialog()
    }

    override fun onBackPressed(): Boolean {
        return if (mIsOrderEdited) {
            showGoBackDialog()
            return true
        } else false
    }

    private fun showGoBackDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val builder: AlertDialog.Builder = AlertDialog.Builder(mActivity)
            builder.apply {
                setTitle(addProductStaticData?.text_go_back)
                setMessage(addProductStaticData?.text_go_back_message)
                setCancelable(true)
                setNegativeButton(getString(R.string.text_no)) { dialog, _ ->
                    dialog.dismiss()
                }
                setPositiveButton(getString(R.string.txt_yes)) { dialog, _ ->
                    mIsOrderEdited = false
                    fragmentManager?.popBackStack()
                    dialog.dismiss()
                }
            }.create().show()
        }
    }

    private fun showDeleteConfirmationDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val builder: AlertDialog.Builder = AlertDialog.Builder(mActivity)
            builder.apply {
                setTitle(getString(R.string.delete_product))
                setMessage(getString(R.string.are_you_sure_to_delete))
                setCancelable(false)
                setPositiveButton(getString(R.string.txt_yes)) { dialog, _ ->
                    dialog.dismiss()
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return@setPositiveButton
                    }
                    showProgressDialog(mActivity)
                    mService.deleteItemServerCall(DeleteItemRequest(mItemId))
                }
                setNegativeButton(getString(R.string.text_no)) { dialog, _ -> dialog.dismiss() }
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }
    }

    private fun showAddProductContainer() {
        mIsOrderEdited = true
        if (shareProductContainer.visibility == View.VISIBLE) {
            shareProductContainer.visibility = View.GONE
            continueTextView.visibility = View.VISIBLE
        }
    }
}