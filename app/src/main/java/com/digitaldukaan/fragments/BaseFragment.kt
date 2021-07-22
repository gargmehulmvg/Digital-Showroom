package com.digitaldukaan.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
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
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.digitaldukaan.MainActivity
import com.digitaldukaan.MyFcmMessageListenerService
import com.digitaldukaan.R
import com.digitaldukaan.adapters.ContactAdapter
import com.digitaldukaan.adapters.ImagesSearchAdapter
import com.digitaldukaan.adapters.OrderNotificationsAdapter
import com.digitaldukaan.constants.*
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.interfaces.IContactItemClicked
import com.digitaldukaan.interfaces.ISearchItemClicked
import com.digitaldukaan.models.dto.ContactModel
import com.digitaldukaan.models.request.PaymentLinkRequest
import com.digitaldukaan.models.request.UpdatePaymentMethodRequest
import com.digitaldukaan.models.response.*
import com.digitaldukaan.network.RetrofitApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.yalantis.ucrop.UCrop
import id.zelory.compressor.Compressor
import io.sentry.Sentry
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


open class BaseFragment : ParentFragment(), ISearchItemClicked, LocationListener {

    protected var mContentView: View? = null
    private var mProgressDialog: Dialog? = null
    protected var mActivity: MainActivity? = null
    private var mImageAdapter = ImagesSearchAdapter()
    private var mImagePickBottomSheet: BottomSheetDialog? = null

    companion object {
        private const val TAG = "BaseFragment"
        private var mCurrentPhotoPath = ""
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as MainActivity
        Log.d(TAG, "onAttach :: called in Application")
    }

    protected fun showProgressDialog(context: Context?, message: String? = "Please wait...") {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            context?.let {
                try {
                    mProgressDialog = Dialog(it)
                    mProgressDialog?.apply {
                        val view = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
                        message?.let {
                            val messageTextView : TextView = view.findViewById(R.id.progressDialogTextView)
                            messageTextView.text = it
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
            context?.let {
                try {
                    mProgressDialog = Dialog(it)
                    val inflate = LayoutInflater.from(it).inflate(R.layout.progress_dialog, null)
                    mProgressDialog?.setContentView(inflate)
                    message?.run {
                        val messageTextView : TextView = inflate.findViewById(R.id.progressDialogTextView)
                        messageTextView.text = this
                    }
                    mProgressDialog?.setCancelable(true)
                    mProgressDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    mProgressDialog?.show()
                } catch (e: Exception) {
                    Log.e(TAG, "showCancellableProgressDialog: ${e.message}", e)
                    Sentry.captureException(e, "$TAG showCancellableProgressDialog")
                }
            }
        }
    }

    fun stopProgress() {
        mActivity?.runOnUiThread {
            try {
                if (mProgressDialog != null) {
                    mProgressDialog?.dismiss()
                    mProgressDialog = null
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
    }

    fun showToast(message: String? = "sample testing") {
        mActivity?.showToast(message)
    }

    open fun exceptionHandlingForAPIResponse(e: Exception) {
        stopProgress()
        when (e) {
            is IllegalStateException -> showToast("System Error :: IllegalStateException :: Unable to reach Server")
            is IOException -> Log.e(TAG, "$TAG exceptionHandlingForAPIResponse: ${e.message}", e)
            is UnknownHostException -> showToast(e.message)
            is UnAuthorizedAccessException -> {
                showToast(e.message)
                logoutFromApplication()
            }
            else -> showToast("Something went wrong")
        }
    }

    protected fun showShortSnackBar(message: String? = "sample testing", showDrawable: Boolean = false, drawableID : Int = 0) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            mContentView?.run {
                try {
                    var msg = ""
                    message?.let { msg = it }
                    Snackbar.make(this, msg, Snackbar.LENGTH_SHORT).apply {
                        if (showDrawable) {
                            val snackBarView = view
                            val snackBarTextView: TextView = snackBarView.findViewById(com.google.android.material.R.id.snackbar_text)
                            snackBarTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, drawableID, 0)
                            snackBarTextView.compoundDrawablePadding =
                                resources.getDimensionPixelOffset(R.dimen._5sdp)
                        }
                        mActivity?.let {
                            setBackgroundTint(ContextCompat.getColor(it, R.color.snack_bar_background))
                            setTextColor(ContextCompat.getColor(it, R.color.white))
                        }
                    }.show()
                } catch (e: Exception) {
                    Log.e(TAG, "showShortSnackBar: ${e.message}", e)
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                        data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), "Exception Point" to "showShortSnackBar", "Exception Message" to e.message, "Exception Logs" to e.toString()
                        )
                    )
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
            fm?.let {
                for (i in 0 until it.backStackEntryCount) {
                    it.popBackStack()
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
        try {
            val clipboard: ClipboardManager = mActivity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText(Constants.CLIPBOARD_LABEL, string)
            clipboard.setPrimaryClip(clip)
            showToast(getString(R.string.link_copied))
        } catch (e: Exception) {
            Log.e(TAG, "copyDataToClipboard: ${e.message}", e)
        }
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
        if (null != image) {
            mActivity?.let {
                if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.STORAGE_REQUEST_CODE)
                    return
                }
            }
        }
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.type = "*/*"
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
                        intent.type = "*/*"
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
        if (null == image) {
            mActivity?.let {
                if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.STORAGE_REQUEST_CODE)
                    return
                }
            }
        }
        val whatsAppIntent = Intent(Intent.ACTION_SEND)
        whatsAppIntent.type = "text/plain"
        whatsAppIntent.putExtra(Intent.EXTRA_TEXT, sharingData)
        image?.let {
            whatsAppIntent.type = "*/*"
            whatsAppIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            whatsAppIntent.putExtra(Intent.EXTRA_STREAM, image.getImageUri(mActivity))
        }
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
                                    putExtra(Intent.EXTRA_STREAM, it)
                                    type = "*/*"
                                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    mActivity?.startActivity(this)
                                } catch (e: Exception) {
                                    showToast("WhatsApp have not been installed. ${e.message}")
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
            val imageUploadStaticData = StaticInstances.sStaticData?.mCatalogStaticData
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
                        openCameraWithCrop()
                    }
                    bottomSheetUploadImageCameraTextView.setOnClickListener {
                        mImagePickBottomSheet?.dismiss()
                        openCameraWithCrop()
                    }
                    bottomSheetUploadImageGallery.setOnClickListener {
                        mImagePickBottomSheet?.dismiss()
                        openMobileGalleryWithCrop()
                    }
                    bottomSheetUploadImageGalleryTextView.setOnClickListener {
                        mImagePickBottomSheet?.dismiss()
                        openMobileGalleryWithCrop()
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
                    mImageAdapter.setContext(mActivity)
                    searchImageImageView.setOnClickListener {
                        searchImageEditText.hideKeyboard()
                        if (searchImageEditText.text.trim().toString().isEmpty()) {
                            searchImageEditText.error = getString(R.string.mandatory_field_message)
                            searchImageEditText.requestFocus()
                            return@setOnClickListener
                        }
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_BING_SEARCH,
                            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), AFInAppEventParameterName.BING_TEXT to searchImageEditText.text.trim().toString())
                        )
                        showProgressDialog(mActivity)
                        CoroutineScopeUtils().runTaskOnCoroutineBackground {
                            try {
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
                            } catch (e: Exception) {
                                Sentry.captureException(e, "showImagePickerBottomSheet: exception")
                                exceptionHandlingForAPIResponse(e)
                            }
                        }
                    }
                }
            }?.show()
        }
    }

    open fun openMobileGalleryWithCrop() {
        mActivity?.let {
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), Constants.IMAGE_PICK_REQUEST_CODE)
                return
            }
        }
        mActivity?.run {
            val fileName = "tempFile_${System.currentTimeMillis()}"
            val storageDirectory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            try {
                val imageFile = File.createTempFile(fileName, ".jpg", storageDirectory)
                mCurrentPhotoPath = imageFile.absolutePath
                val cameraIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                val imageUri = FileProvider.getUriForFile(this, "com.digitaldukaan.fileprovider", imageFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                cameraGalleryWithCropIntentResult.launch(cameraIntent)
            } catch (e: Exception) {
                showToast(e.message)
                Log.e(TAG, "openCamera: ${e.message}", e)
            }
        }
    }

    open fun openMobileGalleryWithoutCrop() {
        mActivity?.let {
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), Constants.IMAGE_PICK_REQUEST_CODE)
                return
            }
        }
        mActivity?.run {
            val fileName = "tempFile_${System.currentTimeMillis()}"
            val storageDirectory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            try {
                val imageFile = File.createTempFile(fileName, ".jpg", storageDirectory)
                mCurrentPhotoPath = imageFile.absolutePath
                val cameraIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
                val imageUri = FileProvider.getUriForFile(this, "com.digitaldukaan.fileprovider", imageFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                cameraGalleryWithoutCropIntentResult.launch(cameraIntent)
            } catch (e: Exception) {
                showToast(e.message)
                Log.e(TAG, "openCamera: ${e.message}", e)
            }
        }
    }

    open fun openCameraWithoutCrop() {
        mActivity?.let {
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), Constants.IMAGE_PICK_REQUEST_CODE)
                return
            }
        }
        mActivity?.run {
            showCancellableProgressDialog(this)
            val fileName = "tempFile_${System.currentTimeMillis()}"
            val storageDirectory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            try {
                val imageFile = File.createTempFile(fileName, ".jpg", storageDirectory)
                mCurrentPhotoPath = imageFile.absolutePath
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val imageUri = FileProvider.getUriForFile(this, "com.digitaldukaan.fileprovider", imageFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                cameraGalleryWithoutCropIntentResult.launch(cameraIntent)
            } catch (e: Exception) {
                Log.e(TAG, "openCamera: ${e.message}", e)
                mActivity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), Constants.IMAGE_PICK_REQUEST_CODE) }
            }
        }
    }

    open fun openCameraWithCrop() {
        mActivity?.let {
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), Constants.IMAGE_PICK_REQUEST_CODE)
                return
            }
        }
        mActivity?.run {
            showCancellableProgressDialog(this)
            val fileName = "tempFile_${System.currentTimeMillis()}"
            val storageDirectory = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            try {
                val imageFile = File.createTempFile(fileName, ".jpg", storageDirectory)
                mCurrentPhotoPath = imageFile.absolutePath
                val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                val imageUri = FileProvider.getUriForFile(this, "com.digitaldukaan.fileprovider", imageFile)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                cameraGalleryWithCropIntentResult.launch(cameraIntent)
            } catch (e: Exception) {
                Log.e(TAG, "openCamera: ${e.message}", e)
                mActivity?.let { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), Constants.IMAGE_PICK_REQUEST_CODE) }
            }
        }
    }

    private var cameraGalleryWithCropIntentResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "onActivityResult: ")
            if (result.resultCode == Activity.RESULT_OK) {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    try {
                        Log.d(TAG, "onActivityResult: OK")
                        val bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
                        Log.d(TAG, "onActivityResult: bitmap :: $bitmap")
                        stopProgress()
                        if (null == bitmap) handleGalleryResult(result, true) else handleCameraResult(bitmap, true)
                    } catch (e: Exception) {
                        Log.e(TAG, "resultLauncherForCamera: ${e.message}", e)
                    }
                }
            }
        }
    
    private var cameraGalleryWithoutCropIntentResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "onActivityResult: ")
            if (result.resultCode == Activity.RESULT_OK) {
                CoroutineScopeUtils().runTaskOnCoroutineMain {
                    showProgressDialog(mActivity)
                    try {
                        Log.d(TAG, "onActivityResult: OK")
                        val bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath)
                        Log.d(TAG, "onActivityResult: bitmap :: $bitmap")
                        stopProgress()
                        if (null == bitmap) handleGalleryResult(result, false) else handleCameraResult(bitmap, false)
                    } catch (e: Exception) {
                        Log.e(TAG, "resultLauncherForCamera: ${e.message}", e)
                    }
                }
            }
        }

    private suspend fun handleCameraResult(it: Bitmap?, isCropAllowed: Boolean) {
        var file = File(mCurrentPhotoPath)
        mActivity?.run {
            Log.d(TAG, "ORIGINAL :: ${file.length() / (1024)} KB")
            file = Compressor.compress(this, file)
            Log.d(TAG, "COMPRESSED :: ${file.length() / (1024)} KB")
        }
        if (file.length() / (1024 * 1024) >= mActivity?.resources?.getInteger(R.integer.image_mb_size) ?: 0) {
            showToast("Images more than ${mActivity?.resources?.getInteger(R.integer.image_mb_size)} are not allowed")
            return
        }
        val fileUri = it?.getImageUri(mActivity)
        if (isCropAllowed) {
            it?.let { uri -> startCropping(uri) }
        } else {
            stopProgress()
            onImageSelectionResultUri(fileUri)
            onImageSelectionResultFile(file)
            onImageSelectionResultFileAndUri(fileUri, file)
        }
    }

    private suspend fun handleGalleryResult(result: ActivityResult, isCropAllowed: Boolean) {
        val bitmap: Bitmap?
        Log.d(TAG, "onActivityResult: bitmap is null")
        val galleryUri = result.data?.data
        bitmap = getBitmapFromUri(galleryUri, mActivity)
        Log.d(TAG, "onActivityResult: bitmap :: $bitmap")
        var file = getImageFileFromBitmap(bitmap, mActivity)
        file?.let {
            Log.d(TAG, "ORIGINAL :: ${it.length() / (1024)} KB")
            mActivity?.run { file = Compressor.compress(this, it) }
            Log.d(TAG, "COMPRESSED :: ${it.length() / (1024)} KB")
            if (it.length() / (1024 * 1024) >= mActivity?.resources?.getInteger(R.integer.image_mb_size) ?: 0) {
                showToast("Images more than ${mActivity?.resources?.getInteger(R.integer.image_mb_size)} are not allowed")
                return@let
            }
            val fileUri = bitmap?.getImageUri(mActivity)
            if (isCropAllowed) {
                bitmap?.let { b -> startCropping(b) }
            } else {
                stopProgress()
                onImageSelectionResultUri(fileUri)
                onImageSelectionResultFile(file)
            }
        }
    }

    private fun startCropping(bitmap: Bitmap?) {
        mActivity?.let {
            val originalImgFile = File(it.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${System.currentTimeMillis()}_originalImgFile.jpg")
            bitmap?.let { b ->convertBitmapToFile(originalImgFile, b) }
            val croppedImgFile = File(it.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${System.currentTimeMillis()}_croppedImgFile.jpg")
            UCrop.of(Uri.fromFile(originalImgFile), Uri.fromFile(croppedImgFile))
                .withAspectRatio(1f, 1f)
                .withMaxResultSize(500, 500)
                .start(it)
            stopProgress()
        }
    }

    private fun convertBitmapToFile(destinationFile: File, bitmap: Bitmap) {
        //create a file to write bitmap data
        destinationFile.createNewFile()
        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, bos)
        val bitmapData = bos.toByteArray()
        //write the bytes in file
        val fos = FileOutputStream(destinationFile)
        fos.write(bitmapData)
        fos.flush()
        fos.close()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult: ")
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: OK")
            if (requestCode == UCrop.REQUEST_CROP) {
                Log.d(TAG, "onActivityResult: CROP_IMAGE_ACTIVITY_REQUEST_CODE ")
                data?.let {
                    val resultUri = UCrop.getOutput(data)
                    Log.d(TAG, "onActivityResult: CROP_IMAGE_ACTIVITY_REQUEST_CODE :: result uri :: $resultUri")
                    onImageSelectionResultUri(resultUri)
                    val croppedBitmap = getBitmapFromUri(resultUri, mActivity)
                    val croppedFile = getImageFileFromBitmap(croppedBitmap, mActivity)
                    onImageSelectionResultFile(croppedFile)
                }
            }
        }
    }

    override fun onSearchImageItemClicked(photoStr: String) {
        showCancellableProgressDialog(mActivity)
        Log.d(TAG, "onSearchImageItemClicked :: $photoStr")
        try {
            Picasso.get().load(photoStr).into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                    CoroutineScopeUtils().runTaskOnCoroutineMain {
                        bitmap?.let {
                            var file = getImageFileFromBitmap(it, mActivity)
                            file?.let {f ->
                                Log.d(TAG, "ORIGINAL :: ${f.length() / (1024)} KB")
                                mActivity?.let { file = Compressor.compress(it, f) }
                                Log.d(TAG, "COMPRESSED :: ${f.length() / (1024)} KB")
                                if (f.length() / (1024 * 1024) >= mActivity?.resources?.getInteger(R.integer.image_mb_size) ?: 0) {
                                    showToast("Images more than ${mActivity?.resources?.getInteger(R.integer.image_mb_size)} are not allowed")
                                    return@runTaskOnCoroutineMain
                                }
                            }
                            stopProgress()
                            mImagePickBottomSheet?.dismiss()
                            val imageUri = it.getImageUri(mActivity)
                            imageUri?.let {uri -> startCropping(bitmap) }
                        }
                    }
                }

                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    Log.d(TAG, "onPrepareLoad: ")
                    stopProgress()
                }

                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                    Log.d(TAG, "onBitmapFailed: ")
                    stopProgress()
                }
            })
        } catch (e: Exception) {
            stopProgress()
            Log.e(TAG, "picasso image loading issue: ${e.message}", e)
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
        StaticInstances.sStepsCompletedList?.let {
            var incompleteProfilePageAction = ""
            var incompleteProfilePageNumber = 0
            for (completedItem in it) {
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
                val message : String? = when(item?.displayStatus) {
                    Constants.DS_MARK_READY -> staticData?.dialog_message_prepaid_pickup
                    Constants.DS_OUT_FOR_DELIVERY -> staticData?.dialog_message_prepaid_delivery
                    else -> staticData?.dialog_message
                }
                setMessage(message)
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

    protected fun showImageDialog(imageStr: String?) {
        mActivity?.let {
            Dialog(it).apply {
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

    protected fun showPaymentLinkSelectionDialog(amount: String, imageCdn: String = "") {
        mActivity?.let {
            Dialog(it).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(true)
                setContentView(R.layout.dialog_payment_link_selection)
                val staticText = StaticInstances.sOrderPageInfoStaticData
                val bottomSheetClose: ImageView = findViewById(R.id.bottomSheetClose)
                val headingTextView: TextView = findViewById(R.id.headingTextView)
                val smsTextView: TextView = findViewById(R.id.smsTextView)
                val whatsAppTextView: TextView = findViewById(R.id.whatsappTextView)
                val smsImageView: ImageView = findViewById(R.id.smsImageView)
                val whatsAppImageView: ImageView = findViewById(R.id.whatsAppImageView)
                headingTextView.text = staticText?.heading_share_payment_link
                smsTextView.text = staticText?.text_sms
                whatsAppTextView.text = staticText?.text_whatsapp
                smsTextView.setOnClickListener {
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_PAYMENT_LINK_SENT,
                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                        data = mapOf(
                            AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                            AFInAppEventParameterName.PATH to AFInAppEventParameterName.SMS
                        )
                    )
                    this.dismiss()
                    onSMSIconClicked()
                    showContactPickerBottomSheet(amount, imageCdn)
                }
                smsImageView.setOnClickListener {
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_PAYMENT_LINK_SENT,
                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                        data = mapOf(
                            AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                            AFInAppEventParameterName.PATH to AFInAppEventParameterName.SMS
                        )
                    )
                    this.dismiss()
                    onSMSIconClicked()
                    showContactPickerBottomSheet(amount, imageCdn)
                }
                whatsAppImageView.setOnClickListener {
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_PAYMENT_LINK_SENT,
                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                        data = mapOf(
                            AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                            AFInAppEventParameterName.PATH to AFInAppEventParameterName.WHATSAPP
                        )
                    )
                    this.dismiss()
                    onWhatsAppIconClicked()
                    val request = PaymentLinkRequest(Constants.MODE_WHATS_APP, amount.toDouble(), "", imageCdn)
                    initiatePaymentLinkServerCall(request)
                }
                whatsAppTextView.setOnClickListener {
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_PAYMENT_LINK_SENT,
                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                        data = mapOf(
                            AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                            AFInAppEventParameterName.PATH to AFInAppEventParameterName.WHATSAPP
                        )
                    )
                    this.dismiss()
                    onWhatsAppIconClicked()
                    val request = PaymentLinkRequest(Constants.MODE_WHATS_APP, amount.toDouble(), "", imageCdn)
                    initiatePaymentLinkServerCall(request)
                }
                bottomSheetClose.setOnClickListener {
                    this.dismiss()
                }
                window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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

    protected fun openMobileGalleryWithCrop(file: File) {
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
        launchFragment(LoginFragment.newInstance(), true)
    }

    fun shareStoreOverWhatsAppServerCall() {
        showProgressDialog(mActivity)
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val response = RetrofitApi().getServerCallObject()?.getShareStore()
                response?.let {
                    stopProgress()
                    if (it.isSuccessful) {
                        it.body()?.let {
                            withContext(Dispatchers.Main) {
                                if (it.mIsSuccessStatus) shareOnWhatsApp(Gson().fromJson<String>(it.mCommonDataStr, String::class.java)) else showShortSnackBar(it.mMessage, true, R.drawable.ic_close_red)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

    open fun getTransactionDetailBottomSheet(txnId: String?, path: String = "") {
        if (!isEmpty(path)) {
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SET_ORDER_PAYMENT_DETAIL,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(
                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    AFInAppEventParameterName.TRANSACTION_ID to txnId,
                    AFInAppEventParameterName.PATH to path
                )
            )
        }
        showProgressDialog(mActivity)
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val response = RetrofitApi().getServerCallObject()?.getOrderTransactions(txnId)
                response?.let {
                    stopProgress()
                    if (it.isSuccessful) {
                        it.body()?.let {
                            withContext(Dispatchers.Main) {
                                if (it.mIsSuccessStatus) {
                                    val responseObj = Gson().fromJson<TransactionDetailResponse>(it.mCommonDataStr, TransactionDetailResponse::class.java)
                                    if (isEmpty(path)) onTransactionDetailResponse(responseObj) else showTransactionDetailBottomSheet(responseObj)
                                } else showShortSnackBar(it.mMessage, true, R.drawable.ic_close_red)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

    private fun showTransactionDetailBottomSheet(response: TransactionDetailResponse?) {
        mActivity?.run {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(this).inflate(
                R.layout.bottom_sheet_transaction_detail,
                findViewById(R.id.bottomSheetContainer)
            )
            bottomSheetDialog.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
                view.run {
                    val staticText = response?.staticText
                    val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                    val billAmountValueTextView: TextView = findViewById(R.id.billAmountValueTextView)
                    val txnChargeValueTextView: TextView = findViewById(R.id.txnChargeValueTextView)
                    val textViewTop: TextView = findViewById(R.id.textViewTop)
                    val textViewBottom: TextView = findViewById(R.id.textViewBottom)
                    val billAmountTextView: TextView = findViewById(R.id.billAmountTextView)
                    val txnChargeTextView: TextView = findViewById(R.id.txnChargeTextView)
                    val paymentModeTextView: TextView = findViewById(R.id.paymentModeTextView)
                    val amountSettleTextView: TextView = findViewById(R.id.amountSettleTextView)
                    val amountSettleValueTextView: TextView = findViewById(R.id.amountSettleValueTextView)
                    val txnId: TextView = findViewById(R.id.txnId)
                    val bottomDate: TextView = findViewById(R.id.bottomDate)
                    val displayMessage: TextView = findViewById(R.id.displayMessage)
                    val ctaTextView: TextView = findViewById(R.id.ctaTextView)
                    val closeImageView: ImageView = findViewById(R.id.closeImageView)
                    val imageViewBottom: ImageView = findViewById(R.id.imageViewBottom)
                    val paymentModeImageView: ImageView = findViewById(R.id.paymentModeImageView)
                    textViewTop.text = response?.transactionMessage
                    textViewBottom.text = response?.settlementMessage
                    billAmountTextView.text = staticText?.bill_amount
                    amountSettleTextView.text = staticText?.amount_to_settled
                    paymentModeTextView.text = staticText?.payment_mode
                    txnId.text = getStringDateTimeFromTransactionDetailDate(getCompleteDateFromOrderString(response?.transactionTimestamp))
                    when (Constants.ORDER_STATUS_PAYOUT_SUCCESS) {
                        response?.settlementState -> {
                            bottomDate.visibility = View.VISIBLE
                            val bottomDisplayStr = "${getStringDateTimeFromTransactionDetailDate(getCompleteDateFromOrderString(response.settlementTimestamp))} ${if (!isEmpty(response.utr)) "| UTR : ${response.utr}" else ""}"
                            bottomDate.text = bottomDisplayStr
                        }
                        else -> bottomDate.visibility = View.GONE
                    }
                    if (null != response?.ctaItem) {
                        displayMessage.text = response.ctaItem?.displayMessage
                        ctaTextView.visibility = View.VISIBLE
                        displayMessage.visibility = View.VISIBLE
                        ctaTextView.setOnClickListener {
                            when(response.ctaItem?.action) {
                                Constants.ACTION_ADD_BANK -> {
                                    bottomSheetDialog.dismiss()
                                    launchFragment(BankAccountFragment.newInstance(null, 0, false, null), false)
                                }
                            }
                        }
                    } else {
                        ctaTextView.visibility = View.INVISIBLE
                        displayMessage.visibility = View.GONE
                    }
                    txnChargeTextView.text = "${staticText?.transaction_charges} (${response?.transactionCharges}%)"
                    bottomSheetHeadingTextView.text = "${staticText?.order_number} ${response?.orderId}"
                    billAmountValueTextView.text = "${getString(R.string.rupee_symbol)} ${response?.amount}"
                    txnChargeValueTextView.text = "${getString(R.string.rupee_symbol)} ${response?.transactionChargeAmount}"
                    amountSettleValueTextView.text = "${getString(R.string.rupee_symbol)} ${response?.settlementAmount}"
                    if (!isEmpty(response?.paymentImage)) mActivity?.let { context -> Glide.with(context).load(response?.paymentImage).into(paymentModeImageView) }
                    if (!isEmpty(response?.settlementCdn)) mActivity?.let { context -> Glide.with(context).load(response?.settlementCdn).into(imageViewBottom) }
                    closeImageView.setOnClickListener { bottomSheetDialog.dismiss() }
                }
            }.show()
        }
    }

    open fun getOrderNotificationBottomSheet(path: String) {
        AppEventsManager.pushAppEvents(
            eventName = AFInAppEventType.EVENT_SET_NEW_ORDER_NOTIFICATIONS,
            isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
            data = mapOf(
                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                AFInAppEventParameterName.PATH to path
            )
        )
        showProgressDialog(mActivity)
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val response = RetrofitApi().getServerCallObject()?.getOrderNotificationPageInfo()
                response?.let {
                    stopProgress()
                    if (it.isSuccessful) {
                        it.body()?.let {
                            withContext(Dispatchers.Main) {
                                if (it.mIsSuccessStatus) {
                                    val responseObj = Gson().fromJson<OrderNotificationResponse>(it.mCommonDataStr, OrderNotificationResponse::class.java)
                                    showOrderNotificationBottomSheet(responseObj)
                                } else showShortSnackBar(it.mMessage, true, R.drawable.ic_close_red)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

    private fun showOrderNotificationBottomSheet(response: OrderNotificationResponse?) {
        mActivity?.run {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_order_notification, findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                view.run {
                    val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                    val closeImageView: ImageView = findViewById(R.id.closeImageView)
                    val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
                    bottomSheetHeadingTextView.text = response?.headingBottomSheet
                    closeImageView.setOnClickListener { bottomSheetDialog.dismiss() }
                    recyclerView.apply {
                        layoutManager = LinearLayoutManager(mActivity)
                        adapter = OrderNotificationsAdapter(mActivity, response?.orderNotificationList, object : IAdapterItemClickListener {
                            override fun onAdapterItemClickListener(position: Int) {
                                val list = response?.orderNotificationList
                                val item = list?.get(position)
                                AppEventsManager.pushAppEvents(
                                    eventName = AFInAppEventType.EVENT_NEW_ORDER_NOTIFICATION_SELECTION,
                                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                                    data = mapOf(
                                        AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                        AFInAppEventParameterName.SELECTION to item?.eventName
                                    )
                                )
                                if (item?.isSelected != true) {
                                    bottomSheetDialog.dismiss()
                                    setOrderNotificationServerCall(item?.id ?: 0)
                                }
                            }
                        })
                    }
                }
            }.show()
        }
    }

    open fun setOrderNotificationServerCall(flag: Int) {
        showProgressDialog(mActivity)
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val response = RetrofitApi().getServerCallObject()?.updateNotificationFlag(UpdatePaymentMethodRequest(flag))
                response?.let {
                    stopProgress()
                    if (it.isSuccessful) {
                        it.body()?.let {
                            withContext(Dispatchers.Main) {
                                if (it.mIsSuccessStatus) {
                                    showShortSnackBar(it.mMessage, true, R.drawable.ic_check_circle)
                                } else showShortSnackBar(it.mMessage, true, R.drawable.ic_close_red)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

    open fun showContactPickerBottomSheet(amount: String, imageCdn: String = "") {
        if (!askContactPermission()) {
            mActivity?.let {
                val mContactPickerBottomSheet: BottomSheetDialog? = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_contact_pick, it.findViewById(R.id.bottomSheetContainer))
                mContactPickerBottomSheet?.apply {
                    val staticText = StaticInstances.sOrderPageInfoStaticData
                    setContentView(view)
                    setBottomSheetCommonProperty()
                    val contactList : ArrayList<ContactModel> = ArrayList()
                    val contactAdapter = ContactAdapter(contactList, mActivity, object : IContactItemClicked {
                        override fun onContactItemClicked(contact: ContactModel) {
                            mContactPickerBottomSheet.dismiss()
                            val request = PaymentLinkRequest(Constants.MODE_SMS, amount.toDouble(), contact.number ?: "", imageCdn)
                            initiatePaymentLinkServerCall(request, contact.name ?: "")
                        }

                    })
                    StaticInstances.sUserContactList.forEachIndexed { _, model -> contactList.add(model) }
                    view?.run {
                        val closeImageView: View = findViewById(R.id.bottomSheetUploadImageCloseImageView)
                        val bottomSheetHeading: TextView = findViewById(R.id.bottomSheetHeading)
                        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
                        val searchImageEditText: EditText = findViewById(R.id.searchImageEditText)
                        bottomSheetHeading.setHtmlData(staticText?.bottom_sheet_heading_enter_contact_number)
                        searchImageEditText.hint = staticText?.bottom_sheet_hint_enter_contact_number
                        searchImageEditText.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(editable: Editable?) {
                                val string = editable?.toString()
                                if (!isEmpty(string)) {
                                    val updatedContactList : ArrayList<ContactModel> = ArrayList()
                                    contactList.forEachIndexed { _, contactModel ->
                                        if (contactModel.name?.toLowerCase(Locale.getDefault())?.contains(string?.toLowerCase(Locale.getDefault()) ?: "") == true ||
                                            contactModel.number?.contains(string ?: "") == true) {
                                            updatedContactList.add(contactModel)
                                        }
                                    }
                                    if (isEmpty(updatedContactList)) {
                                        val contact = ContactModel(string, string)
                                        updatedContactList.add(contact)
                                    }
                                    contactAdapter.setContactList(updatedContactList)
                                }
                            }

                            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                Log.d(TAG, "beforeTextChanged: $p0")
                            }

                            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                                Log.d(TAG, "onTextChanged: $p0")
                            }

                        })
                        closeImageView.setOnClickListener { this@apply.dismiss() }
                        recyclerView.apply {
                            itemAnimator = DefaultItemAnimator()
                            layoutManager = LinearLayoutManager(it)
                            adapter = contactAdapter
                        }
                    }
                }?.show()
            }
        }
    }

    private fun initiatePaymentLinkServerCall(request: PaymentLinkRequest, contactName: String = "") {
        showProgressDialog(mActivity)
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val response = RetrofitApi().getServerCallObject()?.sendPaymentLink(request)
                response?.let {
                    stopProgress()
                    if (it.isSuccessful) {
                        it.body()?.let {
                            withContext(Dispatchers.Main) {
                                if (it.mIsSuccessStatus) {
                                    val responseObj = Gson().fromJson<PaymentLinkResponse>(it.mCommonDataStr, PaymentLinkResponse::class.java)
                                    if (request.mode == Constants.MODE_WHATS_APP) {
                                        shareOnWhatsApp(responseObj?.whatsapp?.text)
                                    } else {
                                        showPaymentLinkSuccessDialog(responseObj?.sms, contactName)
                                    }
                                    refreshOrderPage()
                                } else showShortSnackBar(it.mMessage, true, R.drawable.ic_close_red)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

    private fun showPaymentLinkSuccessDialog(smsObj: SMSItemResponse?, contactName: String) {
        mActivity?.let {
            Dialog(it).apply {
                requestWindowFeature(Window.FEATURE_NO_TITLE)
                setCancelable(true)
                setContentView(R.layout.dialog_payment_link_success)
                val dateHeading: TextView = findViewById(R.id.dateHeading)
                val timeHeading: TextView = findViewById(R.id.timeHeading)
                val amountHeading: TextView = findViewById(R.id.amountHeading)
                val dateTextView: TextView = findViewById(R.id.dateTextView)
                val timeTextView: TextView = findViewById(R.id.timeTextView)
                val idHeading: TextView = findViewById(R.id.idHeading)
                val amountTextView: TextView = findViewById(R.id.amountTextView)
                val linkSentToTextView: TextView = findViewById(R.id.linkSentToTextView)
                val floatingClose: ImageView = findViewById(R.id.floatingClose)
                val idStr = "${smsObj?.staticText?.text_id} ${smsObj?.orderId}"
                idHeading.text = idStr
                val amountStr = "₹ ${smsObj?.amount}"
                amountTextView.text = amountStr
                linkSentToTextView.text = "${smsObj?.staticText?.text_your_link_sent_to} $contactName"
                dateHeading.text = smsObj?.staticText?.text_date
                timeTextView.text = smsObj?.time
                dateTextView.text = smsObj?.date
                timeHeading.text = smsObj?.staticText?.text_time
                amountHeading.text = smsObj?.staticText?.text_amount
                floatingClose.setOnClickListener {
                    this.dismiss()
                }
                window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }.show()
        }
    }

    private var mGoogleApiClient: FusedLocationProviderClient? = null
    private var lastLocation: Location? = null
    private var mCurrentLatitude = 0.0
    private var mCurrentLongitude = 0.0

    private fun checkLocationPermission(): Boolean {
        mActivity?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), Constants.LOCATION_REQUEST_CODE)
                return true
            }
        }
        return false
    }
    
    protected fun getLocationFromGoogleMap() {
        try {
            if (checkLocationPermission()) return
            val locationManager = mActivity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 1f, this)
            mActivity?.let { context -> mGoogleApiClient = LocationServices.getFusedLocationProviderClient(context) }
            mGoogleApiClient?.lastLocation?.addOnCompleteListener(mActivity) { task ->
                if (task.isSuccessful && task.result != null) {
                    lastLocation = task.result
                    mCurrentLatitude = lastLocation?.latitude ?: 0.0
                    mCurrentLongitude = lastLocation?.longitude ?: 0.0
                    onLocationChanged(mCurrentLatitude, mCurrentLongitude)
                } else {
                    if (!isLocationEnabledInSettings(mActivity)) openLocationSettings(true)
                    mCurrentLatitude = 0.0
                    mCurrentLongitude =  0.0
                    onLocationChanged(mCurrentLatitude, mCurrentLongitude)
                }
            }
        } catch (e: Exception) {
            Sentry.captureException(e, "$TAG getLocationFromGoogleMap")
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d(TAG, "onLocationChanged() Latitude: " + location.latitude + " , Longitude: " + location.longitude)
        mCurrentLatitude = location.latitude
        mCurrentLongitude = location.longitude
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
        Log.d(TAG, "onStatusChanged :: p0 :: $p0, p1 :: $p1, p2:: $p2")
    }

    override fun onProviderEnabled(p0: String?) {
        Log.d(TAG, "onProviderEnabled :: $p0")
    }

    override fun onProviderDisabled(p0: String?) {
        Log.d(TAG, "onProviderDisabled :: $p0")
    }

}