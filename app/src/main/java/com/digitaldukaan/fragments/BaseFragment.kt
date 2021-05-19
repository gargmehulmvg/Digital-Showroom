package com.digitaldukaan.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
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
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.digitaldukaan.MainActivity
import com.digitaldukaan.MyFcmMessageListenerService
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ImagesSearchAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit


open class BaseFragment : ParentFragment(), ISearchImageItemClicked {

    protected var mContentView: View? = null
    private var mProgressDialog: Dialog? = null
    protected var mActivity: MainActivity? = null
    private var mImageAdapter = ImagesSearchAdapter()
    private var mImagePickBottomSheet: BottomSheetDialog? = null

    companion object {
        private const val TAG = "BaseFragment"
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as MainActivity
        Log.d(TAG, "onAttach :: called in Application")
    }

    protected fun showProgressDialog(context: Context?, message: String? = "Please wait...") {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            context?.run {
                try {
                    mProgressDialog = Dialog(this)
                    mProgressDialog?.apply {
                        val view = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
                        message?.run {
                            val messageTextView : TextView = view.findViewById(R.id.progressDialogTextView)
                            messageTextView.text = this
                        }
                        setContentView(view)
                        setCancelable(false)
                        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    }?.show()
                } catch (e: Exception) {
                    Log.e(TAG, "showProgressDialog: ${e.message}", e)
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                        data = mapOf(
                            AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                            "Exception Point" to "showProgressDialog",
                            "Exception Message" to e.message,
                            "Exception Logs" to e.toString()
                        )
                    )
                }
            }
        }
    }

    open fun hideBottomNavigationView(isHidden: Boolean) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.run {
                bottomNavigationView.visibility = if (isHidden) View.GONE else View.VISIBLE
                premiumImageView.visibility = if (isHidden) View.GONE else View.VISIBLE
                premiumTextView.visibility = if (isHidden) View.GONE else View.VISIBLE
                view7.visibility = if (isHidden) View.GONE else View.VISIBLE
            }
        }
    }

    open fun onClick(view: View?) {}

    open fun onBackPressed() : Boolean  = false

    protected fun showCancellableProgressDialog(context: Context?, message: String? = "Please wait...") {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                context?.run {
                    mProgressDialog = Dialog(this)
                    val inflate = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null)
                    mProgressDialog?.setContentView(inflate)
                    message?.run {
                        val messageTextView : TextView = inflate.findViewById(R.id.progressDialogTextView)
                        messageTextView.text = this
                    }
                    mProgressDialog?.setCancelable(true)
                    mProgressDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    mProgressDialog?.show()
                }
            } catch (e: java.lang.Exception) {
                Log.e(TAG, "showCancellableProgressDialog: ${e.message}", e)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        "Exception Point" to "showCancellableProgressDialog",
                        "Exception Message" to e.message,
                        "Exception Logs" to e.toString()
                    )
                )
            }
        }
    }

    fun stopProgress() {
        try {
            mActivity?.runOnUiThread {
                if (mProgressDialog != null) {
                    mProgressDialog?.dismiss()
                    mProgressDialog = null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "stopProgress: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "Exception Point" to "stopProgress",
                    "Exception Message" to e.message,
                    "Exception Logs" to e.toString()
                )
            )
        }
    }

    fun showToast(message: String? = "sample testing") {
        mActivity?.showToast(message)
    }

    open fun exceptionHandlingForAPIResponse(e: Exception) {
        stopProgress()
        if (e is UnknownHostException) showToast(e.message)
        if (e is UnAuthorizedAccessException) {
            showToast(e.message)
            logoutFromApplication()
        }
        else showToast("Something went wrong")
    }

    protected fun showShortSnackBar(message: String? = "sample testing", showDrawable: Boolean = false, drawableID : Int = 0) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            message?.let {
                mContentView?.run {
                    try {
                        Snackbar.make(this, message, Snackbar.LENGTH_SHORT).apply {
                            if (showDrawable) {
                                val snackBarView = view
                                val snackBarTextView: TextView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text)
                                snackBarTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableID, 0)
                                snackBarTextView.compoundDrawablePadding = resources.getDimensionPixelOffset(R.dimen._5sdp)
                            }
                            mActivity?.let {
                                setBackgroundTint(ContextCompat.getColor(it, R.color.snack_bar_background))
                                setTextColor(ContextCompat.getColor(it, R.color.white))
                            }
                        }.show()
                    } catch (e : Exception) {
                        Log.e(TAG, "showShortSnackBar: ${e.message}", e)
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(
                                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                "Exception Point" to "showShortSnackBar",
                                "Exception Message" to e.message,
                                "Exception Logs" to e.toString()
                            )
                        )
                    }
                }
            }
        }
    }

    fun EditText.showKeyboard() {
        mActivity?.let {
            val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    fun EditText.hideKeyboard() {
        mActivity?.let {
            val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(this.windowToken, 0)
        }
    }

    fun TextView.showStrikeOffText() {
        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }

    open fun hideSoftKeyboard() {
        val imm = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mContentView?.windowToken, 0)
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
            mActivity?.launchFragment(fragment, addBackStack)
        }
    }

    open fun launchFragment(fragment: Fragment?, addBackStack: Boolean, animationView: View) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.launchFragmentWithAnimation(fragment, addBackStack, animationView)
        }
    }

    open fun clearFragmentBackStack() {
        try {
            val fm = mActivity?.supportFragmentManager
            fm?.run {
                for (i in 0 until backStackEntryCount) {
                    popBackStack()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "clearFragmentBackStack: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "Exception Point" to "clearFragmentBackStack",
                    "Exception Message" to e.message,
                    "Exception Logs" to e.toString()
                )
            )
        }
    }

    open fun copyDataToClipboard(string:String?) {
        val clipboard: ClipboardManager = mActivity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip: ClipData = ClipData.newPlainText(Constants.CLIPBOARD_LABEL, string)
        clipboard.setPrimaryClip(clip)
        showToast(getString(R.string.link_copied))
    }

    open fun showNoInternetConnectionDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mActivity?.let {
                    val builder: AlertDialog.Builder? = AlertDialog.Builder(it)
                    builder?.apply {
                        setTitle(getString(R.string.no_internet_connection))
                        setMessage(getString(R.string.turn_on_internet_message))
                        setCancelable(false)
                        setNegativeButton(getString(R.string.close)) { dialog, _ ->
                            onNoInternetButtonClick(true)
                            dialog.dismiss()
                        }
                    }?.create()?.show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "showNoInternetConnectionDialog: ${e.message}", e)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        "Exception Point" to "showNoInternetConnectionDialog",
                        "Exception Message" to e.message,
                        "Exception Logs" to e.toString()
                    )
                )
            }
        }
    }

    open fun storeStringDataInSharedPref(keyName: String, value: String?) {
        mActivity?.run {
            val editor = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE).edit()
            editor.putString(keyName, value)
            editor.apply()
        }
    }

    open fun getStringDataFromSharedPref(keyName: String?): String {
        val prefs = mActivity?.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)
        return prefs?.getString(keyName, "").toString()
    }

    open fun openUrlInBrowser(url:String?) {
        try {
            url?.let { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
        } catch (e: Exception) {
            Log.e(TAG, "openUrlInBrowser: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "Exception Point" to "openUrlInBrowser",
                    "Exception Message" to e.message,
                    "Exception Logs" to e.toString()
                )
            )
        }
    }

    open fun shareOnWhatsApp(sharingData: String?, image: Bitmap? = null) {
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "text/plain"
        val resInfoList = activity?.packageManager?.queryIntentActivities(shareIntent, 0)
        val shareIntentList = arrayListOf<Intent>()
        if (resInfoList?.isNotEmpty() == true) {
            for (resInfo in resInfoList) {
                val packageName = resInfo.activityInfo.packageName
                if (packageName.toLowerCase(Locale.getDefault()).contains("whatsapp")) {
                    val intent = Intent()
                    intent.component = ComponentName(packageName, resInfo.activityInfo.name)
                    intent.action = Intent.ACTION_SEND
                    intent.type = "text/plain"
                    image?.let {
                        intent.type = "image/jpeg"
                        intent.putExtra(Intent.EXTRA_STREAM, it.getImageUri(mActivity))
                    }
                    intent.`package` = packageName
                    intent.putExtra(Intent.EXTRA_TEXT, sharingData)
                    shareIntentList.add(intent)
                }
            }
        }
        if (shareIntentList.isEmpty()) {
            showToast("No apps to share!")
        } else {
            val chooserIntent = Intent.createChooser(Intent(), "Choose app to share")
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, shareIntentList.toTypedArray())
            activity?.startActivity(chooserIntent)
        }
    }

    protected fun shareDataOnWhatsAppByNumber(phone: String?, message: String? = "") {
        phone?.let {
            var phoneNumber = it
            if (!it.contains("+91")) phoneNumber = "+91$phoneNumber"
            try {
                openWhatsAppInBrowser(phoneNumber, message)
            } catch (e: Exception) {
                showToast(e.message)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        "Exception Point" to "shareDataOnWhatsAppByNumber",
                        "Exception Message" to e.message,
                        "Exception Logs" to e.toString()
                    )
                )
            }
        }
    }

    private fun openWhatsAppInBrowser(mobile: String?, data: String?) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("whatsapp://send?phone=$mobile&text=$data")))
        } catch (e: Exception) {
            Log.e(TAG, "openWhatsApp: ", e)
        }
    }

    open fun shareData(sharingData: String?, image: Bitmap?) {
        val whatsAppIntent = Intent(Intent.ACTION_SEND)
        whatsAppIntent.type = "text/plain"
        image?.let { whatsAppIntent.putExtra(Intent.EXTRA_STREAM, image.getImageUri(mActivity)) }
        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, sharingData)
        try {
            mActivity?.startActivity(whatsAppIntent)
        } catch (ex: ActivityNotFoundException) {
            showToast(ex.message)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "Exception Point" to "shareData",
                    "Exception Message" to ex.message,
                    "Exception Logs" to ex.toString()
                )
            )
        }
    }

    open fun shareDataOnWhatsAppWithImage(sharingData: String?, photoStr: String?) {
        try {
            CoroutineScopeUtils().runTaskOnCoroutineMain {
                Picasso.get().load(photoStr).into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                        bitmap?.let {
                            val imgUri = it.getImageUri(mActivity)
                            Log.d(TAG, "onBitmapLoaded: $imgUri")
                            imgUri?.let {
                                val whatsAppIntent = Intent(Intent.ACTION_SEND)
                                whatsAppIntent.apply {
                                    try {
                                        setPackage("com.whatsapp")
                                        putExtra(Intent.EXTRA_TEXT, sharingData)
                                        putExtra(Intent.EXTRA_STREAM, imgUri)
                                        type = "image/*"
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        mActivity?.startActivity(whatsAppIntent)
                                    } catch (ex: ActivityNotFoundException) {
                                        showToast("WhatsApp have not been installed. ${ex.message}")
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
        } catch (e: Exception) {
            Log.e(TAG, "shareDataOnWhatsAppWithImage: ${e.message}", e)
            showToast(getString(R.string.something_went_wrong))
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "Exception Point" to "shareDataOnWhatsAppWithImage",
                    "Exception Message" to e.message,
                    "Exception Logs" to e.toString()
                )
            )
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
        val appPackageName: String? = mActivity?.packageName
        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
        } catch (ignore: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
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
        mActivity?.let {
            AlertDialog.Builder(it).apply {
                setTitle("Select State")
                setItems(R.array.state_array) { dialogInterface: DialogInterface, i: Int ->
                    val stateList = resources.getStringArray(R.array.state_array).toList()
                    onAlertDialogItemClicked(stateList[i], id, i)
                    dialogInterface.dismiss()
                }
                setCancelable(false)
            }.create().show()
        }
    }

    open fun onAlertDialogItemClicked(selectedStr: String?, id: Int, position: Int) = Unit

    open fun onImageSelectionResult(base64Str : String?) = Unit

    open fun askCameraPermission() {
        mActivity?.let {
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    it,
                    arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA
                    ),
                    Constants.IMAGE_PICK_REQUEST_CODE
                )
                return
            }
        }
        showImagePickerBottomSheet()
    }

    open fun askContactPermission(): Boolean {
        mActivity?.let {
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_CONTACTS), Constants.CONTACT_REQUEST_CODE)
                return true
            }
        }
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            getContactsFromStorage2(mActivity)
        }
        return false
    }

    open fun showImagePickerBottomSheet() {
        mActivity?.let {
            val imageUploadStaticData = StaticInstances.mStaticData?.mCatalogStaticData
            mImagePickBottomSheet = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_image_pick, it.findViewById(R.id.bottomSheetContainer))
            mImagePickBottomSheet?.apply {
                setContentView(view)
                view?.run {
                    val bottomSheetUploadImageCloseImageView: ImageView = findViewById(R.id.bottomSheetUploadImageCloseImageView)
                    val bottomSheetUploadImageHeading: TextView = findViewById(R.id.bottomSheetUploadImageHeading)
                    val bottomSheetUploadImageCamera: View = findViewById(R.id.bottomSheetUploadImageCamera)
                    val bottomSheetUploadImageGallery: View = findViewById(R.id.bottomSheetUploadImageGallery)
                    val bottomSheetUploadImageCameraTextView: TextView = findViewById(R.id.bottomSheetUploadImageCameraTextView)
                    val bottomSheetUploadImageGalleryTextView: TextView = findViewById(R.id.bottomSheetUploadImageGalleryTextView)
                    val bottomSheetUploadImageSearchHeading: TextView = findViewById(R.id.bottomSheetUploadImageSearchHeading)
                    val bottomSheetUploadImageRemovePhotoTextView: TextView = findViewById(R.id.bottomSheetUploadImageRemovePhotoTextView)
                    val searchImageEditText: EditText = findViewById(R.id.searchImageEditText)
                    val searchImageImageView: View = findViewById(R.id.searchImageImageView)
                    val bottomSheetUploadImageRemovePhoto: View = findViewById(R.id.bottomSheetUploadImageRemovePhoto)
                    val searchImageRecyclerView: RecyclerView = findViewById(R.id.searchImageRecyclerView)
                    bottomSheetUploadImageGalleryTextView.text = imageUploadStaticData?.addGallery
                    bottomSheetUploadImageSearchHeading.text = imageUploadStaticData?.searchImageSubTitle
                    bottomSheetUploadImageRemovePhotoTextView.text = imageUploadStaticData?.removeImageText
                    bottomSheetUploadImageHeading.text = imageUploadStaticData?.uploadImageHeading
                    bottomSheetUploadImageCameraTextView.text = imageUploadStaticData?.takePhoto
                    searchImageEditText.hint = imageUploadStaticData?.searchImageHint
                    bottomSheetUploadImageCloseImageView.setOnClickListener { if (mImagePickBottomSheet?.isShowing == true) mImagePickBottomSheet?.dismiss() }
                    if (!StaticInstances.sIsStoreImageUploaded) {
                        bottomSheetUploadImageRemovePhotoTextView.visibility = View.GONE
                        bottomSheetUploadImageRemovePhoto.visibility = View.GONE
                    }
                    bottomSheetUploadImageCamera.setOnClickListener {
                        mImagePickBottomSheet?.dismiss()
                        openCamera()
                    }
                    bottomSheetUploadImageCameraTextView.setOnClickListener {
                        mImagePickBottomSheet?.dismiss()
                        openCamera()
                    }
                    bottomSheetUploadImageGallery.setOnClickListener {
                        mImagePickBottomSheet?.dismiss()
                        openMobileGalleryWithImage()
                    }
                    bottomSheetUploadImageGalleryTextView.setOnClickListener {
                        mImagePickBottomSheet?.dismiss()
                        openMobileGalleryWithImage()
                    }
                    bottomSheetUploadImageRemovePhoto.setOnClickListener {
                        mImagePickBottomSheet?.dismiss()
                        onImageSelectionResultFile(null)
                    }
                    bottomSheetUploadImageRemovePhotoTextView.setOnClickListener {
                        mImagePickBottomSheet?.dismiss()
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
                                            searchImageRecyclerView?.apply {
                                                layoutManager = GridLayoutManager(mActivity, 3)
                                                adapter = mImageAdapter
                                                list?.let { arrayList -> mImageAdapter.setSearchImageList(arrayList) }
                                            }
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }?.show()
        }
    }

    open fun openMobileGalleryWithImage() {
        mActivity?.run {
            ImagePicker.with(this)
                .saveDir(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!)
                .galleryMimeTypes(  //Exclude gif images
                    mimeTypes = arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg"
                    )
                )
                .galleryOnly()
                .crop(1f, 1f)                   // Crop image(Optional), Check Customization for more option
                .compress(1024)               // Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080) //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    try {
                        startForProfileImageResult.launch(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "openMobileGalleryWithImage: ${e.message}", e)
                    }
                }
        }
    }

    open fun openGalleryWithoutCrop() {
        mActivity?.run {
            ImagePicker.with(this)
                .saveDir(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!)
                .galleryMimeTypes(  //Exclude gif images
                    mimeTypes = arrayOf(
                        "image/png",
                        "image/jpg",
                        "image/jpeg"
                    )
                )
                .galleryOnly()
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080) //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    try {
                        startForProfileImageResult.launch(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "openMobileGalleryWithImage: ${e.message}", e)
                    }
                }
        }
    }

    open fun openCamera() {
        mActivity?.run {
            ImagePicker.with(this)
                .cameraOnly()
                .saveDir(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!)
                .crop(1f, 1f) //Crop image(Optional), Check Customization for more option
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080) //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    try {
                        startForProfileImageResult.launch(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "openMobileGalleryWithImage: ${e.message}", e)
                    }
                }
        }
    }

    open fun openFullCamera() {
        mActivity?.run {
            ImagePicker.with(this)
                .cameraOnly()
                .saveDir(getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!)
                .compress(1024)            //Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)
                .createIntent { intent ->
                    try {
                        startForProfileImageResult.launch(intent)
                    } catch (e: Exception) {
                        Log.e(TAG, "openMobileGalleryWithImage: ${e.message}", e)
                    }
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) = Unit

    private val startForProfileImageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            try {
                val resultCode = result.resultCode
                val data = result.data
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        try {
                            val fileUri = data?.data
                            onImageSelectionResultUri(fileUri)
                            val file: File? = File(fileUri?.path!!)
                            onImageSelectionResultFile(file)
                        } catch (e: Exception) {
                            Log.e(TAG, "onActivityResult: ${e.message}", e)
                            showToast(getString(R.string.something_went_wrong))
                        }
                    }
                    ImagePicker.RESULT_ERROR -> showToast(ImagePicker.getError(data))
                    else -> showToast("Task Cancelled")
                }
            } catch (e: Exception) {
                Log.e(TAG, "registerForActivityResult : ${e.message}", e)
            }
        }

    open fun onImageSelectionResultFile(file: File?, mode: String = "") = Unit

    open fun onImageSelectionResultUri(fileUri: Uri?) = Unit

    open fun onNoInternetButtonClick(isNegativeButtonClick: Boolean) = Unit

    override fun onSearchImageItemClicked(photoStr: String) {
        try {
            showCancellableProgressDialog(mActivity)
            Picasso.get().load(photoStr).into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    bitmap?.let {
                        val file = getImageFileFromBitmap(it, mActivity)
                        stopProgress()
                        mImagePickBottomSheet?.dismiss()
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
        } catch (e: Exception) {
            Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "Exception Point" to "onSearchImageItemClicked",
                    "Exception Message" to e.message,
                    "Exception Logs" to e.toString()
                )
            )
        }
    }

    protected fun updateNavigationBarState(actionId: Int) {
        mActivity?.let {
            if (actionId == R.id.menuPremium) {
                it.bottomNavigationView.background = ContextCompat.getDrawable(it, R.drawable.bottom_nav_premium_gradient_background)
                it.premiumTextView.setTextColor(ContextCompat.getColor(it, R.color.premium_text_color))
            } else {
                it.bottomNavigationView.background = null
                it.premiumTextView.setTextColor(ContextCompat.getColor(it, R.color.default_text_light_grey))
            }
            val menu: Menu = it.bottomNavigationView.menu
            menu.findItem(actionId).isChecked = true
        }
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
            val currentFragment = mActivity?.getCurrentFragment() ?: return
            when (incompleteProfilePageAction) {
                Constants.ACTION_LOGO -> askCameraPermission()
                Constants.ACTION_DESCRIPTION -> {
                    if (currentFragment is StoreDescriptionFragment) {
                        launchFragment(HomeFragment.newInstance(), true)
                    } else launchFragment(StoreDescriptionFragment.newInstance(getHeaderByActionInSettingKetList(profilePreviewResponse, Constants.ACTION_STORE_DESCRIPTION), incompleteProfilePageNumber, false, profilePreviewResponse), true)
                }
                Constants.ACTION_BUSINESS -> {
                    if (currentFragment is BusinessTypeFragment) launchFragment(HomeFragment.newInstance(), true) else launchFragment(BusinessTypeFragment.newInstance(getHeaderByActionInSettingKetList(profilePreviewResponse, Constants.ACTION_BUSINESS_TYPE),
                        incompleteProfilePageNumber, false, profilePreviewResponse), true)
                }
                Constants.ACTION_BANK -> launchFragment(BankAccountFragment.newInstance(getHeaderByActionInSettingKetList(profilePreviewResponse, Constants.ACTION_BANK_ACCOUNT),
                    incompleteProfilePageNumber, false, profilePreviewResponse), true)
                else -> launchFragment(HomeFragment.newInstance(), true)
            }
        }
    }

    protected fun showSearchDialog(staticData: OrderPageStaticTextResponse?, mobileNumberString: String, orderIdStr: String, isError: Boolean = false) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mActivity?.let {
                val view = LayoutInflater.from(mActivity).inflate(R.layout.search_dialog, null)
                val dialog = Dialog(it)
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
                        val errorTextView: TextView = findViewById(R.id.errorTextView)
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
                            staticData?.error_mandatory_field = "This field is mandatory"
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
                            AppEventsManager.pushAppEvents(
                                eventName = AFInAppEventType.EVENT_SEARCH_CLICK,
                                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)
                                , AFInAppEventParameterName.SEARCH_BY to if (inputMobileNumber.isEmpty()) AFInAppEventParameterName.PHONE else AFInAppEventParameterName.ORDER_ID)
                            )
                            onSearchDialogContinueButtonClicked(inputOrderId, inputMobileNumber)
                        }
                        mobileNumberEditText.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(p0: Editable?) {
                                val str = p0?.toString()
                                if (str?.isNotEmpty() == true) errorTextView.visibility = View.GONE
                            }

                            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                Log.d(TAG, "beforeTextChanged: do nothing")
                            }

                            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                Log.d(TAG, "onTextChanged: do nothing")
                            }

                        })
                        if (isError) {
                            errorTextView.visibility = View.VISIBLE
                            errorTextView.text = "No order found with this ${if (mobileNumberString.isEmpty()) "Order ID" else "Mobile Number"}"
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

    open fun showDontShowDialog(item: OrderItemResponse?, staticData: OrderPageStaticTextResponse?) {
        mActivity?.let {
            val builder = AlertDialog.Builder(it)
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
                    isCheckBoxVisible = false
                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        isCheckBoxVisible = isChecked
                    }
                }
            }.show()
        }
    }

    open fun onDontShowDialogPositiveButtonClicked(item: OrderItemResponse?) = Unit

    protected fun showImageDialog(imageStr: String?) {
        mActivity?.run {
            Dialog(this).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(true)
                setContentView(R.layout.image_dialog)
                val imageView: ImageView = findViewById(R.id.imageView)
                imageStr?.let {
                    try {
                        Picasso.get().load(it).into(imageView)
                    } catch (e: Exception) {
                        Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(
                                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                "Exception Point" to "showImageDialog",
                                "Exception Message" to e.message,
                                "Exception Logs" to e.toString()
                            )
                        )
                    }
                }
            }.show()
        }
    }

    protected fun showMasterCatalogBottomSheet(addProductBannerStaticDataResponse: AddProductBannerTextResponse?, addProductStaticText: AddProductStaticText?, mode: String) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            try {
                mActivity?.run {
                    val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
                    val view = LayoutInflater.from(mActivity).inflate(
                        R.layout.bottom_sheet_add_products_catalog_builder,
                        findViewById(R.id.bottomSheetContainer)
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
                            bannerImageView?.let {
                                try {
                                    Picasso.get().load(addProductBannerStaticDataResponse?.image_url).into(it)
                                } catch (e: Exception) {
                                    Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                                }
                            }
                            closeImageView.setOnClickListener { bottomSheetDialog.dismiss() }
                            buttonTextView.setOnClickListener{
                                bottomSheetDialog.dismiss()
                                AppEventsManager.pushAppEvents(
                                    eventName = AFInAppEventType.EVENT_CATALOG_BUILDER_TRY_NOW,
                                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                    data = mapOf(
                                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                        AFInAppEventParameterName.PATH to if (mode == Constants.MODE_PRODUCT_LIST) Constants.MODE_PRODUCT_LIST else Constants.MODE_ADD_PRODUCT
                                    )
                                )
                                launchFragment(ExploreCategoryFragment.newInstance(addProductStaticText), true)
                            }
                        }
                    }.show()
                }
            } catch (e: Exception) {
                Log.e(TAG, "showMasterCatalogBottomSheet: ${e.message}", e)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(
                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        "Exception Point" to "showMasterCatalogBottomSheet",
                        "Exception Message" to e.message,
                        "Exception Logs" to e.toString()
                    )
                )
            }
        }
    }

    protected fun openLocationSettings(isBackRequired: Boolean) {
        mActivity?.let {
            AlertDialog.Builder(it).apply {
                setTitle("Permission")
                setMessage("Please allow Location permission")
                setPositiveButton(getString(R.string.txt_yes)) { dialogInterface, _ ->
                    dialogInterface?.dismiss()
                    if (isBackRequired) it.onBackPressed()
                    it.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
                setNegativeButton(getString(R.string.text_no)) { dialogInterface, _ -> dialogInterface?.dismiss() }
            }.create().show()
        }
    }

    protected fun openMobileGalleryWithImage(file: File) {
        Log.d(TAG, "openMobileGalleryWithImage: ${file.name}")
        val galleryIntent = Intent()
        galleryIntent.action = Intent.ACTION_VIEW
        galleryIntent.setDataAndType(Uri.fromFile(file), "image/*")
        startActivity(galleryIntent)
    }

    fun showDownloadNotification(file: File, titleStr: String?) {
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.fromFile(file), "image/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            mActivity?.let {
                val pendingIntent: PendingIntent = PendingIntent.getActivity(it, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                MyFcmMessageListenerService.createNotification(titleStr, "Download completed.", pendingIntent, it)
            }
        } catch (e: Exception) {
            showToast(e.message)
        }
    }

    fun logoutFromApplication() {
        mActivity?.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)?.edit()?.clear()?.apply()
        clearFragmentBackStack()
        storeStringDataInSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN, "")
        storeStringDataInSharedPref(Constants.USER_AUTH_TOKEN, "")
        storeStringDataInSharedPref(Constants.STORE_NAME, "")
        storeStringDataInSharedPref(Constants.USER_MOBILE_NUMBER, "")
        storeStringDataInSharedPref(Constants.STORE_ID, "")
        launchFragment(LoginFragment.newInstance(), false)
    }

}