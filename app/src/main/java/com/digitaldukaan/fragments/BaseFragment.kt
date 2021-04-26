package com.digitaldukaan.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ImagesSearchAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.interfaces.ISearchImageItemClicked
import com.digitaldukaan.models.response.*
import com.digitaldukaan.network.RetrofitApi
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.bottom_sheet_image_pick.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.UnknownHostException
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


open class BaseFragment : ParentFragment(), ISearchImageItemClicked {

    protected lateinit var mContentView: View
    private var mProgressDialog: Dialog? = null
    protected lateinit var mActivity: MainActivity
    private var mImageAdapter = ImagesSearchAdapter()
    private lateinit var mImagePickBottomSheet: BottomSheetDialog

    companion object {
        private const val TAG = "BaseFragment"
        lateinit var mStaticData: StaticTextResponse
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as MainActivity
        Log.d(TAG, "onAttach :: called in Application")
    }

    protected fun showProgressDialog(context: Context?, message: String? = "Please wait...") {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            context?.run {
                mProgressDialog = Dialog(this)
                mProgressDialog?.apply {
                    val view = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
                    message?.run {
                        val messageTextView : TextView = view.findViewById(R.id.progressDialogTextView)
                        messageTextView.text = this
                    }
                    setContentView(view)
                    setCancelable(false)
                    window!!.setBackgroundDrawable(
                        ColorDrawable(Color.TRANSPARENT)
                    )
                }?.show()
            }
        }
    }

    open fun hideBottomNavigationView(isHidden: Boolean) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.bottomNavigationView.visibility = if (isHidden) View.GONE else View.VISIBLE
            mActivity.premiumImageView.visibility = if (isHidden) View.GONE else View.VISIBLE
            mActivity.premiumTextView.visibility = if (isHidden) View.GONE else View.VISIBLE
            mActivity.view7.visibility = if (isHidden) View.GONE else View.VISIBLE
        }
    }

    open fun onClick(view: View?) {}

    open fun onBackPressed() : Boolean  = false

    protected fun showCancellableProgressDialog(context: Context?, message: String? = "Please wait...") {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            context?.run {
                mProgressDialog = Dialog(this)
                val inflate = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
                mProgressDialog?.setContentView(inflate)
                message?.run {
                    val messageTextView : TextView = inflate.findViewById(R.id.progressDialogTextView)
                    messageTextView.text = this
                }
                mProgressDialog?.setCancelable(true)
                mProgressDialog?.window!!.setBackgroundDrawable(
                    ColorDrawable(Color.TRANSPARENT)
                )
                mProgressDialog?.show()
            }
        }
    }

    fun stopProgress() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (mProgressDialog != null) {
                mProgressDialog?.let {
                    mProgressDialog?.dismiss()
                }
            }
        }
    }

    fun showToast(message: String? = "sample testing") {
        mActivity.showToast(message)
    }

    open fun exceptionHandlingForAPIResponse(e: Exception) {
        stopProgress()
        if (e is UnknownHostException) showToast(e.message)
        else showToast(e.message)
    }

    protected fun showShortSnackBar(message: String? = "sample testing", showDrawable: Boolean = false, drawableID : Int = 0) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            message?.let {
                Snackbar.make(mContentView, message, Snackbar.LENGTH_SHORT).apply {
                    if (showDrawable) {
                        val snackBarView = view
                        val snackBarTextView:TextView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text)
                        snackBarTextView.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            drawableID,
                            0
                        )
                        snackBarTextView.compoundDrawablePadding = resources.getDimensionPixelOffset(R.dimen._5sdp)
                    }
                    setBackgroundTint(ContextCompat.getColor(mActivity, R.color.snack_bar_background))
                    setTextColor(ContextCompat.getColor(mActivity, R.color.white))
                }.show()
            }
        }
    }

    fun EditText.showKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
    }

    fun EditText.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(this.windowToken, 0)
    }

    fun TextView.showStrikeOffText() {
        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    open fun hideSoftKeyboard() {
        val imm = mActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mContentView.windowToken, 0)
    }

    fun TextView.setHtmlData(string: String?) {
        string?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                this.text = Html.fromHtml(string, Html.FROM_HTML_MODE_COMPACT)
            } else {
                this.text = Html.fromHtml(string)
            }
        }
    }

    open fun launchFragment(fragment: Fragment?, addBackStack: Boolean) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.launchFragment(fragment, addBackStack)
        }
    }

    open fun launchFragment(fragment: Fragment?, addBackStack: Boolean, animationView: View) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.launchFragmentWithAnimation(fragment, addBackStack, animationView)
        }
    }

    open fun clearFragmentBackStack() {
        val fm = mActivity.supportFragmentManager
        for (i in 0 until fm.backStackEntryCount) {
            fm.popBackStack()
        }
    }

    open fun copyDataToClipboard(string:String?) {
        val clipboard: ClipboardManager = mActivity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(Constants.CLIPBOARD_LABEL, string)
        clipboard.setPrimaryClip(clip)
        showToast(getString(R.string.link_copied))
    }

    open fun showNoInternetConnectionDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            val builder: AlertDialog.Builder = AlertDialog.Builder(mActivity)
            builder.apply {
                setTitle(getString(R.string.no_internet_connection))
                setMessage(getString(R.string.turn_on_internet_message))
                setCancelable(false)
                setNegativeButton(getString(R.string.close)) { dialog, _ ->
                    onNoInternetButtonClick(true)
                    dialog.dismiss()
                }
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
        }
    }

    open fun storeStringDataInSharedPref(keyName: String, value: String?) {
        val editor = mActivity.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE).edit()
        editor.putString(keyName, value)
        editor.apply()
    }

    open fun getStringDataFromSharedPref(keyName: String?): String {
        val prefs = mActivity.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)
        return prefs.getString(keyName, "").toString()
    }

    open fun openUrlInBrowser(url:String?) {
        url?.let {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }
    }

    open fun shareDataOnWhatsApp(sharingData: String?) {
        val whatsAppIntent = Intent(Intent.ACTION_SEND)
        whatsAppIntent.type = "text/plain"
        whatsAppIntent.setPackage("com.whatsapp")
        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, sharingData)
        try {
            mActivity.startActivity(whatsAppIntent)
        } catch (ex: ActivityNotFoundException) {
            showToast(ex.message)
        }
    }

    open fun shareDataOnWhatsAppWithImage(sharingData: String?, image: Bitmap?) {
        val whatsAppIntent = Intent(Intent.ACTION_SEND)
        whatsAppIntent.type = "text/plain"
        whatsAppIntent.setPackage("com.whatsapp")
        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, sharingData)
        image?.let {
            whatsAppIntent.type = "image/jpeg"
            whatsAppIntent.putExtra(Intent.EXTRA_STREAM, image.getImageUri(mActivity))
        }
        try {
            mActivity.startActivity(whatsAppIntent)
        } catch (ex: ActivityNotFoundException) {
            showToast(ex.message)
        }
    }

    open fun shareData(sharingData: String?, image: Bitmap?) {
        val whatsAppIntent = Intent(Intent.ACTION_SEND)
        whatsAppIntent.type = "text/plain"
        image?.let { whatsAppIntent.putExtra(Intent.EXTRA_STREAM, image.getImageUri(mActivity)) }
        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, sharingData)
        try {
            mActivity.startActivity(whatsAppIntent)
        } catch (ex: ActivityNotFoundException) {
            showToast(ex.message)
        }
    }

    open fun shareDataOnWhatsAppWithImage(sharingData: String?, photoStr: String?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            Picasso.get().load(photoStr).into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    // loaded bitmap is here (bitmap)
                    bitmap?.let {
                        val imgUri = it.getImageUri(mActivity)
                        Log.d(TAG, "onBitmapLoaded: $imgUri")
                        imgUri?.let {
                            val whatsAppIntent = Intent(Intent.ACTION_SEND)
                            whatsAppIntent.apply {
                                type = "text/plain"
                                setPackage("com.whatsapp")
                                putExtra(Intent.EXTRA_TEXT, sharingData)
                                putExtra(Intent.EXTRA_STREAM, imgUri)
                                type = "image/jpeg"
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                try {
                                    mActivity.startActivity(whatsAppIntent)
                                } catch (ex: ActivityNotFoundException) {
                                    showToast("WhatsApp have not been installed.")
                                }
                            }
                        }
                    }
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    Log.d(TAG, "onPrepareLoad: ")
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    Log.d(TAG, "onBitmapFailed: ")
                }
            })
        }
    }

    open fun startShinningAnimation(view: View) {
        val service:ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
        service.scheduleAtFixedRate({
            CoroutineScopeUtils().runTaskOnCoroutineMain {
                val animation = TranslateAnimation(0f, view.width.toFloat(), 0f, 0f)
                animation.apply {
                    duration = 650
                    fillAfter = true
                    interpolator = AccelerateDecelerateInterpolator()
                }
                view.startAnimation(animation)
            }
        },Constants.SHINE_ANIMATION_INTERVAL, Constants.SHINE_ANIMATION_INTERVAL, TimeUnit.MILLISECONDS)
    }

    open fun openPlayStore() {
        val appPackageName: String = mActivity.packageName
        try {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$appPackageName")
                )
            )
        } catch (ignore: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")
                )
            )
        }
    }

    open fun BottomSheetDialog.setBottomSheetCommonProperty() {
        behavior.skipCollapsed = true
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setOnDismissListener {
            Handler(Looper.getMainLooper()).postDelayed(
                { hideSoftKeyboard() },
                Constants.TIMER_INTERVAL
            )
        }
    }

    protected open fun showStateSelectionDialog() {
        val builder = AlertDialog.Builder(mActivity)
        builder.setTitle("Select State")
        builder.setItems(R.array.state_array) { dialogInterface: DialogInterface, i: Int ->
            val stateList = resources.getStringArray(R.array.state_array).toList()
            onAlertDialogItemClicked(stateList[i], id, i)
            dialogInterface.dismiss()
        }
        builder.setCancelable(false)
        val alertDialog = builder.create()
        alertDialog.show()
    }

    open fun onAlertDialogItemClicked(selectedStr: String?, id: Int, position: Int) = Unit

    open fun onImageSelectionResult(base64Str : String?) = Unit

    open fun askCameraPermission() {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                mActivity,
                arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                Constants.IMAGE_PICK_REQUEST_CODE
            )
            return
        }
        showImagePickerBottomSheet()
    }

    open fun askContactPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(mActivity, arrayOf(Manifest.permission.READ_CONTACTS), Constants.CONTACT_REQUEST_CODE)
            return true
        }
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            getContactsFromStorage2(mActivity)
        }
        return false
    }

    open fun showImagePickerBottomSheet() {
        val imageUploadStaticData = mStaticData.mStaticData.mCatalogStaticData
        mImagePickBottomSheet = BottomSheetDialog(mActivity, R.style.BottomSheetDialogTheme)
        val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_image_pick, mActivity.findViewById(R.id.bottomSheetContainer))
        mImagePickBottomSheet.apply {
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
                bottomSheetUploadImageGalleryTextView.text = imageUploadStaticData.addGallery
                bottomSheetUploadImageSearchHeading.text = imageUploadStaticData.searchImageSubTitle
                bottomSheetUploadImageRemovePhotoTextView.text = imageUploadStaticData.removeImageText
                bottomSheetUploadImageHeading.text = imageUploadStaticData.uploadImageHeading
                bottomSheetUploadImageCameraTextView.text = imageUploadStaticData.takePhoto
                searchImageEditText.hint = imageUploadStaticData.searchImageHint
                bottomSheetUploadImageCloseImageView.setOnClickListener { if (mImagePickBottomSheet.isShowing) mImagePickBottomSheet.dismiss() }
                if (!StaticInstances.sIsStoreImageUploaded) {
                    bottomSheetUploadImageRemovePhotoTextView.visibility = View.GONE
                    bottomSheetUploadImageRemovePhoto.visibility = View.GONE
                }
                bottomSheetUploadImageCamera.setOnClickListener {
                    mImagePickBottomSheet.dismiss()
                    openCamera()
                }
                bottomSheetUploadImageCameraTextView.setOnClickListener {
                    mImagePickBottomSheet.dismiss()
                    openCamera()
                }
                bottomSheetUploadImageGallery.setOnClickListener {
                    mImagePickBottomSheet.dismiss()
                    openGallery()
                }
                bottomSheetUploadImageGalleryTextView.setOnClickListener {
                    mImagePickBottomSheet.dismiss()
                    openGallery()
                }
                bottomSheetUploadImageRemovePhoto.setOnClickListener {
                    mImagePickBottomSheet.dismiss()
                    onImageSelectionResultFile(null)
                }
                bottomSheetUploadImageRemovePhotoTextView.setOnClickListener {
                    mImagePickBottomSheet.dismiss()
                    onImageSelectionResultFile(null)
                }
                mImageAdapter.setSearchImageListener(this@BaseFragment)
                searchImageImageView.setOnClickListener {
                    searchImageEditText.hideKeyboard()
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
                                            adapter = mImageAdapter
                                            mImageAdapter.setSearchImageList(list)
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

    open fun openGallery() {
        ImagePicker.with(mActivity)
            .galleryOnly()
            .crop(1f, 1f)            //Crop image(Optional), Check Customization for more option
            .compress(1024)            //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            ) //Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }

    open fun openGalleryWithoutCrop() {
        ImagePicker.with(mActivity)
            .galleryOnly()
            .compress(1024)            //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            ) //Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }

    open fun openCamera() {
        ImagePicker.with(mActivity)
            .cameraOnly()
            .crop(1f, 1f) //Crop image(Optional), Check Customization for more option
            .compress(1024)            //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            ) //Final image resolution will be less than 1080 x 1080(Optional)
            .start()
    }

    open fun openFullCamera() {
        ImagePicker.with(mActivity)
            .cameraOnly()
            .compress(1024)            //Final image size will be less than 1 MB(Optional)
            .maxResultSize(
                1080,
                1080
            )
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            Activity.RESULT_OK -> {
                //Image Uri will not be null for RESULT_OK
                val fileUri = data?.data
                //imgProfile.setImageURI(fileUri)
                //You can get File object from intent
                val file: File? = ImagePicker.getFile(data)
                //You can also get File Path from intent
                //val filePath: String? = ImagePicker.getFilePath(data)
                //onImageSelectionResult(convertImageFileToBase64(file))
                onImageSelectionResultUri(fileUri)
                onImageSelectionResultFile(file)
            }
            ImagePicker.RESULT_ERROR -> {
                Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
            }
            else -> {
                Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    open fun onImageSelectionResultFile(file: File?, mode: String = "") = Unit

    open fun onImageSelectionResultUri(fileUri: Uri?) = Unit

    open fun onNoInternetButtonClick(isNegativeButtonClick: Boolean) = Unit

    override fun onSearchImageItemClicked(photoStr: String) {
        showProgressDialog(mActivity)
        Picasso.get().load(photoStr).into(object : com.squareup.picasso.Target {
            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                // loaded bitmap is here (bitmap)
                bitmap?.let {
                    val file = getImageFileFromBitmap(it, mActivity)
                    stopProgress()
                    if (::mImagePickBottomSheet.isInitialized) mImagePickBottomSheet.dismiss()
                    onImageSelectionResultFile(file, Constants.MODE_CROP)
                }
            }

            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                Log.d("TAG", "onPrepareLoad: ")
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                Log.d("TAG", "onBitmapFailed: ")
            }
        })
    }

    protected fun updateNavigationBarState(actionId: Int) {
        if (actionId == R.id.menuPremium) {
            mActivity.bottomNavigationView.background = ContextCompat.getDrawable(mActivity, R.drawable.bottom_nav_premium_gradient_background)
            mActivity.premiumTextView.setTextColor(ContextCompat.getColor(mActivity, R.color.premium_text_color))
        } else {
            mActivity.bottomNavigationView.background = null
            mActivity.premiumTextView.setTextColor(ContextCompat.getColor(mActivity, R.color.default_text_light_grey))
        }
        val menu: Menu = mActivity.bottomNavigationView.menu
        menu.findItem(actionId).isChecked = true
    }

    protected fun switchToInCompleteProfileFragment(profilePreviewResponse: ProfileInfoResponse?) {
        Log.d(TAG, "switchToInCompleteProfileFragment: called")
        StaticInstances.sStepsCompletedList?.run {
            var incompleteProfilePageAction = ""
            var incompleteProfilePageNumber = 0
            for (completedItem in this) {
                ++incompleteProfilePageNumber
                if (!completedItem.isCompleted) {
                    incompleteProfilePageAction = completedItem.action ?: ""
                    break
                }
            }
            when (incompleteProfilePageAction) {
                Constants.ACTION_LOGO -> askCameraPermission()
                Constants.ACTION_DESCRIPTION -> {
                    if (mActivity.getCurrentFragment() is StoreDescriptionFragment) {
                        launchFragment(HomeFragment.newInstance(), true)
                    } else launchFragment(StoreDescriptionFragment.newInstance(getHeaderByActionInSettingKetList(profilePreviewResponse, Constants.ACTION_STORE_DESCRIPTION), incompleteProfilePageNumber, false, profilePreviewResponse), true)
                }
                Constants.ACTION_BUSINESS -> {
                    if (mActivity.getCurrentFragment() is BusinessTypeFragment) launchFragment(HomeFragment.newInstance(), true) else launchFragment(BusinessTypeFragment.newInstance(getHeaderByActionInSettingKetList(profilePreviewResponse, Constants.ACTION_BUSINESS_TYPE),
                        incompleteProfilePageNumber, false, profilePreviewResponse), true)
                }
                Constants.ACTION_BANK -> launchFragment(BankAccountFragment.newInstance(getHeaderByActionInSettingKetList(profilePreviewResponse, Constants.ACTION_BANK_ACCOUNT),
                    incompleteProfilePageNumber, false, profilePreviewResponse), true)
                else -> launchFragment(HomeFragment.newInstance(), true)
            }
        }
    }

    protected fun showSearchDialog(staticData: OrderPageStaticTextResponse?, mobileNumberString: String, orderIdStr: String) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity.let {
                val view = LayoutInflater.from(mActivity).inflate(R.layout.search_dialog, null)
                val dialog = Dialog(mActivity)
                dialog.apply {
                    setContentView(view)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    view?.run {
                        val searchRadioGroup: RadioGroup = findViewById(R.id.searchRadioGroup)
                        val orderIdRadioButton: RadioButton = findViewById(R.id.orderIdRadioButton)
                        val phoneRadioButton: RadioButton = findViewById(R.id.phoneNumberRadioButton)
                        val searchInputLayout: TextInputLayout = findViewById(R.id.searchInputLayout)
                        val mobileNumberEditText: EditText = findViewById(R.id.mobileNumberEditText)
                        val searchByHeading: TextView = findViewById(R.id.searchByHeading)
                        val confirmTextView: TextView = findViewById(R.id.confirmTextView)
                        searchByHeading.text = staticData?.heading_search_dialog
                        orderIdRadioButton.text = staticData?.search_dialog_selection_one
                        phoneRadioButton.text = staticData?.search_dialog_selection_two
                        confirmTextView.text = staticData?.search_dialog_button_text
                        searchRadioGroup.setOnCheckedChangeListener { _, checkedId ->
                            when(checkedId) {
                                orderIdRadioButton.id -> {
                                    searchInputLayout.hint = "${staticData?.heading_search_dialog} ${staticData?.search_dialog_selection_one}"
                                }
                                phoneRadioButton.id -> {
                                    searchInputLayout.hint = "${staticData?.heading_search_dialog} ${staticData?.search_dialog_selection_two}"
                                }
                            }
                            mobileNumberEditText.setText("")
                        }
                        orderIdRadioButton.isChecked = true
                        if (mobileNumberString.isNotEmpty()) {
                            phoneRadioButton.isChecked = true
                            mobileNumberEditText.setText(mobileNumberString)
                        } else {
                            orderIdRadioButton.isChecked = true
                            mobileNumberEditText.setText(orderIdStr)
                        }
                        confirmTextView.setOnClickListener {
                            var inputOrderId = ""
                            var inputMobileNumber = ""
                            staticData?.error_mandatory_field = "This field is mandatory" // todo remove this
                            if (orderIdRadioButton.isChecked) {
                                inputOrderId = mobileNumberEditText.text.trim().toString()
                                if (inputOrderId.isEmpty()) {
                                    mobileNumberEditText.error = staticData?.error_mandatory_field
                                    mobileNumberEditText.requestFocus()
                                    return@setOnClickListener
                                }
                            } else {
                                inputMobileNumber = mobileNumberEditText.text.trim().toString()
                                if (inputMobileNumber.isEmpty()) {
                                    mobileNumberEditText.error = staticData?.error_mandatory_field
                                    mobileNumberEditText.requestFocus()
                                    return@setOnClickListener
                                }
                            }
                            dismiss()
                            onSearchDialogContinueButtonClicked(inputOrderId, inputMobileNumber)
                        }
                    }
                }.show()
            }
        }
    }

    open fun onSearchDialogContinueButtonClicked(inputOrderId: String, inputMobileNumber: String) = Unit

    open fun convertDateStringOfOrders(list: ArrayList<OrderItemResponse>) {
        list.forEachIndexed { _, itemResponse ->
            itemResponse.updatedDate = getDateFromOrderString(itemResponse.createdAt)
            itemResponse.updatedCompleteDate = getCompleteDateFromOrderString(itemResponse.createdAt)
        }
    }

    open fun openDontShowDialog(item: OrderItemResponse?, staticData: OrderPageStaticTextResponse?) {
        val builder = AlertDialog.Builder(mActivity)
        val view: View = layoutInflater.inflate(R.layout.dont_show_again_dialog, null)
        var isCheckBoxVisible = "" == PrefsManager.getStringDataFromSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN)
        builder.apply {
            setTitle(staticData?.dialog_text_alert)
            setMessage(staticData?.dialog_message)
            if (isCheckBoxVisible) setView(view)
            setPositiveButton(staticData?.dialog_text_yes) { dialogInterface, _ ->
                run {
                    dialogInterface.dismiss()
                    storeStringDataInSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN, if (isCheckBoxVisible) Constants.TEXT_YES else Constants.TEXT_NO)
                    onDontShowDialogPositiveButtonClicked(item)
                }
            }
            setNegativeButton(staticData?.dialog_text_no) { dialogInterface, _ ->
                run {
                    dialogInterface.dismiss()
                    if (isCheckBoxVisible) storeStringDataInSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN, Constants.TEXT_NO)
                }
            }
            view.run {
                val checkBox: CheckBox = view.findViewById(R.id.checkBox)
                checkBox.text = staticData?.dialog_check_box_text
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    run {
                        isCheckBoxVisible = isChecked
                    }
                }
            }
        }.show()
    }

    open fun onDontShowDialogPositiveButtonClicked(item: OrderItemResponse?) = Unit

    protected fun showImageDialog(imageStr: String?) {
        Dialog(mActivity).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setCancelable(true)
            setContentView(R.layout.image_dialog)
            val imageView: ImageView = findViewById(R.id.imageView)
            imageStr?.let {
                Picasso.get().load(it).into(imageView)
            }
        }.show()
    }

    protected fun shareDataOnWhatsAppByNumber(phone: String?, message: String?) {
        try {
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=91$phone&text=$message")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.setPackage("com.whatsapp")
            startActivity(intent)
        } catch (e: java.lang.Exception) {
            showToast("Error/n$e")
        }
    }

    protected fun showMaterCatalogBottomSheet(addProductBannerStaticDataResponse: AddProductBannerTextResponse?, addProductStaticText: AddProductStaticText?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
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
                        bottomSheetDialog.dismiss()
                        launchFragment(ExploreCategoryFragment.newInstance(addProductStaticText), true)
                    }
                }
            }.show()
        }
    }

}