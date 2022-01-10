package com.digitaldukaan.fragments

import android.Manifest
import android.app.Dialog
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.*
import com.digitaldukaan.R
import com.digitaldukaan.adapters.*
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.*
import com.digitaldukaan.models.dto.InventoryEnableDTO
import com.digitaldukaan.models.request.AddProductItemCategory
import com.digitaldukaan.models.request.AddProductRequest
import com.digitaldukaan.models.request.DeleteItemRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.network.RetrofitApi
import com.digitaldukaan.services.AddProductService
import com.digitaldukaan.services.isInternetConnectionAvailable
import com.digitaldukaan.services.serviceinterface.IAddProductServiceInterface
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.layout_add_product_fragment.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class AddProductFragment : BaseFragment(), IAddProductServiceInterface, IAdapterItemClickListener,
    IChipItemClickListener, IOnToolbarIconClick, PopupMenu.OnMenuItemClickListener,
    IOnToolbarSecondIconClick {

    private var mService: AddProductService? = null
    private var mAddProductBannerStaticDataResponse: AddProductBannerTextResponse? = null
    private var mAddProductStaticData: AddProductStaticText? = null
    private var mItemId = 0
    private var mItemCategoryId = 0
    private val mImagesStrList: ArrayList<AddProductImagesResponse> = ArrayList()
    private var imagePickBottomSheet: BottomSheetDialog? = null
    private var mImageSearchAdapter = ImagesSearchAdapter()
    private var mImageChangePosition = 0
    private var mAddProductStoreCategoryList: ArrayList<StoreCategoryItem>? = ArrayList()
    private val mTempProductCategoryList: ArrayList<StoreCategoryItem> = ArrayList()
    private var addProductChipsAdapter: AddProductsChipsAdapter? = null
    private var mOptionsMenuResponse: ArrayList<TrendingListResponse>? = null
    private var mImageAddAdapter: AddProductsImagesAdapter? = null
    private var mProductNameStr: String? = ""
    private var mProductPriceStr: String? = ""
    private var mProductDiscountedPriceStr: String? = ""
    private var mProductDescriptionPriceStr: String? = ""
    private var mIsAddNewProduct: Boolean = false
    private var mIsOrderEdited = false
    private var mIsOrderDeleted = false
    private var mIsVariantAvailable = false
    private var mIsManageInventoryAvailable = false
    private var mIsManageInventoryToggleChanged = false
    private var mAddProductResponse: AddProductResponse? = null
    private var discountPriceEditText: EditText? = null
    private var productDescriptionEditText: EditText? = null
    private var enterCategoryEditText: EditText? = null
    private var priceEditText: EditText? = null
    private var nameEditText: EditText? = null
    private var continueTextView: TextView? = null
    private var originalPriceTextView: TextView? = null
    private var discountedPriceTextView: TextView? = null
    private var pricePercentageOffTextView: TextView? = null
    private var imagesLeftTextView: TextView? = null
    private var addDiscountLabel: TextView? = null
    private var addItemTextView: TextView? = null
    private var addVariantsTextView: TextView? = null
    private var shareProductContainer: View? = null
    private var noImagesLayout: View? = null
    private var discountContainer: View? = null
    private var variantRecyclerView: RecyclerView? = null
    private var imagesRecyclerView: RecyclerView? = null
    private var chipGroupRecyclerView: RecyclerView? = null
    private var productDescriptionInputLayout: TextInputLayout? = null
    private var mActiveVariantAdapter: ActiveVariantAdapterV2? = null
    private var mActiveVariantList: ArrayList<VariantItemResponse>? = null
    private var mDeletedVariantList: ArrayList<VariantItemResponse>? = null
    private var mYoutubeItemResponse: AddProductImagesResponse? = null
    private var mInventoryAdapter: InventoryEnableAdapter? = null

    companion object {

        private var sIsVariantImageClicked = false
        private var sVariantImageClickedPosition = 0

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
        mService?.setServiceListener(this)
        AppEventsManager.pushAppEvents(eventName = AFInAppEventType.EVENT_ADD_ITEM, isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true, data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.IS_MERCHANT to "1"
        ))
    }

    override fun onStop() {
        super.onStop()
        ToolBarManager.getInstance()?.apply {
            setSecondSideIconVisibility(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        TAG = "AddProductFragment"
        FirebaseCrashlytics.getInstance().apply { setCustomKey("screen_tag", TAG) }
        mContentView = inflater.inflate(R.layout.layout_add_product_fragment, container, false)
        mIsManageInventoryAvailable = StaticInstances.sPermissionHashMap?.get(Constants.MANAGE_INVENTORY) ?: false
        ToolBarManager.getInstance()?.apply {
            hideToolBar(mActivity, false)
            hideBackPressFromToolBar(mActivity, false)
            onBackPressed(this@AddProductFragment)
            setSideIconVisibility(true)
            setSecondSideIconVisibility(mItemId != 0)
            mActivity?.run {
                setSecondSideIcon(ContextCompat.getDrawable(this, R.drawable.ic_delete), this@AddProductFragment)
                setSideIcon(ContextCompat.getDrawable(this, R.drawable.ic_options_menu), this@AddProductFragment)
            }
        }
        hideBottomNavigationView(true)
        addVariantsTextView = mContentView?.findViewById(R.id.addVariantsTextView)
        addItemTextView = mContentView?.findViewById(R.id.addItemTextView)
        discountContainer = mContentView?.findViewById(R.id.discountContainer)
        noImagesLayout = mContentView?.findViewById(R.id.noImagesLayout)
        imagesRecyclerView = mContentView?.findViewById(R.id.imagesRecyclerView)
        chipGroupRecyclerView = mContentView?.findViewById(R.id.chipGroupRecyclerView)
        addDiscountLabel = mContentView?.findViewById(R.id.addDiscountLabel)
        productDescriptionInputLayout = mContentView?.findViewById(R.id.productDescriptionInputLayout)
        continueTextView = mContentView?.findViewById(R.id.continueTextView)
        enterCategoryEditText = mContentView?.findViewById(R.id.enterCategoryEditText)
        shareProductContainer = mContentView?.findViewById(R.id.shareProductContainer)
        discountPriceEditText = mContentView?.findViewById(R.id.discountedPriceEditText)
        productDescriptionEditText = mContentView?.findViewById(R.id.productDescriptionEditText)
        priceEditText = mContentView?.findViewById(R.id.priceEditText)
        nameEditText = mContentView?.findViewById(R.id.nameEditText)
        originalPriceTextView = mContentView?.findViewById(R.id.originalPriceTextView)
        discountedPriceTextView = mContentView?.findViewById(R.id.discountedPriceTextView)
        pricePercentageOffTextView = mContentView?.findViewById(R.id.pricePercentageOffTextView)
        variantRecyclerView = mContentView?.findViewById(R.id.variantRecyclerView)
        imagesLeftTextView = mContentView?.findViewById(R.id.imagesLeftTextView)
        discountPriceEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val str = s?.toString()
                if (isNotEmpty(str)) {
                    val priceStr = priceEditText?.text.toString()
                    when {
                        isEmpty(priceStr) -> {
                            if (str != "0.0") {
                                priceEditText?.apply {
                                    error = mAddProductStaticData?.error_mandatory_field
                                    requestFocus()
                                }
                                discountPriceEditText?.text = null
                            }
                            return
                        }
                        "." == str -> {}
                        !isDouble(str) -> {
                            discountPriceEditText?.apply {
                                text = null
                                error = mAddProductStaticData?.error_mandatory_field
                                requestFocus()
                            }
                        }
                        priceStr.toDouble() < (str?.toDouble() ?: 0.0) -> {
                            discountPriceEditText?.apply {
                                error = mAddProductStaticData?.error_discount_price_less_then_original_price
                                text = null
                                requestFocus()
                            }
                        }
                        0.0 == str?.toDoubleOrNull() -> {
                            originalPriceTextView?.text = null
                            discountedPriceTextView?.apply {
                                visibility = View.INVISIBLE
                                text = null
                            }
                            pricePercentageOffTextView?.text = null
                        }
                        else -> {
                            mIsOrderEdited = true
                            discountedPriceTextView?.apply {
                                visibility = View.VISIBLE
                                val discountStr = "${mAddProductStaticData?.text_rupees_symbol} ${str?.toDouble()}"
                                text = discountStr
                            }
                            originalPriceTextView?.apply {
                                val originalPriceStr = "${mAddProductStaticData?.text_rupees_symbol} ${priceStr.toDouble()}"
                                text = originalPriceStr
                                showStrikeOffText()
                            }
                            val priceDouble = priceStr.toDouble()
                            val discountedPriceDouble = str?.toDouble() ?: 0.0
                            val percentage = ((priceDouble - discountedPriceDouble) / priceDouble) * 100
                            val percentageOffStr = "${percentage.toInt()}% OFF"
                            pricePercentageOffTextView?.text = percentageOffStr
                        }
                    }
                } else {
                    originalPriceTextView?.text = null
                    pricePercentageOffTextView?.text = null
                    discountedPriceTextView?.visibility = View.INVISIBLE
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = showAddProductContainer()

        })
        if (isEmpty(mImagesStrList)) {
            mImagesStrList.add(0, AddProductImagesResponse(0,"", 0, Constants.MEDIA_TYPE_IMAGES))
        } else {
            mImagesStrList[0] = AddProductImagesResponse(0,"", 0, Constants.MEDIA_TYPE_IMAGES)
        }
        if (null == mAddProductResponse) {
            showProgressDialog(mActivity)
            mService?.getItemInfo(mItemId)
        } else {
            mAddProductStaticData = mAddProductResponse?.addProductStaticText
            setStaticDataFromResponse()
            setupImagesRecyclerView()
            setupCategoryChipRecyclerView()
            setupOptionsMenu()
            if (isNotEmpty(mAddProductResponse?.storeItem?.description)) {
                addItemTextView?.visibility = View.GONE
                productDescriptionInputLayout?.visibility = View.VISIBLE
                productDescriptionEditText?.setText(mAddProductResponse?.storeItem?.description)
            }
            handleVisibilityTextWatcher()
            val price = mAddProductResponse?.storeItem?.price ?: 0.0
            val discountedPrice = mAddProductResponse?.storeItem?.discountedPrice ?: 0.0
            if (0.0 == discountedPrice || discountedPrice >= price) {
                discountPriceEditText?.text = null
            } else {
                discountContainer?.visibility = View.VISIBLE
            }
            setupYoutubeUI()
            setupManageInventoryUI()
        }
        return mContentView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            if (View.GONE == productDescriptionInputLayout?.visibility) {
                if (true == mProductDescriptionPriceStr?.isNotEmpty()) {
                    addItemTextView?.visibility = View.GONE
                    productDescriptionInputLayout?.visibility = View.VISIBLE
                }
            }
            if (View.GONE == discountContainer?.visibility) {
                val price = if (true == mProductPriceStr?.isEmpty()) 0.0 else mProductPriceStr?.toDouble() ?: 0.0
                val discountedPrice = if (true == mProductDiscountedPriceStr?.isEmpty()) 0.0 else mProductDiscountedPriceStr?.toDouble() ?: 0.0
                if (0.0 == discountedPrice || discountedPrice >= price) {
                    discountPriceEditText?.text = null
                } else {
                    discountContainer?.visibility = View.VISIBLE
                }
            }
            mActiveVariantList = ArrayList()
            mDeletedVariantList = ArrayList()
        } catch (e: Exception) {
            Log.e(TAG, "onViewCreated: ${e.message}", e)
        }
    }

    private fun handleVisibilityTextWatcher() {
        priceEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val str = s?.toString()
                if (isNotEmpty(str?.trim())) {
                    mIsOrderEdited = true
                    if (View.VISIBLE != discountContainer?.visibility) addDiscountLabel?.visibility = View.VISIBLE
                    showAddProductContainer()
                }
                mProductPriceStr = str
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })
        nameEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mProductNameStr = s?.toString()
                val str = s?.toString()
                if (isNotEmpty(str?.trim())) {
                    mIsOrderEdited = true
                    showAddProductContainer()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })
        productDescriptionEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val str = s?.toString()
                if (isNotEmpty(str?.trim())) {
                    mIsOrderEdited = true
                    showAddProductContainer()
                    mProductDescriptionPriceStr = str
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })
        enterCategoryEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mIsOrderEdited = true
                showAddProductContainer()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })
        youtubeLinkEditText?.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mIsOrderEdited = true
                showAddProductContainer()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

        })
    }

    override fun onClick(view: View?) {
        when(view?.id) {
            addItemTextView?.id -> {
                addItemTextView?.visibility = View.GONE
                productDescriptionInputLayout?.visibility = View.VISIBLE
                productDescriptionEditText?.requestFocus()
                productDescriptionEditText?.isFocusable = true
            }
            addDiscountLabel?.id -> {
                addDiscountLabel?.visibility = View.GONE
                discountContainer?.visibility = View.VISIBLE
                discountPriceEditText?.addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {
                        val str = s?.toString()
                        mProductDiscountedPriceStr = str
                    }

                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit

                })
            }
            tryNowTextView?.id -> {
                if (null == mAddProductBannerStaticDataResponse) {
                    if (!isInternetConnectionAvailable(mActivity)) {
                        showNoInternetConnectionDialog()
                        return
                    }
                    showProgressDialog(mActivity)
                    mService?.getAddOrderBottomSheetData()
                } else showMasterCatalogBottomSheet(mAddProductBannerStaticDataResponse, mAddProductStaticData, Constants.MODE_ADD_PRODUCT)
            }
            continueTextView?.id -> {
                if (checkValidation()) {
                    val nameStr = nameEditText?.text.toString().trim()
                    val priceStr = priceEditText?.text?.trim().toString()
                    var discountedStr = discountPriceEditText?.text?.trim().toString()
                    val descriptionStr = productDescriptionEditText?.text.toString().trim()
                    val categoryStr = enterCategoryEditText?.text.toString()
                    if (!isInternetConnectionAvailable(mActivity)) return else {
                        val imageListRequest: ArrayList<AddProductImagesResponse> = ArrayList()
                        mImagesStrList.forEachIndexed { _, imageItem ->
                            if (isNotEmpty(imageItem.imageUrl)) imageListRequest.add(AddProductImagesResponse(imageItem.imageId, imageItem.imageUrl, 1, imageItem.mediaType))
                        }
                        if (true != mAddProductResponse?.deletedVariants?.isEmpty() && true == mAddProductResponse?.isVariantSaved) {
                            mAddProductResponse?.deletedVariants?.values?.let {
                                val deletedVariantList = ArrayList<VariantItemResponse>(it)
                                deletedVariantList.forEachIndexed { _, itemResponse -> itemResponse.status = 0 }
                                mAddProductResponse?.storeItem?.variantsList?.addAll(deletedVariantList)
                            }
                        }
                        var price: Double? = 0.0
                        try {
                            price = if (isNotEmpty(priceStr)) priceStr.toDoubleOrNull() else 0.0
                        } catch (e: Exception) {
                            Log.e(TAG, "AddProductFragment onClick request: ", e)
                        }
                        val discountPrice = if (discountedStr.isNotEmpty()) {
                            if (discountedStr.startsWith(".")) {
                                discountedStr = "0$discountedStr"
                            }
                            discountedStr.toDouble()
                        } else 0.0
                        val finalList: ArrayList<VariantItemResponse> = ArrayList()
                        var isErrorInVariantList = false
                        var isVariantNameSameAsItemName = false
                        var isVariantNameCommon = false
                        val uniqueVariantMap: HashMap<String, Int> = HashMap()
                        mActiveVariantList?.forEachIndexed { _, itemResponse ->
                            uniqueVariantMap[itemResponse.variantName?.toLowerCase(Locale.getDefault())?.trim() ?: ""] = 1
                        }
                        mActiveVariantList?.forEachIndexed { _, itemResponse ->
                            if (isEmpty(itemResponse.variantName?.trim())) {
                                itemResponse.isVariantNameEmptyError = true
                                isErrorInVariantList = true
                                return@forEachIndexed
                            } else {
                                if (uniqueVariantMap.containsKey(itemResponse.variantName?.toLowerCase(Locale.getDefault())?.trim())) {
                                    if (uniqueVariantMap[itemResponse.variantName?.toLowerCase(Locale.getDefault())?.trim()] == 1) {
                                        uniqueVariantMap[itemResponse.variantName?.toLowerCase(Locale.getDefault())?.trim() ?: ""] = 2
                                        isVariantNameCommon = false
                                    } else isVariantNameCommon = true
                                }
                                if (isVariantNameCommon) return@forEachIndexed
                                isVariantNameSameAsItemName = (itemResponse.variantName ?: "").equals(nameStr, true)
                                itemResponse.isVariantNameEmptyError = false
                                if (isVariantNameSameAsItemName) return@forEachIndexed
                            }
                        }
                        if (isVariantNameCommon) {
                            showShortSnackBar(mAddProductStaticData?.error_variant_already_exist, true, R.drawable.ic_close_red_small)
                            return
                        }
                        if (isVariantNameSameAsItemName) {
                            showShortSnackBar(mAddProductStaticData?.error_variant_name_same_with_item_name, true, R.drawable.ic_close_red_small)
                            return
                        }
                        if (isErrorInVariantList) {
                            mActiveVariantAdapter?.notifyItemRangeChanged(0, mActiveVariantAdapter?.getDataSourceList()?.size ?: 0)
                            return
                        }
                        mActiveVariantList?.let { list -> finalList.addAll(list) }
                        mDeletedVariantList?.let { list -> finalList.addAll(list) }
                        if (isNotEmpty(youtubeLinkEditText?.text?.toString())) {
                            val youtubeLink = youtubeLinkEditText?.text?.toString()?.replace(" ", "")?.trim()
                            if (null == mYoutubeItemResponse)
                                mYoutubeItemResponse = AddProductImagesResponse(imageId = 0, imageUrl = youtubeLink ?: "", status = 1, mediaType = Constants.MEDIA_TYPE_VIDEOS)
                            else
                                mYoutubeItemResponse?.imageUrl = youtubeLink ?: ""
                            mYoutubeItemResponse?.let { youtube -> imageListRequest.add(youtube) }
                        }
                        finalList.forEachIndexed { _, itemResponse -> if (Constants.INVENTORY_DISABLE == itemResponse.managedInventory) itemResponse.availableQuantity = 0 }
                        val request = AddProductRequest(
                            id = mItemId,
                            available = 1,
                            price = if (isNotEmpty(mActiveVariantList)) mActiveVariantList?.get(0)?.price else price,
                            discountedPrice = if (isNotEmpty(mActiveVariantList)) mActiveVariantList?.get(0)?.discountedPrice ?: 0.0 else discountPrice,
                            description = descriptionStr.trim(),
                            itemCategory = if (isEmpty(categoryStr.trim())) AddProductItemCategory(0, "") else AddProductItemCategory(mItemCategoryId, categoryStr),
                            imageList = imageListRequest,
                            name = nameStr.trim(),
                            variantsList = finalList,
                            managedInventory = if (true == manageInventorySwitch?.isChecked) Constants.INVENTORY_ENABLE else Constants.INVENTORY_DISABLE,
                            inventoryCount = if ((true == manageInventorySwitch?.isChecked) && !mIsVariantAvailable) mInventoryAdapter?.getDataSource()?.get(0)?.inventoryCount ?: 0 else 0,
                            lowQuantity = if (mIsAddNewProduct || mIsManageInventoryToggleChanged) 5 else mAddProductResponse?.storeItem?.lowQuantity ?: 5,
                            tags = mAddProductResponse?.storeItem?.productTagsArray
                        )
                        showProgressDialog(mActivity)
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_SAVE_ITEM,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                AFInAppEventParameterName.IMAGES            to Gson().toJson(mImagesStrList),
                                AFInAppEventParameterName.NAME              to mProductNameStr,
                                AFInAppEventParameterName.AVAILABLE         to "1",
                                AFInAppEventParameterName.DESCRIPTION       to descriptionStr,
                                AFInAppEventParameterName.CATEGORY          to categoryStr,
                                AFInAppEventParameterName.DISCOUNTED_PRICE  to discountedStr,
                                AFInAppEventParameterName.PRICE             to priceStr,
                                AFInAppEventParameterName.VARIANTS          to Gson().toJson(finalList)
                            )
                        )
                        mService?.setItem(request)
                    }
                }
            }
            updateCameraImageView?.id -> showAddProductImagePickerBottomSheet(0)
            updateCameraTextView?.id -> showAddProductImagePickerBottomSheet(0)
            learnYouTubeLinkHelpTextView?.id -> showLearnYouTubeBottomSheet()
            manageInventoryContainer?.id -> {
                when (View.GONE) {
                    manageInventoryBottomContainer?.visibility -> {
                        manageInventoryArrowImageView?.animate()?.rotation(0f)?.setDuration(Constants.TIMER_ARROW_ANIMATION)?.start()
                        manageInventoryBottomContainer?.visibility = View.VISIBLE
                    }
                    else -> {
                        manageInventoryArrowImageView?.animate()?.rotation(90f)?.setDuration(Constants.TIMER_ARROW_ANIMATION)?.start()
                        manageInventoryBottomContainer?.visibility = View.GONE
                    }
                }
            }
            addVariantsTextView?.id -> {
                mIsVariantAvailable = true
                if (null != mInventoryAdapter && isNotEmpty(mInventoryAdapter?.getDataSource())) {
                    val item = InventoryEnableDTO(
                        isVariantEnabled = true,
                        isCheckBoxEnabled = true == manageInventorySwitch?.isChecked,
                        isCheckboxSelected = true == manageInventorySwitch?.isChecked,
                        isEditTextEnabled = true == manageInventorySwitch?.isChecked,
                        inventoryCount = 0,
                        inventoryName = mAddProductStaticData?.hint_available_quantity
                    )
                    if (isEmpty(mActiveVariantList)) mInventoryAdapter?.getDataSource()?.clear()
                    mInventoryAdapter?.addInventoryToDataSource(item)
                }
                if (isEmpty(mActiveVariantList)) {
                    addNewVariantInList()
                    setupVariantRecyclerView()
                } else {
                    addNewVariantInList()
                    mActiveVariantAdapter?.notifyItemRangeChanged(0, mActiveVariantAdapter?.getDataSourceList()?.size ?: 0)
                }
                showAddProductContainer()
            }
        }
    }

    private fun showLearnYouTubeBottomSheet() {
        try {
            mActivity?.let {
                val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(it).inflate(
                    R.layout.bottom_sheet_learn_youtube,
                    it.findViewById(R.id.bottomSheetContainer)
                )
                bottomSheetDialog.apply {
                    setContentView(view)
                    view.run {
                        mAddProductResponse?.youtubeInfo?.let { youtube ->
                            val headingTextView: TextView = findViewById(R.id.headingTextView)
                            val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
                            headingTextView.text = youtube.heading
                            recyclerView.apply {
                                layoutManager = LinearLayoutManager(mActivity)
                                adapter = LearnYoutubeAdapter(youtube.steps)
                            }
                        }
                    }
                }.show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "showMoreOptionsBottomSheet: ${e.message}", e)
        }
    }

    private fun addNewVariantInList() {
        if (mActivity?.resources?.getInteger(R.integer.variant_count) ?: 0 > mActiveVariantList?.size ?: 0) {
            var priceStr = priceEditText?.text?.toString()?.trim()
            priceStr = if (isEmpty(priceStr)) "0.0" else {
                if (true == priceStr?.startsWith(".")) {
                    priceStr = "0$priceStr"
                }
                priceStr
            }
            var discountPriceStr = discountPriceEditText?.text?.toString()?.trim()
            discountPriceStr = if (isEmpty(discountPriceStr)) "0.0" else {
                if (true == discountPriceStr?.startsWith(".")) {
                    discountPriceStr = "0$discountPriceStr"
                }
                discountPriceStr
            }
            mActiveVariantList?.add(
                VariantItemResponse(
                    variantId = 0,
                    variantName = "",
                    price = priceStr?.toDouble() ?: 0.0,
                    discountedPrice = discountPriceStr?.toDouble() ?: 0.0,
                    status = 1,
                    masterId = 0,
                    available = 1,
                    variantImagesList = null,
                    managedInventory = if (true == manageInventorySwitch?.isChecked) Constants.INVENTORY_ENABLE else Constants.INVENTORY_DISABLE,
                    availableQuantity = 0,
                    isVariantNameEmptyError = false
                )
            )
        } else showToast("Only ${mActivity?.resources?.getInteger(R.integer.variant_count)} Variants are allowed.")
    }

    private fun setupVariantRecyclerView() {
        variantHeadingTextView?.apply {
            visibility = View.VISIBLE
            text = mAddProductStaticData?.text_variants
        }
        priceCardView?.visibility = View.GONE
        addVariantsTextView?.text = mAddProductStaticData?.text_add_variant
        mActiveVariantAdapter = ActiveVariantAdapterV2(mActivity, mAddProductStaticData, mAddProductResponse?.recentVariantsList, mActiveVariantList, object : IVariantItemClickListener {

            override fun onVariantDeleteClicked(position: Int) = showDeleteVariantConfirmationDialog(position)

            override fun onVariantImageClicked(position: Int) {
                showAddProductContainer()
                showAddProductImagePickerBottomSheet(position, true)
            }

            override fun onVariantListEmpty() {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    variantHeadingTextView?.visibility = View.GONE
                    priceCardView?.visibility = View.VISIBLE
                    addVariantsTextView?.text = mActivity?.getString(R.string.add_variants_optional)
                    if (isEmpty(mActiveVariantList)) {
                        val item = InventoryEnableDTO(
                            isVariantEnabled = false,
                            isCheckBoxEnabled = Constants.INVENTORY_ENABLE == mAddProductResponse?.storeItem?.managedInventory,
                            isEditTextEnabled = Constants.INVENTORY_ENABLE == mAddProductResponse?.storeItem?.managedInventory,
                            isCheckboxSelected = (true == manageInventorySwitch?.isChecked),
                            inventoryCount = 0,
                            inventoryName = mAddProductStaticData?.hint_available_quantity
                        )
                        mInventoryAdapter?.let { adapter ->
                            adapter.getDataSource().clear()
                            adapter.addInventoryToDataSource(item)
                        }
                    }
                }
            }

            override fun onVariantItemChanged() = showAddProductContainer()

            override fun onVariantNameChangedListener(variantUpdatedName: String?, variantPosition: Int) {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    showAddProductContainer()
                    if (null != mInventoryAdapter && isNotEmpty(mInventoryAdapter?.getDataSource())) {
                        mInventoryAdapter?.getDataSource()?.get(variantPosition)?.inventoryName = variantUpdatedName
                        mInventoryAdapter?.notifyItemChanged(variantPosition)
                    }
                }
            }

            override fun onVariantInventoryIconClicked(position: Int) {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    val nestedScrollView: NestedScrollView? = mContentView?.findViewById(R.id.addProductNestedScrollView)
                    val manageInventoryOuterContainer: View? = mContentView?.findViewById(R.id.manageInventoryOuterContainer)
                    nestedScrollView?.smoothScrollTo(0, manageInventoryOuterContainer?.y?.toInt() ?: 0, Constants.TIMER_MANAGE_INVENTORY_SCROLL.toInt())
                }
            }

        })
        variantRecyclerView?.apply {
            itemAnimator = DefaultItemAnimator()
            layoutManager = LinearLayoutManager(mActivity)
            visibility = View.VISIBLE
            adapter = mActiveVariantAdapter
        }
    }

    private fun showDeleteVariantConfirmationDialog(position: Int) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.currentFocus?.clearFocus()
        }
        mActivity?.let {
            val dialog = Dialog(it)
            dialog.apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(true)
                setContentView(R.layout.dialog_delete_variant_confirmation)
                val deleteVariantMessageTextView: TextView = dialog.findViewById(R.id.deleteVariantMessageTextView)
                val deleteVariantTextView: TextView = dialog.findViewById(R.id.deleteVariantTextView)
                val deleteVariantCancelTextView: TextView = dialog.findViewById(R.id.deleteVariantCancelTextView)
                deleteVariantMessageTextView.text = mAddProductStaticData?.dialog_delete_variant_message
                deleteVariantTextView.text = mAddProductStaticData?.text_delete
                deleteVariantCancelTextView.text = mAddProductStaticData?.text_cancel
                deleteVariantTextView.setOnClickListener {
                    dialog.dismiss()
                    if (position >= (mActiveVariantList?.size ?: 0)) return@setOnClickListener
                    val variantItem = mActiveVariantList?.get(position)
                    variantItem?.let { item ->
                        item.status = 0
                        mDeletedVariantList?.add(item)
                    }
                    mActiveVariantAdapter?.deleteItemFromActiveVariantList(position)
                    mInventoryAdapter?.let { adapter ->
                        if (isNotEmpty(adapter.getDataSource())) {
                            adapter.getDataSource().removeAt(position)
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
                deleteVariantCancelTextView.setOnClickListener { dialog.dismiss() }
            }.show()
        }
    }

    private fun showAddProductImagePickerBottomSheet(position: Int, isVariantImageClicked: Boolean = false) {
        sIsVariantImageClicked = isVariantImageClicked
        if (sIsVariantImageClicked) sVariantImageClickedPosition = position
        mActivity?.let { it ->
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), Constants.IMAGE_PICK_REQUEST_CODE)
                return
            }
        }
        showAddProductContainer()
        mIsOrderEdited = true
        mActivity?.let {
            imagePickBottomSheet = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_image_pick_v2, it.findViewById(R.id.bottomSheetContainer))
            imagePickBottomSheet?.apply {
                setContentView(view)
                view?.run {
                    val bottomSheetUploadImageCloseImageView: ImageView = findViewById(R.id.bottomSheetUploadImageCloseImageView)
                    val bottomSheetUploadImageHeading: TextView = findViewById(R.id.bottomSheetUploadImageHeading)
                    val bottomSheetUploadImageCameraTextView: TextView = findViewById(R.id.bottomSheetUploadImageCameraTextView)
                    val bottomSheetUploadImageGalleryTextView: TextView = findViewById(R.id.bottomSheetUploadImageGalleryTextView)
                    val bottomSheetUploadImageSearchHeading: TextView = findViewById(R.id.bottomSheetUploadImageSearchHeading)
                    val bottomSheetUploadImageRemovePhotoTextView: TextView = findViewById(R.id.bottomSheetUploadImageRemovePhotoTextView)
                    val searchImageEditText: EditText = findViewById(R.id.searchImageEditText)
                    val searchImageImageView: View = findViewById(R.id.searchImageImageView)
                    val searchImageRecyclerView: RecyclerView = findViewById(R.id.searchImageRecyclerView)
                    bottomSheetUploadImageGalleryTextView.text = mAddProductStaticData?.bottom_sheet_add_from_gallery
                    bottomSheetUploadImageSearchHeading.text = mAddProductStaticData?.bottom_sheet_you_can_add_upto_4_images
                    bottomSheetUploadImageRemovePhotoTextView.text = mAddProductStaticData?.bottom_sheet_remove_image
                    bottomSheetUploadImageHeading.text = mAddProductStaticData?.bottom_sheet_add_image
                    bottomSheetUploadImageCameraTextView.text = mAddProductStaticData?.bottom_sheet_take_a_photo
                    searchImageEditText.hint = mAddProductStaticData?.bottom_sheet_hint_search_for_images_here
                    mProductNameStr?.let { str ->
                        if (isVariantImageClicked) {
                            val name = str + " " + mActiveVariantList?.get(position)?.variantName
                            searchImageEditText.setText(name)
                        } else searchImageEditText.setText(str)
                    }
                    bottomSheetUploadImageCloseImageView.setOnClickListener { if (true == imagePickBottomSheet?.isShowing) imagePickBottomSheet?.dismiss() }
                    if (!isVariantImageClicked) {
                        bottomSheetUploadImageRemovePhotoTextView.visibility = if (0 == position) View.GONE else View.VISIBLE
                    } else {
                        val variantImageEmpty = isEmpty(mActiveVariantList?.get(position)?.variantImagesList)
                        bottomSheetUploadImageRemovePhotoTextView.visibility = if (variantImageEmpty) View.GONE else View.VISIBLE
                    }
                    bottomSheetUploadImageCameraTextView.setOnClickListener {
                        imagePickBottomSheet?.dismiss()
                        openCameraWithCrop()
                    }
                    bottomSheetUploadImageGalleryTextView.setOnClickListener {
                        imagePickBottomSheet?.dismiss()
                        openMobileGalleryWithCrop()
                    }
                    bottomSheetUploadImageRemovePhotoTextView.setOnClickListener {
                        imagePickBottomSheet?.dismiss()
                        if (isVariantImageClicked) {
                            val item = mActiveVariantList?.get(position)?.variantImagesList?.get(0)
                            item?.let { deleteItem ->
                                deleteItem.status = 0
                                mActiveVariantList?.get(position)?.variantImagesList?.set(0, deleteItem)
                            }
                            mActiveVariantAdapter?.notifyItemChanged(position)
                        } else {
                            mImagesStrList.removeAt(mImageChangePosition)
                            mImageAddAdapter?.setListToAdapter(mImagesStrList)
                        }
                    }
                    mImageSearchAdapter.setSearchImageListener(this@AddProductFragment)
                    mImageSearchAdapter.setContext(mActivity)
                    searchImageImageView.setOnClickListener {
                        if (isEmpty(searchImageEditText.text.trim().toString())) {
                            searchImageEditText.error = getString(R.string.mandatory_field_message)
                            searchImageEditText.requestFocus()
                            return@setOnClickListener
                        }
                        showProgressDialog(mActivity)
                        CoroutineScopeUtils().runTaskOnCoroutineBackground {
                            try {
                                val response = RetrofitApi().getServerCallObject()?.searchImagesFromBing(searchImageEditText.text.trim().toString(), getStringDataFromSharedPref(Constants.STORE_ID))
                                response?.let { it ->
                                    if (it.isSuccessful) {
                                        it.body()?.let {
                                            withContext(Dispatchers.Main) {
                                                stopProgress()
                                                val list = it.mImagesList
                                                searchImageRecyclerView?.apply {
                                                    layoutManager = GridLayoutManager(mActivity, 3)
                                                    adapter = mImageSearchAdapter
                                                    list?.let { arrayList -> mImageSearchAdapter.setSearchImageList(arrayList) }
                                                }
                                            }
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                exceptionHandlingForAPIResponse(e)
                            }
                        }
                    }
                    if (isNotEmpty(searchImageEditText.text.toString().trim())) searchImageImageView.callOnClick()
                }
            }?.show()
        }
    }

    override fun onResume() {
        super.onResume()
        stopProgress()
    }

    override fun onImageSelectionResultFile(file: File?, mode: String) {
        try {
            Log.d(TAG, "onImageSelectionResultFile: onImageSelectionResultFile :: mode $mode")
            if (Constants.MODE_CROP == mode) {
                imagePickBottomSheet?.dismiss()
                showImageCropDialog(file)
                return
            }
            if (!isInternetConnectionAvailable(mActivity)) {
                showNoInternetConnectionDialog()
                return
            }
            if (null == file) {
                showShortSnackBar(getString(R.string.something_went_wrong), true, R.drawable.ic_close_red)
                return
            }
            val fileRequestBody = MultipartBody.Part.createFormData("media", file.name, RequestBody.create("image/*".toMediaTypeOrNull(), file))
            val imageTypeRequestBody = RequestBody.create("text/plain".toMediaTypeOrNull(), Constants.BASE64_STORE_ITEMS)
            showCancellableProgressDialog(mActivity)
            mService?.generateCDNLink(imageTypeRequestBody, fileRequestBody)
            imagePickBottomSheet?.dismiss()
        } catch (e: Exception) {
            Log.e(TAG, "onImageSelectionResultFile: ${e.message}", e)
        }
    }

    private fun showImageCropDialog(file: File?) {
        mActivity?.run {
            val imageCropDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            imageCropDialog.apply {
                val view = LayoutInflater.from(mActivity).inflate(R.layout.layout_crop_photo, null)
                setContentView(view)
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                val cropImageView: CropImageView = view.findViewById(R.id.cropImageView)
                val doneImageView: View = view.findViewById(R.id.doneImageView)
                cropImageView.setAspectRatio(1, 1)
                cropImageView.setFixedAspectRatio(true)
                cropImageView.isAutoZoomEnabled = true
                cropImageView.setMaxCropResultSize(2040, 2040)
                cropImageView.setImageUriAsync(file?.toUri())
                doneImageView.setOnClickListener {
                    val croppedImage = cropImageView.croppedImage
                    val croppedImageFile = getImageFileFromBitmap(croppedImage, this@run)
                    CoroutineScopeUtils().runTaskOnCoroutineMain {
                        showAddProductContainer()
                        onImageSelectionResultFile(croppedImageFile, "")
                    }
                    imageCropDialog.dismiss()
                }
            }.show()
        }
    }

    private fun checkValidation(): Boolean {
        try {
            return when {
                isEmpty(nameEditText?.text?.toString()?.trim()) -> {
                    nameEditText?.apply {
                        error = mAddProductStaticData?.error_mandatory_field
                        requestFocus()
                    }
                    false
                }
                "." == priceEditText?.text?.toString() -> {
                    priceEditText?.apply {
                        error = mAddProductStaticData?.error_mandatory_field
                        requestFocus()
                    }
                    false
                }
                true == priceEditText?.text?.startsWith(".") -> {
                    var priceStr = priceEditText?.text?.toString()
                    priceStr = "0$priceStr"
                    priceEditText?.setText(priceStr)
                    return true
                }
                isNotEmpty(discountPriceEditText?.text?.toString()) && "." == discountPriceEditText?.text?.toString() -> {
                    discountPriceEditText?.apply {
                        text = null
                        error = mAddProductStaticData?.error_mandatory_field
                        requestFocus()
                    }
                    false
                }
                isNotEmpty(discountPriceEditText?.text?.toString()) && (priceEditText?.text.toString().toDouble() < discountPriceEditText?.text.toString().toDouble()) -> {
                    discountPriceEditText?.apply {
                        text = null
                        error = mAddProductStaticData?.error_discount_price_less_then_original_price
                        requestFocus()
                    }
                    false
                }
                isNotEmpty(youtubeLinkEditText?.text?.toString()) && !isYoutubeUrlValid(youtubeLinkEditText?.text?.toString() ?: "") -> {
                    youtubeLinkEditText?.apply {
                        error = mActivity?.getString(R.string.please_enter_valid_url)
                        requestFocus()
                    }
                    false
                }
                ((!mIsVariantAvailable) && (true == manageInventorySwitch?.isChecked) && (null == mInventoryAdapter?.getDataSource()?.get(0)?.inventoryCount || 0 == mInventoryAdapter?.getDataSource()?.get(0)?.inventoryCount)) -> {
                    showEmptyInventoryDialog()
                    false
                }
                (mIsVariantAvailable && (true == manageInventorySwitch?.isChecked)) -> {
                    var isAllCheckboxChecked = true
                    mInventoryAdapter?.getDataSource()?.forEachIndexed { _, inventory ->
                        if (inventory.isCheckboxSelected && (0 == inventory.inventoryCount)) {
                            isAllCheckboxChecked = false
                            return@forEachIndexed
                        }
                    }
                    if (!isAllCheckboxChecked) {
                        showEmptyInventoryDialog()
                        return false
                    }
                    true
                }
                else -> true
            }
        } catch (e: Exception) {
            Log.e(TAG, "checkValidation: ${e.message}", e)
            return false
        }
    }

    override fun onAddProductBannerStaticDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mAddProductBannerStaticDataResponse = Gson().fromJson<AddProductBannerTextResponse>(commonResponse.mCommonDataStr, AddProductBannerTextResponse::class.java)
            mAddProductBannerStaticDataResponse?.let { showMasterCatalogBottomSheet(it, mAddProductStaticData, Constants.MODE_ADD_PRODUCT) }
        }
    }

    override fun onGetAddProductDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                stopProgress()
                mAddProductResponse = Gson().fromJson<AddProductResponse>(commonResponse.mCommonDataStr, AddProductResponse::class.java)
                StaticInstances.sIsShareStoreLocked = mAddProductResponse?.isShareStoreLocked ?: false
                Log.d(TAG, "onGetAddProductDataResponse: $mAddProductResponse")
                mAddProductStaticData = mAddProductResponse?.addProductStaticText
                mAddProductResponse?.storeItem?.run {
                    nameEditText?.setText(name)
                    mProductNameStr = name
                    priceEditText?.setText(if (0.0 != price) price.toString() else null)
                    if (0.0 == discountedPrice || discountedPrice >= price) {
                        if (price != 0.0) {
                            addDiscountLabel?.visibility = View.VISIBLE
                        }
                        discountPriceEditText?.text = null
                    } else {
                        discountContainer?.visibility = View.VISIBLE
                        discountPriceEditText?.setText("$discountedPrice")
                    }
                    if (isNotEmpty(mAddProductResponse?.storeItem?.imagesList)) {
                        noImagesLayout?.visibility = View.GONE
                        imagesRecyclerView?.apply {
                            layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                            mImagesStrList.clear()
                            if (isEmpty(mImagesStrList)) mImagesStrList.add(AddProductImagesResponse(0, "", 0, Constants.MEDIA_TYPE_IMAGES))
                            mAddProductResponse?.storeItem?.imagesList?.forEachIndexed { _, imagesResponse ->
                                if (0 != imagesResponse.status && Constants.MEDIA_TYPE_IMAGES == imagesResponse.mediaType) mImagesStrList.add(AddProductImagesResponse(imagesResponse.imageId, imagesResponse.imageUrl, 1, imagesResponse.mediaType))
                            }
                            mImageAddAdapter = AddProductsImagesAdapter(this@AddProductFragment, mImagesStrList ,mAddProductStaticData?.text_upload_or_search_images, this@AddProductFragment)
                            adapter = mImageAddAdapter
                        }
                    } else {
                        imagesRecyclerView?.visibility = View.GONE
                        noImagesLayout?.visibility = View.VISIBLE
                    }
                    if (description.isNotEmpty()) {
                        addItemTextView?.visibility = View.GONE
                        productDescriptionInputLayout?.visibility = View.VISIBLE
                        productDescriptionEditText?.setText(description)
                    }
                }
                setupCategoryChipRecyclerView()
                setStaticDataFromResponse()
                setupOptionsMenu()
                setupYoutubeUI()
                if (isNotEmpty(mAddProductResponse?.storeItem?.variantsList)) {
                    mIsVariantAvailable = true
                    mActiveVariantList = ArrayList()
                    mActiveVariantList = mAddProductResponse?.storeItem?.variantsList
                    setupVariantRecyclerView()
                }
                setupManageInventoryUI()
                shareProductContainer?.setOnClickListener {
                    if (StaticInstances.sIsShareStoreLocked) {
                        getLockedStoreShareDataServerCall(Constants.MODE_SHARE_STORE)
                        return@setOnClickListener
                    }
                    val productNameStr = mAddProductResponse?.storeItem?.name?.trim()
                    val newProductName = replaceTemplateString(productNameStr)
                    val sharingData = "ItemName: ${mAddProductResponse?.storeItem?.name}\nPrice:  ₹${mAddProductResponse?.storeItem?.price} \nDiscounted Price: ₹${mAddProductResponse?.storeItem?.discountedPrice}\n\n\uD83D\uDED2 ORDER NOW, Click on the link below\n\n" + "${mAddProductResponse?.domain}/product/${mAddProductResponse?.storeItem?.id}/$newProductName"
                    if (isEmpty(mAddProductResponse?.storeItem?.imageUrl)) shareOnWhatsApp(sharingData, null) else shareDataOnWhatsAppWithImage(sharingData, mAddProductResponse?.storeItem?.imageUrl)
                }
                shareProductContainer?.visibility = if (mIsAddNewProduct) View.GONE else View.VISIBLE
                continueTextView?.visibility = if (mIsAddNewProduct) View.VISIBLE else View.GONE
                handleVisibilityTextWatcher()
            } catch (e: Exception) {
                Log.e(TAG, "onGetAddProductDataResponse: ${e.message}", e)
            }
        }
    }

    private fun setupYoutubeUI() {
        val youtubeLinkContainer: View? = mContentView?.findViewById(R.id.youtubeLinkContainer)
        if (null == mAddProductResponse?.youtubeInfo) {
            youtubeLinkContainer?.visibility = View.GONE
        } else {
            mAddProductResponse?.youtubeInfo?.let { youtube ->
                val learnYouTubeLinkTextView: TextView? = mContentView?.findViewById(R.id.learnYouTubeLinkHelpTextView)
                youtubeLinkContainer?.visibility = View.VISIBLE
                youtubeLinkInputLayout?.hint = youtube.hint1
                learnYouTubeLinkTextView?.text = youtube.message
                mAddProductResponse?.storeItem?.imagesList?.forEachIndexed { _, imagesItemResponse ->
                    if (Constants.MEDIA_TYPE_VIDEOS == imagesItemResponse.mediaType && 1 == imagesItemResponse.status) {
                        mYoutubeItemResponse = imagesItemResponse
                        return@forEachIndexed
                    }
                }
                if (null != mYoutubeItemResponse) youtubeLinkEditText?.setText(mYoutubeItemResponse?.imageUrl)
            }
        }
    }

    private fun setupManageInventoryUI() {
        if (!mIsManageInventoryAvailable) {
            manageInventoryOuterContainer?.visibility = View.GONE
            return
        }
        mAddProductStaticData?.let { staticText ->
            manageInventoryHeadingTextView?.text = staticText.heading_manage_inventory
            manageInventoryMessageTextView?.text = staticText.sub_heading_inventory
            val availableQuantity = if (0 == mAddProductResponse?.storeItem?.lowQuantity) mAddProductStaticData?.footer_text_low_stock_disabled_alert else mAddProductStaticData?.footer_text_low_stock_alert
            val availableQuantityStr = availableQuantity?.replace("xxx", "${mAddProductResponse?.storeItem?.lowQuantity}", true)
            manageInventoryFooterTextView?.text = availableQuantityStr
        }
        val inventoryList = ArrayList<InventoryEnableDTO>()
        if (isEmpty(mActiveVariantList)) {
            val item = InventoryEnableDTO(
                isVariantEnabled = false,
                isCheckBoxEnabled = Constants.INVENTORY_ENABLE == mAddProductResponse?.storeItem?.managedInventory,
                isEditTextEnabled = Constants.INVENTORY_ENABLE == mAddProductResponse?.storeItem?.managedInventory,
                isCheckboxSelected = (true == manageInventorySwitch?.isChecked),
                inventoryCount = mAddProductResponse?.storeItem?.availableQuantity,
                inventoryName = mAddProductStaticData?.hint_available_quantity
            )
            inventoryList.add(item)
        } else {
            mActiveVariantList?.forEachIndexed { _, variantItem ->
                val item = InventoryEnableDTO(
                    isVariantEnabled = true,
                    isCheckBoxEnabled = Constants.INVENTORY_ENABLE == mAddProductResponse?.storeItem?.managedInventory,
                    isCheckboxSelected = Constants.INVENTORY_ENABLE == variantItem.managedInventory,
                    isEditTextEnabled = Constants.INVENTORY_ENABLE == variantItem.managedInventory,
                    inventoryCount = if (Constants.INVENTORY_ENABLE == variantItem.managedInventory) variantItem.availableQuantity else 0,
                    inventoryName = variantItem.variantName
                )
                inventoryList.add(item)
            }
        }
        Log.d(TAG, "setupManageInventoryUI: inventoryList :: $inventoryList")
        if (null == mInventoryAdapter) {
            mInventoryAdapter = InventoryEnableAdapter(mAddProductStaticData?.error_text_inventory_limit, inventoryList, object : IProductInventoryListener {

                override fun onItemInventoryChangeListener(inventoryCount: String) {
                    CoroutineScopeUtils().runTaskOnCoroutineMain {
                        if (isEmpty(inventoryCount)) return@runTaskOnCoroutineMain
                        mIsOrderEdited = true
                        showAddProductContainer()
                    }
                }

                override fun onVariantInventoryChangeListener(inventoryCount: String, position: Int) {
                    CoroutineScopeUtils().runTaskOnCoroutineMain {
                        if (isEmpty(inventoryCount)) return@runTaskOnCoroutineMain
                        if (isEmpty(mActiveVariantList)) return@runTaskOnCoroutineMain
                        if (position >= (mActiveVariantList?.size ?: 0)) return@runTaskOnCoroutineMain
                        mActiveVariantList?.get(position)?.availableQuantity = inventoryCount.toInt()
                        mIsOrderEdited = true
                        showAddProductContainer()
                    }
                }

                override fun onVariantInventoryItemCheckChange(isCheck: Boolean, inventoryItem: InventoryEnableDTO, position: Int) {
                    CoroutineScopeUtils().runTaskOnCoroutineMain {
                        if (isEmpty(mActiveVariantList)) return@runTaskOnCoroutineMain
                        if (position < 0) return@runTaskOnCoroutineMain
                        inventoryItem.apply {
                            isCheckboxSelected = isCheck
                            isEditTextEnabled = isCheck
                            inventoryCount = 0
                        }
                        mActiveVariantList?.get(position)?.managedInventory = if (isCheck) Constants.INVENTORY_ENABLE else Constants.INVENTORY_DISABLE
                        mIsOrderEdited = true
                        showAddProductContainer()
                        mInventoryAdapter?.notifyItemChanged(position)
                        if (!isCheck) {
                            var isAllCheckBoxUnChecked = true
                            mActiveVariantList?.forEachIndexed { _, itemResponse ->
                                if (Constants.INVENTORY_ENABLE == itemResponse.managedInventory) {
                                    isAllCheckBoxUnChecked = false
                                    return@forEachIndexed
                                }
                            }

                            if (isAllCheckBoxUnChecked) {
                                val manageInventorySwitch: SwitchMaterial? = mContentView?.findViewById(R.id.manageInventorySwitch)
                                manageInventorySwitch?.isChecked = false
                            }
                        }
                        mActiveVariantAdapter?.notifyItemChanged(position)
                    }
                }

            })
            manageInventoryRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity)
                setRecyclerListener { hideSoftKeyboard() }
                adapter = mInventoryAdapter
            }
        } else mInventoryAdapter?.notifyItemRangeChanged(0, mInventoryAdapter?.getDataSource()?.size ?: 0)
        manageInventorySwitch?.isChecked = (Constants.INVENTORY_ENABLE == mAddProductResponse?.storeItem?.managedInventory)
        manageInventorySwitch?.setOnCheckedChangeListener { _, isChecked ->
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_INVENTORY_TOGGLE, isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    AFInAppEventParameterName.INVENTORY to "${if (isChecked) Constants.INVENTORY_ENABLE else Constants.INVENTORY_DISABLE}",
                    AFInAppEventParameterName.ITEM_ID to "${mAddProductResponse?.storeItem?.id}"
                )
            )
            showAddProductContainer()
            mIsManageInventoryToggleChanged = true
            mIsOrderEdited = true
            when {
                isChecked -> {
                    mInventoryAdapter?.getDataSource()?.forEachIndexed { _, item ->
                        item.isCheckboxSelected = true
                        item.isCheckBoxEnabled = true
                        item.isEditTextEnabled = true
                    }
                    if (mIsVariantAvailable) {
                        mActiveVariantList?.forEachIndexed { _, itemResponse ->
                            itemResponse.availableQuantity = 0
                            itemResponse.managedInventory = Constants.INVENTORY_ENABLE
                        }
                    }
                    val availableQuantity = if (0 == mAddProductResponse?.storeItem?.lowQuantity) mAddProductStaticData?.footer_text_low_stock_disabled_alert else mAddProductStaticData?.footer_text_low_stock_alert
                    val availableQuantityStr = availableQuantity?.replace("xxx", "5", true)
                    manageInventoryFooterTextView?.text = availableQuantityStr
                }
                else -> {
                    mInventoryAdapter?.getDataSource()?.forEachIndexed { _, item ->
                        item.isCheckboxSelected = false
                        item.isCheckBoxEnabled = false
                        item.isEditTextEnabled = false
                        item.inventoryCount = 0
                    }
                    if (mIsVariantAvailable) {
                        mActiveVariantList?.forEachIndexed { _, itemResponse ->
                            itemResponse.availableQuantity = 0
                            itemResponse.managedInventory = Constants.INVENTORY_DISABLE
                        }
                    }
                    val availableQuantity = if (0 == mAddProductResponse?.storeItem?.lowQuantity) mAddProductStaticData?.footer_text_low_stock_disabled_alert else mAddProductStaticData?.footer_text_low_stock_alert
                    val availableQuantityStr = availableQuantity?.replace("xxx", "${mAddProductResponse?.storeItem?.lowQuantity}", true)
                    manageInventoryFooterTextView?.text = availableQuantityStr
                }
            }
            mInventoryAdapter?.notifyDataSetChanged()
            mActiveVariantAdapter?.notifyDataSetChanged()
        }
    }

    private fun setupOptionsMenu() {
        mOptionsMenuResponse = mAddProductResponse?.addProductStoreOptionsMenu
        if (isEmpty(mOptionsMenuResponse)) ToolBarManager.getInstance()?.setSideIconVisibility(false)
    }

    private fun setupCategoryChipRecyclerView() {
        try {
            mAddProductResponse?.addProductStoreCategories?.let {
                chipGroupRecyclerView?.apply {
                    layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
                    mAddProductStoreCategoryList = it.storeCategoriesList
                    if (isNotEmpty(mAddProductStoreCategoryList)) {
                        mTempProductCategoryList.clear()
                        mAddProductStoreCategoryList?.forEachIndexed { _, categoryItem ->
                            if (isNotEmpty(categoryItem.name)) mTempProductCategoryList.add(categoryItem)
                        }
                        mTempProductCategoryList.forEachIndexed { _, categoryItem ->
                            if (categoryItem.id == mAddProductResponse?.storeItem?.category?.id) {
                                enterCategoryEditText?.setText(categoryItem.name)
                                mItemCategoryId = mAddProductResponse?.storeItem?.category?.id ?: 0
                                categoryItem.isSelected = true
                            } else categoryItem.isSelected = false
                        }
                        addProductChipsAdapter = AddProductsChipsAdapter(mTempProductCategoryList, this@AddProductFragment)
                        adapter = addProductChipsAdapter
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "setupCategoryChipRecyclerView: ${e.message}", e)
        }
    }

    private fun setStaticDataFromResponse() {
        try {
            mAddProductStaticData?.run {
                ToolBarManager.getInstance()?.headerTitle = heading_add_product_page
                val addItemTextView: TextView? = mContentView?.findViewById(R.id.addItemTextView)
                val tryNowTextView: TextView? = mContentView?.findViewById(R.id.tryNowTextView)
                val textView2: TextView? = mContentView?.findViewById(R.id.textView2)
                val updateCameraTextView: TextView? = mContentView?.findViewById(R.id.updateCameraTextView)
                val nameInputLayout: TextInputLayout? = mContentView?.findViewById(R.id.nameInputLayout)
                val priceInputLayout: TextInputLayout? = mContentView?.findViewById(R.id.priceInputLayout)
                val discountedPriceInputLayout: TextInputLayout? = mContentView?.findViewById(R.id.discountedPriceInputLayout)
                val enterCategoryInputLayout: TextInputLayout? = mContentView?.findViewById(R.id.enterCategoryInputLayout)
                tryNowTextView?.text = text_try_now
                addDiscountLabel?.text = text_add_discount_on_this_item
                textView2?.text = heading_add_product_banner
                updateCameraTextView?.text = text_upload_or_search_images
                nameInputLayout?.hint = hint_item_name
                priceInputLayout?.hint = hint_price
                discountedPriceInputLayout?.hint = hint_discounted_price
                enterCategoryInputLayout?.hint = hint_enter_category_optional
                addItemTextView?.text = text_add_item_description
                continueTextView?.text = if (mIsAddNewProduct) text_add_item else getString(R.string.save)
                val count = mImagesStrList.size
                val imagesLeftStr = "${if (0 == count) count else count - 1}/4 $text_images_added"
                imagesLeftTextView?.text = imagesLeftStr
            }
        } catch (e: Exception) {
            Log.e(TAG, "setStaticDataFromResponse: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "Exception Point" to "setStaticDataFromResponse",
                    "Exception Message" to e.message,
                    "Exception Logs" to e.toString()
                )
            )
        }
    }

    override fun onAddProductDataResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                stopProgress()
                if (commonResponse.mIsSuccessStatus) {
                    showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_check_circle)
                    fragmentManager?.popBackStack()
                } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
            } catch (e: Exception) {
                Log.e(TAG, "onAddProductDataResponse: ${e.message}", e)
            }
        }
    }

    override fun onConvertFileToLinkResponse(commonResponse: CommonApiResponse) {
        stopProgress()
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                showAddProductContainer()
                if (commonResponse.mIsSuccessStatus) {
                    showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_check_circle)
                    val base64Str = Gson().fromJson<String>(commonResponse.mCommonDataStr, String::class.java)
                    if (sIsVariantImageClicked) {
                        if (isEmpty(mActiveVariantList?.get(sVariantImageClickedPosition)?.variantImagesList))
                            mActiveVariantList?.get(sVariantImageClickedPosition)?.variantImagesList = ArrayList()
                        if (isNotEmpty(mActiveVariantList?.get(sVariantImageClickedPosition)?.variantImagesList)) {
                            val item = mActiveVariantList?.get(sVariantImageClickedPosition)?.variantImagesList?.get(0)
                            val imageItem = VariantItemImageResponse(
                                if (0 != item?.imageId) item?.imageId ?: 0 else 0,
                                item?.imageUrl ?: "",
                                0
                            )
                            val newImageItem = VariantItemImageResponse(0, base64Str, 1)
                            mActiveVariantList?.get(sVariantImageClickedPosition)?.variantImagesList?.set(0, newImageItem)
                            mActiveVariantList?.get(sVariantImageClickedPosition)?.variantImagesList?.add(imageItem)
                        } else {
                            val imageItem = VariantItemImageResponse(0, base64Str, 1)
                            mActiveVariantList?.get(sVariantImageClickedPosition)?.variantImagesList?.add(imageItem)
                        }
                        mActiveVariantAdapter?.notifyItemChanged(sVariantImageClickedPosition)
                    } else if (1 == mImagesStrList.size || 0 == mImageChangePosition) {
                        mImagesStrList.add(AddProductImagesResponse(0, base64Str, 1, Constants.MEDIA_TYPE_IMAGES))
                    } else if (0 != mImageChangePosition) {
                        val imageResponse = mImagesStrList[mImageChangePosition]
                        imageResponse.imageUrl = base64Str
                        mImagesStrList[mImageChangePosition] = imageResponse
                    }
                    setupImagesRecyclerView()
                } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
            } catch (e: Exception) {
                Log.e(TAG, "onConvertFileToLinkResponse: ${e.message}", e)
            }
        }
    }

    private fun setupImagesRecyclerView() {
        try {
            noImagesLayout?.visibility = View.GONE
            imagesRecyclerView?.visibility = View.VISIBLE
            imagesRecyclerView?.apply {
                layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                mImageAddAdapter = AddProductsImagesAdapter(
                    this@AddProductFragment,
                    mImagesStrList,
                    mAddProductStaticData?.text_upload_or_search_images,
                    this@AddProductFragment
                )
                adapter = mImageAddAdapter
                mImageAddAdapter?.setListToAdapter(mImagesStrList)
            }
            val imagesLeftStr = "${mImagesStrList?.size - 1}/4 ${mAddProductStaticData?.text_images_added}"
            imagesLeftTextView?.text = imagesLeftStr
        } catch (e: Exception) {
            Log.e(TAG, "setupImagesRecyclerView: ${e.message}", e)
        }
    }

    override fun onDeleteItemResponse(commonResponse: CommonApiResponse) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            mIsOrderDeleted = false
            if (commonResponse.mIsSuccessStatus) {
                mIsOrderDeleted = true
                showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_check_circle)
                mActivity?.onBackPressed()
            } else showShortSnackBar(commonResponse.mMessage, true, R.drawable.ic_close_red)
        }
    }

    override fun onAddProductException(e: Exception) = exceptionHandlingForAPIResponse(e)

    override fun onAdapterItemClickListener(position: Int) {
        mImageChangePosition = position
        showAddProductContainer()
        showAddProductImagePickerBottomSheet(position)
    }

    override fun onChipItemClickListener(position: Int) {
        try {
            mTempProductCategoryList.forEachIndexed { _, categoryItem -> categoryItem.isSelected = false }
            mTempProductCategoryList[position].isSelected = true
            enterCategoryEditText?.setText(mTempProductCategoryList[position].name)
            mItemCategoryId = mTempProductCategoryList[position].id
            enterCategoryEditText?.setSelection(mTempProductCategoryList[position].name?.length ?: 0)
            addProductChipsAdapter?.setAddProductStoreCategoryList(mTempProductCategoryList)
        } catch (e: Exception) {
            Log.e(TAG, "onChipItemClickListener: ${e.message}", e)
        }
    }

    override fun onToolbarSideIconClicked() {
        val sideView:View? = mActivity?.findViewById(R.id.sideIconToolbar)
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

    override fun onToolbarSecondIconClicked() = showDeleteConfirmationDialog()

    override fun onBackPressed(): Boolean {
        try {
            return if (!mIsOrderDeleted && mIsOrderEdited && shareProductContainer?.visibility != View.VISIBLE) {
                showGoBackDialog()
                return true
            } else {
                fragmentManager?.popBackStack()
                true
            }
        } catch (e: Exception) {
            return true
        }
    }

    private fun showGoBackDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mActivity?.let {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(it)
                    builder.apply {
                        setTitle(mAddProductStaticData?.text_go_back)
                        setMessage(mAddProductStaticData?.text_go_back_message)
                        setCancelable(true)
                        setNegativeButton(getString(R.string.text_no)) { dialog, _ -> dialog.dismiss() }
                        setPositiveButton(getString(R.string.txt_yes)) { dialog, _ ->
                            nameEditText?.hideKeyboard()
                            mIsOrderEdited = false
                            fragmentManager?.popBackStack()
                            dialog.dismiss()
                        }
                    }.create().show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "showGoBackDialog: ${e.message}", e)
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.let {
                val builder: AlertDialog.Builder = AlertDialog.Builder(it)
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
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_DELETE_ITEM,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
                        )
                        mService?.deleteItemServerCall(DeleteItemRequest(mItemId))
                    }
                    setNegativeButton(getString(R.string.text_no)) { dialog, _ -> dialog.dismiss() }
                }.create().show()
            }
        }
    }

    private fun showAddProductContainer() {
        if (View.VISIBLE == shareProductContainer?.visibility) {
            shareProductContainer?.visibility = View.GONE
            continueTextView?.visibility = View.VISIBLE
            if (!mIsAddNewProduct) continueTextView?.text = getString(R.string.save)
        }
    }

    override fun onLockedStoreShareSuccessResponse(lockedShareResponse: LockedStoreShareResponse) = showLockedStoreShareBottomSheet(lockedShareResponse)

    private fun showEmptyInventoryDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.let { context ->
                Dialog(context).apply {
                    setCancelable(true)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    setContentView(R.layout.empty_inventory_dialog)
                    val addTextView: TextView = findViewById(R.id.addTextView)
                    val disableTextView: TextView = findViewById(R.id.disableTextView)
                    val headingTextView: TextView = findViewById(R.id.headingTextView)
                    val messageTextView: TextView = findViewById(R.id.messageTextView)
                    headingTextView.text = mAddProductStaticData?.dialog_heading_inventory
                    messageTextView.text = mAddProductStaticData?.dialog_sub_heading_inventory
                    disableTextView.text = mAddProductStaticData?.dialog_cta_disable_inventory
                    addTextView.text = mAddProductStaticData?.dialog_cta_add_inventory
                    addTextView.setOnClickListener { this.dismiss() }
                    disableTextView.setOnClickListener {
                        this.dismiss()
                        if (mIsVariantAvailable) {
                            var unCheckedCheckBox = 0
                            mInventoryAdapter?.getDataSource()?.forEachIndexed { position, variantItem ->
                                if (0 == variantItem.inventoryCount && variantItem.isCheckboxSelected) {
                                    unCheckedCheckBox++
                                    variantItem.isCheckboxSelected = false
                                    variantItem.isEditTextEnabled = false
                                    variantItem.inventoryCount = 0
                                    mInventoryAdapter?.notifyItemChanged(position)
                                    if (isNotEmpty(mActiveVariantList)) mActiveVariantList?.get(position)?.managedInventory = Constants.INVENTORY_DISABLE
                                }
                            }
                            if (unCheckedCheckBox == (mInventoryAdapter?.getDataSource()?.size ?: 0)) {
                                disableInventorySwitch()
                            }
                        } else {
                            disableInventorySwitch()
                        }
                    }
                }.show()
            }
        }
    }

    private fun disableInventorySwitch() {
        val manageInventorySwitch: SwitchMaterial? = mContentView?.findViewById(R.id.manageInventorySwitch)
        manageInventorySwitch?.isChecked = false
    }
}