package com.digitaldukaan.fragments

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.app.PendingIntent
import android.content.*
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.provider.Settings
import android.text.*
import android.text.InputFilter.LengthFilter
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.MainActivity
import com.digitaldukaan.MyFcmMessageListenerService
import com.digitaldukaan.R
import com.digitaldukaan.adapters.*
import com.digitaldukaan.constants.*
import com.digitaldukaan.exceptions.DeprecateAppVersionException
import com.digitaldukaan.exceptions.UnAuthorizedAccessException
import com.digitaldukaan.interfaces.IAdapterItemClickListener
import com.digitaldukaan.interfaces.IContactItemClicked
import com.digitaldukaan.interfaces.ISearchItemClicked
import com.digitaldukaan.models.dto.ContactModel
import com.digitaldukaan.models.request.PaymentLinkRequest
import com.digitaldukaan.models.request.UpdateInvitationRequest
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
import id.zelory.compressor.constraint.quality
import kotlinx.android.synthetic.main.activity_main2.*
import kotlinx.android.synthetic.main.bottom_sheet_custom_domain_selection.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.UnknownHostException
import java.util.*
import kotlin.collections.ArrayList


open class BaseFragment : ParentFragment(), ISearchItemClicked, LocationListener {

    protected var mContentView: View? = null
    protected var mActivity: MainActivity? = null
    protected var TAG: String = ""

    private var mProgressDialog: Dialog? = null
    private var mImageAdapter = ImagesSearchAdapter()
    private var mImagePickBottomSheet: BottomSheetDialog? = null
    private var mAppUpdateDialog: Dialog? = null
    private var mGoogleApiClient: FusedLocationProviderClient? = null
    private var mLastLocation: Location? = null
    private var mCurrentLatitude = 0.0
    private var mCurrentLongitude = 0.0
    private var mMultiUserAdapter: StaffInvitationAdapter? = null
    private var webConsoleBottomSheetDialog: BottomSheetDialog? = null

    companion object {
        private var sStaffInvitationDialog: Dialog? = null
        private var mCurrentPhotoPath = ""
        var sIsInvitationAvailable: Boolean = true
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mActivity = context as MainActivity
        Log.d(TAG, "onAttach :: called in Application")
    }

    protected fun showProgressDialog(context: Context?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            context?.let {
                try {
                    mProgressDialog = Dialog(it)
                    mProgressDialog?.apply {
                        val view = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null)
                        setContentView(view)
                        setCancelable(false)
                        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    }?.show()
                } catch (e: Exception) {
                    Log.e(TAG, "showProgressDialog: ${e.message}", e)
                    AppEventsManager.pushAppEvents(
                        eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                        isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                        data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID), "Exception Point" to "showProgressDialog", "Exception Message" to e.message, "Exception Logs" to e.toString())
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
                separator.visibility = if (isHidden) View.GONE else View.VISIBLE
            }
        }
    }

    open fun onClick(view: View?) = Unit

    open fun onBackPressed(): Boolean = false

    protected fun showCancellableProgressDialog(context: Context?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            context?.let {
                try {
                    mProgressDialog = Dialog(it)
                    val inflate = LayoutInflater.from(it).inflate(R.layout.progress_dialog, null)
                    mProgressDialog?.setContentView(inflate)
                    mProgressDialog?.setCancelable(true)
                    mProgressDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    mProgressDialog?.show()
                } catch (e: Exception) {
                    Log.e(TAG, "showCancellableProgressDialog: ${e.message}", e)
                }
            }
        }
    }

    fun stopProgress() {
        mActivity?.runOnUiThread {
            try {
                if (null != mProgressDialog) {
                    mProgressDialog?.dismiss()
                    mProgressDialog = null
                }
            } catch (e: Exception) {
                Log.e(TAG, "stopProgress: ${e.message}", e)
                AppEventsManager.pushAppEvents(
                    eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                    isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
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
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            stopProgress()
            when (e) {
                is IllegalStateException -> showToast("System Error :: IllegalStateException :: Unable to reach Server")
                is IOException -> Log.e(TAG, "$TAG exceptionHandlingForAPIResponse: ${e.message}", e)
                is UnknownHostException -> showToast(e.message)
                is UnAuthorizedAccessException -> logoutFromApplication()
                is DeprecateAppVersionException -> showVersionUpdateDialog()
                else -> showToast(mActivity?.getString(R.string.something_went_wrong))
            }
        }
    }

    protected fun showShortSnackBar(
        message: String? = "sample testing",
        showDrawable: Boolean = false,
        drawableID: Int = 0
    ) {
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
                            snackBarTextView.compoundDrawablePadding = resources.getDimensionPixelOffset(R.dimen._5sdp)
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
                        data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                            "Exception Point" to "showShortSnackBar",
                            "Exception Message" to e.message,
                            "Exception Logs" to e.toString()
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

    open fun TextView.showStrikeOffText() {
        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
    }


    open fun TextView.setMaxLength(length: Int) {
        filters = arrayOf<InputFilter>(LengthFilter(length))
    }

    open fun hideSoftKeyboard() {
        val imm = mActivity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mContentView?.windowToken, 0)
    }

    fun TextView.setHtmlData(string: String?) {
        string?.let { it ->
            this.text = Html.fromHtml(it, Html.FROM_HTML_MODE_COMPACT)
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
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "Exception Point" to "clearFragmentBackStack",
                    "Exception Message" to e.message,
                    "Exception Logs" to e.toString()
                )
            )
        }
    }

    open fun copyDataToClipboard(string: String?) {
        try {
            val clipboard = mActivity?.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
            val clip = ClipData.newPlainText(Constants.CLIPBOARD_LABEL, string)
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
                    val builder: AlertDialog.Builder = AlertDialog.Builder(it)
                    builder.apply {
                        setTitle(getString(R.string.no_internet_connection))
                        setMessage(getString(R.string.turn_on_internet_message))
                        setCancelable(false)
                        setNegativeButton(getString(R.string.close)) { dialog, _ ->
                            onNoInternetButtonClick(true)
                            dialog.dismiss()
                        }
                    }.create().show()
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

    open fun openUrlInBrowser(url: String?) {
        try {
            url?.let { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(it))) }
        } catch (e: Exception) {
            Log.e(TAG, "openUrlInBrowser: ${e.message}", e)
            AppEventsManager.pushAppEvents(
                eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                isCleverTapEvent = true, isAppFlyerEvent = true, isServerCallEvent = true,
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
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
                if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
        if (isEmpty(shareIntentList)) {
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
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
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
                if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) { ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), Constants.STORAGE_REQUEST_CODE)
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
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
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

    open fun startViewAnimation(view: View?, technique: Techniques = Techniques.Tada, duration: Long = 300) {
        view?.let { v -> YoYo.with(technique).duration(duration).playOn(v) }
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
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), Constants.IMAGE_PICK_REQUEST_CODE)
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
                    bottomSheetUploadImageCloseImageView.setOnClickListener { if (true == mImagePickBottomSheet?.isShowing) mImagePickBottomSheet?.dismiss() }
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
                            isCleverTapEvent = true,
                            isAppFlyerEvent = true,
                            isServerCallEvent = true,
                            data = mapOf(
                                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                AFInAppEventParameterName.BING_TEXT to searchImageEditText.text.trim().toString()
                            )
                        )
                        showProgressDialog(mActivity)
                        CoroutineScopeUtils().runTaskOnCoroutineBackground {
                            try {
                                val response = RetrofitApi().getServerCallObject()?.searchImagesFromBing(searchImageEditText.text.trim().toString(), getStringDataFromSharedPref(Constants.STORE_ID))
                                response?.let { res ->
                                    if (res.isSuccessful) {
                                        res.body()?.let {
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
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
            if (ActivityCompat.checkSelfPermission(it, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(it, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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
                mActivity?.let {
                    ActivityCompat.requestPermissions(it, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA), Constants.IMAGE_PICK_REQUEST_CODE)
                }
            }
        }
    }

    private var cameraGalleryWithCropIntentResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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

    private var cameraGalleryWithoutCropIntentResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
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
            file = Compressor.compress(this, file) { quality(if (false == StaticInstances.sPermissionHashMap?.get(Constants.PREMIUM_USER)) (mActivity?.resources?.getInteger(R.integer.premium_compression_value) ?: 80) else 100) }
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
            mActivity?.run { file = Compressor.compress(this, it) {
                quality(if (false == StaticInstances.sPermissionHashMap?.get(Constants.PREMIUM_USER)) (mActivity?.resources?.getInteger(R.integer.premium_compression_value) ?: 80) else 100)
            } }
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
        try {
            mActivity?.let {
                val originalImgFile = File(it.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${System.currentTimeMillis()}_originalImgFile.jpg")
                bitmap?.let { b -> convertBitmapToFile(originalImgFile, b) }
                val croppedImgFile = File(it.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "${System.currentTimeMillis()}_croppedImgFile.jpg")
                UCrop.of(Uri.fromFile(originalImgFile), Uri.fromFile(croppedImgFile))
                    .withAspectRatio(1f, 1f)
                    .withMaxResultSize(500, 500)
                    .start(it)
                stopProgress()
            }
        } catch (e: Exception) {
            Log.e(TAG, "startCropping: ${e.message}", e)
        }
    }

    private fun convertBitmapToFile(destinationFile: File, bitmap: Bitmap) {
        try {//create a file to write bitmap data
            destinationFile.createNewFile()
            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, bos)
            val bitmapData = bos.toByteArray()
            //write the bytes in file
            val fos = FileOutputStream(destinationFile)
            fos.write(bitmapData)
            fos.flush()
            fos.close()
        } catch (e: Exception) {
            Log.e(TAG, "convertBitmapToFile: ${e.message}", e)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult: ")
        if (resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "onActivityResult: OK")
            if (requestCode == UCrop.REQUEST_CROP) {
                Log.d(TAG, "onActivityResult: CROP_IMAGE_ACTIVITY_REQUEST_CODE ")
                data?.let {
                    val resultUri = UCrop.getOutput(data)
                    Log.d(
                        TAG,
                        "onActivityResult: CROP_IMAGE_ACTIVITY_REQUEST_CODE :: result uri :: $resultUri"
                    )
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
                            file?.let { f ->
                                Log.d(TAG, "ORIGINAL :: ${f.length() / (1024)} KB")
                                mActivity?.let { context ->
                                    file = Compressor.compress(context, f) {
                                        quality(if (false == StaticInstances.sPermissionHashMap?.get(Constants.PREMIUM_USER)) (mActivity?.resources?.getInteger(R.integer.premium_compression_value) ?: 80) else 100) } }
                                Log.d(TAG, "COMPRESSED :: ${f.length() / (1024)} KB")
                                if (f.length() / (1024 * 1024) >= mActivity?.resources?.getInteger(R.integer.image_mb_size) ?: 0) {
                                    showToast("Images more than ${mActivity?.resources?.getInteger(R.integer.image_mb_size)} are not allowed")
                                    return@runTaskOnCoroutineMain
                                }
                            }
                            stopProgress()
                            mImagePickBottomSheet?.dismiss()
                            val imageUri = it.getImageUri(mActivity)
                            imageUri?.let { startCropping(bitmap) }
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
                data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                    "Exception Point" to "onSearchImageItemClicked",
                    "Exception Message" to e.message,
                    "Exception Logs" to e.toString()
                )
            )
        }
    }

    protected fun updateNavigationBarState(actionId: Int) {
        mActivity?.let { activity ->
            activity.bottomNavigationView.background = null
            activity.premiumTextView.setTextColor(ContextCompat.getColor(activity, if (R.id.menuPremium == actionId) R.color.premium_text_color else R.color.default_text_light_grey))
            val menu: Menu = activity.bottomNavigationView.menu
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
                        launchFragment(OrderFragment.newInstance(), true)
                    } else launchFragment(StoreDescriptionFragment.newInstance(getHeaderByActionInSettingKetList(profilePreviewResponse, Constants.ACTION_STORE_DESCRIPTION), incompleteProfilePageNumber, false, profilePreviewResponse), true)
                }
                Constants.ACTION_BUSINESS -> {
                    if (currentFragment is BusinessTypeFragment) launchFragment(OrderFragment.newInstance(), true) else launchFragment(BusinessTypeFragment.newInstance(getHeaderByActionInSettingKetList(profilePreviewResponse, Constants.ACTION_BUSINESS_TYPE), incompleteProfilePageNumber, false, profilePreviewResponse), true)
                }
                Constants.ACTION_BANK -> launchFragment(BankAccountFragment.newInstance(getHeaderByActionInSettingKetList(profilePreviewResponse, Constants.ACTION_BANK_ACCOUNT), incompleteProfilePageNumber, false, profilePreviewResponse), true)
                else -> launchFragment(OrderFragment.newInstance(), true)
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
                            when (checkedId) {
                                orderIdRadioButton.id -> { searchInputLayout.hint = "${staticData?.heading_search_dialog} ${staticData?.search_dialog_selection_one}" }
                                phoneRadioButton.id -> { searchInputLayout.hint = "${staticData?.heading_search_dialog} ${staticData?.search_dialog_selection_two}" }
                            }
                            mobileNumberEditText.setText("")
                        }
                        orderIdRadioButton.isChecked = true
                        if (isNotEmpty(mobileNumberString) && (mActivity?.getCurrentFragment() is SearchOrdersFragment)) {
                            phoneRadioButton.isChecked = true
                            mobileNumberEditText.setText(mobileNumberString)
                        } else if (mActivity?.getCurrentFragment() is SearchOrdersFragment) {
                            orderIdRadioButton.isChecked = true
                            mobileNumberEditText.setText(orderIdStr)
                        }
                        confirmTextView.setOnClickListener {
                            var inputOrderId = ""
                            var inputMobileNumber = ""
                            staticData?.error_mandatory_field = "This field is mandatory"
                            if (orderIdRadioButton.isChecked) {
                                inputOrderId = mobileNumberEditText.text.trim().toString()
                                if (isEmpty(inputOrderId)) {
                                    mobileNumberEditText.error = staticData?.error_mandatory_field
                                    mobileNumberEditText.requestFocus()
                                    return@setOnClickListener
                                }
                            } else {
                                inputMobileNumber = mobileNumberEditText.text.trim().toString()
                                if (isEmpty(inputMobileNumber)) {
                                    mobileNumberEditText.error = staticData?.error_mandatory_field
                                    mobileNumberEditText.requestFocus()
                                    return@setOnClickListener
                                }
                            }
                            dismiss()
                            AppEventsManager.pushAppEvents(
                                eventName = AFInAppEventType.EVENT_SEARCH_CLICK,
                                isCleverTapEvent = true,
                                isAppFlyerEvent = true,
                                isServerCallEvent = true,
                                data = mapOf(
                                    AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                    AFInAppEventParameterName.SEARCH_BY to if (isEmpty(inputMobileNumber)) AFInAppEventParameterName.PHONE else AFInAppEventParameterName.ORDER_ID
                                )
                            )
                            onSearchDialogContinueButtonClicked(inputOrderId, inputMobileNumber)
                        }
                        mobileNumberEditText.addTextChangedListener(object : TextWatcher {
                            override fun afterTextChanged(p0: Editable?) {
                                val str = p0?.toString()
                                if (isNotEmpty(str)) errorTextView.visibility = View.GONE
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
                            val message = "No order found with this ${if (isEmpty(mobileNumberString)) "Order ID" else "Mobile Number"}"
                            errorTextView.text = message
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
            var isCheckBoxVisible = ("" == PrefsManager.getStringDataFromSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN))
            builder.apply {
                setTitle(staticData?.dialog_text_alert)
                val message: String? = when (item?.displayStatus) {
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
                    checkBox.setOnCheckedChangeListener { _, isChecked -> isCheckBoxVisible = isChecked }
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
                imageStr?.let { str ->
                    try {
                        Picasso.get().load(str).into(imageView)
                    } catch (e: Exception) {
                        Log.e("PICASSO", "picasso image loading issue: ${e.message}", e)
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_SERVER_EXCEPTION,
                            isCleverTapEvent = true,
                            isAppFlyerEvent = true,
                            isServerCallEvent = true,
                            data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
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
                    val view = LayoutInflater.from(mActivity).inflate(R.layout.bottom_sheet_add_products_catalog_builder, findViewById(R.id.bottomSheetContainer))
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
                            bannerImageView.let {
                                Picasso.get().load(addProductBannerStaticDataResponse?.image_url).into(it)
                            }
                            closeImageView.setOnClickListener { bottomSheetDialog.dismiss() }
                            buttonTextView.setOnClickListener {
                                bottomSheetDialog.dismiss()
                                AppEventsManager.pushAppEvents(
                                    eventName = AFInAppEventType.EVENT_CATALOG_BUILDER_TRY_NOW,
                                    isCleverTapEvent = true,
                                    isAppFlyerEvent = true,
                                    isServerCallEvent = true,
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
                    data = mapOf(AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                        "Exception Point" to "showMasterCatalogBottomSheet",
                        "Exception Message" to e.message,
                        "Exception Logs" to e.toString()
                    )
                )
            }
        }
    }

    protected fun openLocationSettings(isBackRequired: Boolean) {
        mActivity?.let { context ->
            AlertDialog.Builder(context).apply {
                setTitle("Permission")
                setMessage("Please allow Location permission")
                setPositiveButton(getString(R.string.txt_yes)) { dialogInterface, _ ->
                    dialogInterface?.dismiss()
                    if (isBackRequired) context.onBackPressed()
                    context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
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

    fun logoutFromApplication(isAppLogout: Boolean = false) {
        if (!isAppLogout) showToast(mActivity?.getString(R.string.logout_message))
        mActivity?.getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE)?.edit()?.clear()?.apply()
        storeStringDataInSharedPref(Constants.KEY_DONT_SHOW_MESSAGE_AGAIN, "")
        storeStringDataInSharedPref(Constants.USER_AUTH_TOKEN, "")
        storeStringDataInSharedPref(Constants.STORE_NAME, "")
        storeStringDataInSharedPref(Constants.USER_MOBILE_NUMBER, "")
        storeStringDataInSharedPref(Constants.STORE_ID, "")
        clearFragmentBackStack()
        launchFragment(LoginFragmentV2.newInstance(isAppLogout), true)
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
                                if (it.mIsSuccessStatus) shareOnWhatsApp(
                                    Gson().fromJson<String>(it.mCommonDataStr, String::class.java)
                                ) else showShortSnackBar(it.mMessage, true, R.drawable.ic_close_red)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

    private fun requestFeaturePermissionServerCall(id: Int) {
        showProgressDialog(mActivity)
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val response = RetrofitApi().getServerCallObject()?.getRequestPermissionText(id)
                response?.let {
                    stopProgress()
                    if (it.isSuccessful) {
                        it.body()?.let {
                            withContext(Dispatchers.Main) {
                                if (it.mIsSuccessStatus) shareDataOnWhatsAppByNumber(Gson().fromJson<String>(it.mCommonDataStr, String::class.java), StaticInstances.sMerchantMobileNumber) else showShortSnackBar(it.mMessage, true, R.drawable.ic_close_red)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

    fun getLockedStoreShareDataServerCall(mode: Int) {
        showCancellableProgressDialog(mActivity)
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val response = RetrofitApi().getServerCallObject()?.getLockedStoreShareData(mode)
                response?.let {
                    stopProgress()
                    if (it.isSuccessful) {
                        it.body()?.let {
                            withContext(Dispatchers.Main) {
                                if (it.mIsSuccessStatus) {
                                    val lockedShareResponse = Gson().fromJson<LockedStoreShareResponse>(it.mCommonDataStr, LockedStoreShareResponse::class.java)
                                    onLockedStoreShareSuccessResponse(lockedShareResponse)
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
            val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_transaction_detail, findViewById(R.id.bottomSheetContainer))
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
                            when (response.ctaItem?.action) {
                                Constants.ACTION_ADD_BANK -> {
                                    bottomSheetDialog.dismiss()
                                    launchFragment(BankAccountFragment.newInstance(null, 0, false, null), false) }
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
                    if (isNotEmpty(response?.paymentImage)) mActivity?.let { context -> Glide.with(context).load(response?.paymentImage).into(paymentModeImageView) }
                    if (isNotEmpty(response?.settlementCdn)) mActivity?.let { context -> Glide.with(context).load(response?.settlementCdn).into(imageViewBottom) }
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
                        adapter = OrderNotificationsAdapter(
                            mActivity,
                            response?.orderNotificationList,
                            object : IAdapterItemClickListener {
                                override fun onAdapterItemClickListener(position: Int) {
                                    val list = response?.orderNotificationList
                                    val item = list?.get(position)
                                    AppEventsManager.pushAppEvents(
                                        eventName = AFInAppEventType.EVENT_NEW_ORDER_NOTIFICATION_SELECTION,
                                        isCleverTapEvent = true,
                                        isAppFlyerEvent = true,
                                        isServerCallEvent = true,
                                        data = mapOf(
                                            AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                            AFInAppEventParameterName.SELECTION to item?.eventName
                                        )
                                    )
                                    if (true != item?.isSelected) {
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
                val mContactPickerBottomSheet: BottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_contact_pick, it.findViewById(R.id.bottomSheetContainer))
                mContactPickerBottomSheet.apply {
                    val staticText = StaticInstances.sOrderPageInfoStaticData
                    setContentView(view)
                    setBottomSheetCommonProperty()
                    val contactList: ArrayList<ContactModel> = ArrayList()
                    val contactAdapter =
                        ContactAdapter(contactList, mActivity, object : IContactItemClicked {
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
                                if (isNotEmpty(string)) {
                                    val updatedContactList: ArrayList<ContactModel> = ArrayList()
                                    contactList.forEachIndexed { _, contactModel -> if (contactModel.name?.toLowerCase(Locale.getDefault())?.contains(string?.toLowerCase(Locale.getDefault()) ?: "") == true || contactModel.number?.contains(string ?: "") == true) { updatedContactList.add(contactModel) } }
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
                }.show()
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
                                    if (Constants.MODE_WHATS_APP == request.mode) {
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
                floatingClose.setOnClickListener { this.dismiss() }
                window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }.show()
        }
    }

    private fun checkLocationPermission(): Boolean {
        mActivity?.let {
            if (ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(it, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            mActivity?.let { context ->
                mGoogleApiClient?.lastLocation?.addOnCompleteListener(context) { task ->
                    if (task.isSuccessful && null != task.result) {
                        mLastLocation = task.result
                        mCurrentLatitude = mLastLocation?.latitude ?: 0.0
                        mCurrentLongitude = mLastLocation?.longitude ?: 0.0
                        onLocationChanged(mCurrentLatitude, mCurrentLongitude)
                    } else {
                        if (!isLocationEnabledInSettings(mActivity)) openLocationSettings(true)
                        mCurrentLatitude = 0.0
                        mCurrentLongitude =  0.0
                        onLocationChanged(mCurrentLatitude, mCurrentLongitude)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "getLocationFromGoogleMap: ${e.message}", e)
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

    open fun RecyclerView.smoothSnapToPosition(position: Int, snapMode: Int = LinearSmoothScroller.SNAP_TO_START) {
        val smoothScroller = object : LinearSmoothScroller(this.context) {
            override fun getVerticalSnapPreference(): Int = snapMode
            override fun getHorizontalSnapPreference(): Int = snapMode
        }
        smoothScroller.targetPosition = position
        layoutManager?.startSmoothScroll(smoothScroller)
    }

    open fun openSubscriptionLockedUrlInBrowser(url: String) = openWebViewFragment(this@BaseFragment, "", url)

    open fun showShipmentConfirmationBottomSheet(mOrderDetailStaticData: OrderDetailsStaticTextResponse?, orderId: Int?) {
        mActivity?.let { context ->
            val bottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(context).inflate(
                R.layout.bottom_sheet_shipment_confirmation,
                context.findViewById(R.id.bottomSheetContainer)
            )
            bottomSheetDialog.apply {
                setContentView(view)
                val bottomSheetClose: View = view.findViewById(R.id.bottomSheetClose)
                val headingTextView: TextView = view.findViewById(R.id.headingTextView)
                val ctaTextView: TextView = view.findViewById(R.id.ctaTextView)
                val radioButtonDeliveryPartnerSubHeading: TextView = view.findViewById(R.id.radioButtonDeliveryPartnerSubHeading)
                val radioButtonShipMyselfSubHeading: TextView = view.findViewById(R.id.radioButtonShipMyselfSubHeading)
                val radioButtonDeliveryPartner: RadioButton = view.findViewById(R.id.radioButtonDeliveryPartner)
                val radioButtonShipMyself: RadioButton = view.findViewById(R.id.radioButtonShipMyself)
                bottomSheetClose.setOnClickListener { bottomSheetDialog.dismiss() }
                headingTextView.text = mOrderDetailStaticData?.bottom_sheet_heading_how_will_you_ship
                radioButtonShipMyselfSubHeading.text = mOrderDetailStaticData?.bottom_sheet_sub_message2_select_this
                radioButtonDeliveryPartnerSubHeading.text = mOrderDetailStaticData?.bottom_sheet_sub_message1_select_this
                radioButtonDeliveryPartner.apply {
                    text = mOrderDetailStaticData?.bottom_sheet_message1_ship_using_partners
                    isChecked = true
                    setOnClickListener { radioButtonShipMyself.isChecked = false }
                }
                radioButtonShipMyself.apply {
                    text = mOrderDetailStaticData?.bottom_sheet_message2_i_will_ship
                    setOnClickListener { radioButtonDeliveryPartner.isChecked = false }
                }
                radioButtonDeliveryPartnerSubHeading.setOnClickListener {
                    radioButtonDeliveryPartner.isChecked = true
                    radioButtonShipMyself.isChecked = false
                }
                radioButtonShipMyselfSubHeading.setOnClickListener {
                    radioButtonShipMyself.isChecked = true
                    radioButtonDeliveryPartner.isChecked = false
                }
                ctaTextView.apply {
                    text = mOrderDetailStaticData?.text_next
                    setOnClickListener {
                        AppEventsManager.pushAppEvents(
                            eventName = AFInAppEventType.EVENT_DELIVERY_SHIPPING_MODE,
                            isCleverTapEvent = true,
                            isAppFlyerEvent = true,
                            isServerCallEvent = true,
                            data = mapOf(
                                AFInAppEventParameterName.STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
                                AFInAppEventParameterName.ORDER_ID to "$orderId"
                            )
                        )
                        bottomSheetDialog.dismiss()
                        onShipmentCtaClicked(radioButtonDeliveryPartner.isChecked)
                    }
                }
            }.show()
        }
    }

    open fun onCommonWebViewShouldOverrideUrlLoading(url: String, view: WebView) = when {
        url.startsWith("tel:") -> {
            try {
                view.context?.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(url)))
            } catch (e: Exception) {
                Log.e(TAG, "shouldOverrideUrlLoading :: tel :: ${e.message}", e)
            }
            true
        }
        url.contains("mailto:") -> {
            try {
                view.context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                Log.e(TAG, "shouldOverrideUrlLoading :: mailto :: ${e.message}", e)
            }
            true
        }
        url.contains("whatsapp:") -> {
            try {
                view.context?.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            } catch (e: Exception) {
                Log.e(TAG, "shouldOverrideUrlLoading :: whatsapp :: ${e.message}", e)
            }
            true
        }
        else -> {
            view.loadUrl(url)
            true
        }
    }

    open fun showLockedStoreShareBottomSheet(lockedShareResponse: LockedStoreShareResponse) {
        mActivity?.run {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_locked_store_share, findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
                setContentView(view)
                view.run {
                    val domainMessageTextView: TextView = findViewById(R.id.domainMessageTextView)
                    val headingTextView: TextView = findViewById(R.id.headingTextView)
                    val subHeadingTextView: TextView = findViewById(R.id.subHeadingTextView)
                    val storeDomainTextView: TextView = findViewById(R.id.storeDomainTextView)
                    val progressBarContainer: View = findViewById(R.id.progressBarContainer)
                    val domainListContainer: View = findViewById(R.id.domainListContainer)
                    val offerMessageTextView: TextView = findViewById(R.id.offerMessageTextView)
                    val domainTextView: TextView = findViewById(R.id.domainTextView)
                    val buyNowTextView: TextView = findViewById(R.id.buyNowTextView)
                    val promoCodeTextView: TextView = findViewById(R.id.promoCodeTextView)
                    val originalPriceTextView: TextView = findViewById(R.id.originalPriceTextView)
                    val messageTextView: TextView = findViewById(R.id.messageTextView)
                    val priceTextView: TextView = findViewById(R.id.priceTextView)
                    headingTextView.text = lockedShareResponse.heading
                    subHeadingTextView.text = lockedShareResponse.subHeading
                    storeDomainTextView.text = lockedShareResponse.storeDomain
                    domainMessageTextView.text = lockedShareResponse.message
                    progressBarContainer.visibility = View.VISIBLE
                    CoroutineScopeUtils().runTaskOnCoroutineBackground {
                        try {
                            val response = RetrofitApi().getServerCallObject()?.getCustomDomainBottomSheetData()
                            response?.let {
                                stopProgress()
                                if (it.isSuccessful) {
                                    it.body()?.let {
                                        withContext(Dispatchers.Main) {
                                            if (it.mIsSuccessStatus) {
                                                val customDomainBottomSheetResponse = Gson().fromJson<CustomDomainBottomSheetResponse>(it.mCommonDataStr, CustomDomainBottomSheetResponse::class.java)
                                                progressBarContainer.visibility = View.GONE
                                                domainListContainer.visibility = View.VISIBLE
                                                domainMessageTextView.text = lockedShareResponse.message
                                                val item = customDomainBottomSheetResponse?.primaryDomain
                                                offerMessageTextView.text = customDomainBottomSheetResponse?.staticText?.text_best_pick_for_you
                                                domainTextView.text = item?.domainName
                                                promoCodeTextView.text = item?.promo
                                                val messageStr = "${item?.infoData?.firstYearText}\n${item?.infoData?.renewsText}"
                                                messageTextView.text = messageStr
                                                buyNowTextView.apply {
                                                    text = item?.cta?.text
                                                    setTextColor(Color.parseColor(item?.cta?.textColor))
                                                    setOnClickListener {
                                                        bottomSheetDialog.dismiss()
                                                        if (Constants.NEW_RELEASE_TYPE_WEBVIEW == item?.cta?.action) {
                                                            val url = "${BuildConfig.WEB_VIEW_URL}${item.cta?.pageUrl}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&domain_name=${item.domainName}&purchase_price=${item.originalPrice}&renewal_price=${item.renewalPrice}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.LANDING_PAGE}"
                                                            openWebViewFragmentV3(this@BaseFragment, "", url)
                                                        }
                                                    }
                                                }
                                                var price = "₹${item?.originalPrice}"
                                                originalPriceTextView.apply {
                                                    text = price
                                                    paintFlags = (priceTextView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG)
                                                }
                                                price = "₹${item?.discountedPrice}"
                                                priceTextView.text = price
                                                mActivity?.let { context ->
                                                    promoCodeTextView.setTextColor(ContextCompat.getColor(context, R.color.open_green))
                                                    offerMessageTextView.setBackgroundColor(ContextCompat.getColor(context, R.color.purple_best_offer_bg))
                                                }
                                                val searchTextView: TextView = findViewById(R.id.searchTextView)
                                                val moreSuggestionsTextView: TextView = findViewById(R.id.moreSuggestionsTextView)
                                                val searchMessageTextView: TextView = findViewById(R.id.searchMessageTextView)
                                                val suggestedDomainRecyclerView = findViewById<RecyclerView>(R.id.suggestedDomainRecyclerView)
                                                searchMessageTextView.text = customDomainBottomSheetResponse?.staticText?.text_cant_find
                                                moreSuggestionsTextView.text = customDomainBottomSheetResponse?.staticText?.text_more_suggestions
                                                searchTextView.apply {
                                                    text = customDomainBottomSheetResponse?.staticText?.text_search
                                                    setOnClickListener {
                                                        bottomSheetDialog.dismiss()
                                                        if (Constants.NEW_RELEASE_TYPE_WEBVIEW == customDomainBottomSheetResponse.searchCta?.action) {
                                                            val url = "${BuildConfig.WEB_VIEW_URL}${customDomainBottomSheetResponse.searchCta?.pageUrl}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.LANDING_PAGE}"
                                                            openWebViewFragmentV3(this@BaseFragment, "", url)
                                                        }
                                                    }
                                                }
                                                suggestedDomainRecyclerView.apply {
                                                    layoutManager = LinearLayoutManager(mActivity)
                                                    adapter = CustomDomainSelectionAdapter(
                                                        customDomainBottomSheetResponse.suggestedDomainsList,
                                                        object : IAdapterItemClickListener {

                                                            override fun onAdapterItemClickListener(position: Int) {
                                                                bottomSheetDialog.dismiss()
                                                                val domainItemResponse = customDomainBottomSheetResponse.suggestedDomainsList?.get(position)
                                                                if (Constants.NEW_RELEASE_TYPE_WEBVIEW == domainItemResponse?.cta?.action) {
                                                                    val url = "${BuildConfig.WEB_VIEW_URL}${domainItemResponse.cta?.pageUrl}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&domain_name=${domainItemResponse.domainName}&purchase_price=${domainItemResponse.originalPrice}&renewal_price=${domainItemResponse.renewalPrice}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.LANDING_PAGE}"
                                                                    openWebViewFragmentV3(this@BaseFragment, "", url)
                                                                }
                                                            }
                                                        })
                                                }
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
            }.show()
        }
    }

    fun showStaffInvitationDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            Log.d(TAG, "showStaffInvitationDialog: called")
            if (null != sStaffInvitationDialog && true == sStaffInvitationDialog?.isShowing) return@runTaskOnCoroutineMain
            if (null == StaticInstances.sStaffInvitation) return@runTaskOnCoroutineMain
            mActivity?.let { context ->
                sStaffInvitationDialog = Dialog(context)
                val view = LayoutInflater.from(context).inflate(R.layout.multi_user_selection_dialog, null)
                var selectedId = 1
                sStaffInvitationDialog?.apply {
                    setContentView(view)
                    setCancelable(false)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    view?.run {
                        val dialogImageView: ImageView = findViewById(R.id.dialogImageView)
                        val moreOptionsContainer: View = findViewById(R.id.moreOptionsContainer)
                        val dialogOptionsRecyclerView: RecyclerView = findViewById(R.id.dialogOptionsRecyclerView)
                        val nextTextView: TextView = findViewById(R.id.nextTextView)
                        val moreOptionsTextView: TextView = findViewById(R.id.moreOptionsTextView)
                        val dialogHeadingTextView: TextView = findViewById(R.id.dialogHeadingTextView)
                        val staffInvitation = StaticInstances.sStaffInvitation
                        mActivity?.let { context ->
                            Glide.with(context).load(staffInvitation?.cdn).into(dialogImageView)
                        }
                        dialogHeadingTextView.text = staffInvitation?.heading
                        moreOptionsTextView.setHtmlData(staffInvitation?.textMoreOptions)
                        if (true == staffInvitation?.invitationList?.isNotEmpty()) {
                            staffInvitation.invitationList[0]?.isSelected = true
                            mMultiUserAdapter = StaffInvitationAdapter(staffInvitation.invitationList, object : IAdapterItemClickListener {
                                override fun onAdapterItemClickListener(position: Int) {
                                    staffInvitation.invitationList.forEachIndexed { _, item -> item?.isSelected = false }
                                    staffInvitation.invitationList[position]?.isSelected = true
                                    selectedId = when (staffInvitation.invitationList[position]?.id) {
                                        Constants.STAFF_INVITATION_CODE_EXIT -> {
                                            staffInvitation.invitationList[position]?.id ?: Constants.STAFF_INVITATION_CODE_EXIT
                                        }
                                        Constants.STAFF_INVITATION_CODE_REJECT -> {
                                            staffInvitation.invitationList[position]?.id ?: Constants.STAFF_INVITATION_CODE_REJECT
                                        }
                                        else -> {
                                            staffInvitation.invitationList[position]?.id ?: Constants.STAFF_INVITATION_CODE_ACCEPT
                                        }
                                    }
                                    mMultiUserAdapter?.notifyDataSetChanged()
                                }
                            })
                        } else return@runTaskOnCoroutineMain
                        moreOptionsContainer.setOnClickListener {
                            moreOptionsContainer.visibility = View.GONE
                            mMultiUserAdapter?.showCompleteList()
                        }
                        dialogOptionsRecyclerView.apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = mMultiUserAdapter
                        }
                        nextTextView.apply {
                            text = staffInvitation?.cta?.text
                            setOnClickListener {
                                CoroutineScopeUtils().runTaskOnCoroutineBackground {
                                    try {
                                        val response = RetrofitApi().getServerCallObject()?.updateInvitationStatus(UpdateInvitationRequest(status = selectedId, StoreId = staffInvitation?.invitedStoreId ?: 0, userId = getStringDataFromSharedPref(Constants.USER_ID).toInt(), languageId = 1))
                                        response?.let {
                                            stopProgress()
                                            if (it.isSuccessful) {
                                                val updateInvitationResponse = Gson().fromJson<CheckStaffInviteResponse>(it.body()?.mCommonDataStr, CheckStaffInviteResponse::class.java)
                                                it.body()?.let {
                                                    withContext(Dispatchers.Main) {
                                                        if (it.mIsSuccessStatus) {
                                                            sStaffInvitationDialog?.dismiss()
                                                            showShortSnackBar(it.mMessage, true, R.drawable.ic_check_circle)
                                                            when (selectedId) {
                                                                Constants.STAFF_INVITATION_CODE_ACCEPT -> {
                                                                    sIsInvitationAvailable = updateInvitationResponse.mIsInvitationAvailable
                                                                    StaticInstances.sPermissionHashMap = null
                                                                    StaticInstances.sPermissionHashMap = updateInvitationResponse.permissionsMap
                                                                    storeStringDataInSharedPref(Constants.STORE_ID, updateInvitationResponse.storeId)
                                                                    StaticInstances.sPermissionHashMap?.let { map -> launchScreenFromPermissionMap(map) }
                                                                }
                                                                Constants.STAFF_INVITATION_CODE_EXIT -> logoutFromApplication(isAppLogout = true)
                                                                else -> checkStaffInvite()
                                                            }
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
                        }
                    }
                }?.show()
            }
        }
    }

    fun checkStaffInvite() {
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val checkStaffInviteResponse = RetrofitApi().getServerCallObject()?.checkStaffInvite()
                checkStaffInviteResponse?.let { it ->
                    val staffInviteResponse = Gson().fromJson<CheckStaffInviteResponse>(it.body()?.mCommonDataStr, CheckStaffInviteResponse::class.java)
                    Log.d(TAG, "sIsInvitationAvailable :: staffInviteResponse?.mIsInvitationAvailable ${staffInviteResponse?.mIsInvitationAvailable}")
                    Log.d(TAG, "checkStaffInvite: response set sIsInvitationAvailable :: ${staffInviteResponse?.mIsInvitationAvailable ?: false} ")
                    sIsInvitationAvailable = staffInviteResponse?.mIsInvitationAvailable ?: false
                    StaticInstances.sStaffInvitation = staffInviteResponse?.mStaffInvitation
                    onCheckStaffInviteResponse()
                }
            } catch (e: Exception) {
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

    fun launchScreenFromPermissionMap(permissionMap: HashMap<String, Boolean>) {
        clearFragmentBackStack()
        Log.d(TAG, "$permissionMap")
        Log.d(TAG, "sIsInvitationAvailable :: $sIsInvitationAvailable")
        when {
            true == permissionMap[Constants.PAGE_ORDER] -> {
                launchFragment(OrderFragment.newInstance(isClearOrderPageResponse = true), true)
            }
            true == permissionMap[Constants.PAGE_CATALOG] -> {
                launchFragment(ProductFragment.newInstance(), true)
            }
            true == permissionMap[Constants.PAGE_PREMIUM] -> {
                launchFragment(PremiumPageInfoFragment.newInstance(), true)
            }
            true == permissionMap[Constants.PAGE_MARKETING] -> {
                launchFragment(MarketingFragment.newInstance(), true)
            }
            true == permissionMap[Constants.PAGE_SETTINGS] -> {
                launchFragment(SettingsFragment.newInstance(), true)
            }
        }
        mActivity?.checkBottomNavBarFeatureVisibility()
        Log.d(TAG, " sIsInvitationAvailable :: launchScreenFromPermissionMap: called")
        if (sIsInvitationAvailable) showStaffInvitationDialog()
    }

    fun showStaffFeatureLockedBottomSheet(id: Int) {
        mActivity?.run {
            val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
            val view = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_staff_feature_locked, findViewById(R.id.bottomSheetContainer))
            bottomSheetDialog.apply {
                setContentView(view)
                setBottomSheetCommonProperty()
                view.run {
                    val headingTextView: TextView = findViewById(R.id.headingTextView)
                    val ctaImageView: ImageView = findViewById(R.id.ctaImageView)
                    val ctaTextView: TextView = findViewById(R.id.ctaTextView)
                    val ctaContainer: View = findViewById(R.id.ctaContainer)
                    headingTextView.text = StaticInstances.sStaticData?.mStaffLockBottomSheet?.heading
                    ctaContainer.setOnClickListener {
                        requestFeaturePermissionServerCall(id)
                        bottomSheetDialog.dismiss()
                    }
                    ctaTextView.apply {
                        text = StaticInstances.sStaticData?.mStaffLockBottomSheet?.cta?.text
                        setTextColor(Color.parseColor(StaticInstances.sStaticData?.mStaffLockBottomSheet?.cta?.textColor))
                        mActivity?.let { context ->
                            Glide.with(context).load(StaticInstances.sStaticData?.mStaffLockBottomSheet?.cta?.cdn).into(ctaImageView)
                        }
                    }
                }
            }.show()
        }
    }

    open fun showVersionUpdateDialog() {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (true == mAppUpdateDialog?.isShowing) return@runTaskOnCoroutineMain
            mActivity?.let {
                mAppUpdateDialog = Dialog(it)
                val view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_app_update, null)
                mAppUpdateDialog?.apply {
                    setContentView(view)
                    setCancelable(false)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    view?.run {
                        val updateTextView: TextView = findViewById(R.id.updateTextView)
                        updateTextView.setOnClickListener {
                            (this@apply).dismiss()
                            openPlayStore()
                        }
                    }
                }?.show()
            }
        }
    }

    override fun onProviderEnabled(provider: String) = Unit

    override fun onProviderDisabled(provider: String) = Unit

    fun openDomainPurchaseBottomSheetServerCall() {
        showProgressDialog(mActivity)
        CoroutineScopeUtils().runTaskOnCoroutineBackground {
            try {
                val customDomainBottomSheetResponse = RetrofitApi().getServerCallObject()?.getCustomDomainBottomSheetData()
                customDomainBottomSheetResponse?.body()?.let { body ->
                    stopProgress()
                    val bottomSheetResponse = Gson().fromJson<CustomDomainBottomSheetResponse>(body.mCommonDataStr, CustomDomainBottomSheetResponse::class.java)
                    showDomainPurchasedBottomSheet(bottomSheetResponse, isNoDomainFoundLayout = true)
                }
            } catch (e: Exception) {
                exceptionHandlingForAPIResponse(e)
            }
        }
    }

    fun showDomainPurchasedBottomSheet(customDomainBottomSheetResponse: CustomDomainBottomSheetResponse, isNoDomainFoundLayout: Boolean = false, hideTopView: Boolean = false) {
        mActivity?.runOnUiThread {
            mActivity?.let {
                val bottomSheetDialog = BottomSheetDialog(it, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(it).inflate(R.layout.bottom_sheet_custom_domain_selection, it.findViewById(R.id.bottomSheetContainer))
                bottomSheetDialog.apply {
                    setContentView(view)
                    view?.run {
                        customDomainBottomSheetResponse.staticText?.let { staticText ->
                            val domainPurchasedGroup: View = findViewById(R.id.domainPurchasedGroup)
                            val noDomainFoundGroup: View = findViewById(R.id.noDomainFoundGroup)
                            val headingTextView2: TextView = findViewById(R.id.headingTextView)
                            if (isNoDomainFoundLayout) {
                                noDomainFoundGroup.visibility = View.VISIBLE
                                domainPurchasedGroup.visibility = View.GONE
                                val bottomSheetUpperContainerTextView: TextView = findViewById(R.id.bottomSheetUpperContainerTextView)
                                val noDomainFoundTextView: TextView = findViewById(R.id.noDomainFoundTextView)
                                val noDomainFoundMessageTextView: TextView = findViewById(R.id.noDomainFoundMessageTextView)
                                val bottomSheetUpperContainerRecyclerView: RecyclerView = findViewById(R.id.bottomSheetUpperContainerRecyclerView)
                                bottomSheetUpperContainerTextView.text = staticText.text_get_professional_email
                                noDomainFoundTextView.text = staticText.text_workspace_heading
                                noDomainFoundMessageTextView.text = staticText.text_workspace_subheading
                                bottomSheetUpperContainerRecyclerView.apply {
                                    layoutManager = LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false)
                                    adapter  = GoogleWorkspaceAppsAdapter(mActivity, customDomainBottomSheetResponse.workspaceCdnList)
                                }
                            } else {
                                noDomainFoundGroup.visibility = View.GONE
                                domainPurchasedGroup.visibility = View.VISIBLE
                            }
                            val searchTextView: TextView = findViewById(R.id.searchTextView)
                            val subHeadingTextView: TextView = findViewById(R.id.subHeadingTextView)
                            val searchMessageTextView: TextView = findViewById(R.id.searchMessageTextView)
                            val moreSuggestionsTextView: TextView = findViewById(R.id.moreSuggestionsTextView)
                            subHeadingTextView.text = staticText.subheading_budiness_needs_domain
                            headingTextView2.text = staticText.heading_last_step
                            if (hideTopView) headingTextView2.text = null
                            moreSuggestionsTextView.text = staticText.text_more_suggestions
                            searchMessageTextView.text = staticText.text_cant_find
                            searchTextView.text = staticText.text_search
                            searchTextView.setOnClickListener {
                                bottomSheetDialog.dismiss()
                                if (Constants.NEW_RELEASE_TYPE_WEBVIEW == customDomainBottomSheetResponse.searchCta?.action) {
                                    val url = BuildConfig.WEB_VIEW_URL + "${customDomainBottomSheetResponse.searchCta?.pageUrl}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.ON_BOARDING}"
                                    openWebViewFragmentV3(this@BaseFragment, "", url)
                                }
                            }
                        }
                        customDomainBottomSheetResponse.primaryDomain?.let { primaryDomain ->
                            val premiumHeadingTextView: TextView = findViewById(R.id.premiumHeadingTextView)
                            val domainTextView: TextView = findViewById(R.id.domainTextView)
                            val priceTextView: TextView = findViewById(R.id.priceTextView)
                            val promoCodeTextView: TextView = findViewById(R.id.promoCodeTextView)
                            val messageTextView: TextView = findViewById(R.id.messageTextView)
                            val message2TextView: TextView = findViewById(R.id.message2TextView)
                            val originalPriceTextView: TextView = findViewById(R.id.originalPriceTextView)
                            val buyNowTextView: TextView = findViewById(R.id.buyNowTextView)
                            premiumHeadingTextView.text = primaryDomain.heading
                            domainTextView.text = primaryDomain.domainName
                            promoCodeTextView.text = primaryDomain.promo
                            var amount = "₹${primaryDomain.discountedPrice}"
                            priceTextView.text = amount
                            amount = "₹${primaryDomain.originalPrice}"
                            originalPriceTextView.text = amount
                            originalPriceTextView.showStrikeOffText()
                            buyNowTextView.apply {
                                text = primaryDomain.cta?.text
                                setTextColor(Color.parseColor(primaryDomain.cta?.textColor))
                                setBackgroundColor(Color.parseColor(primaryDomain.cta?.textBg))
                                setOnClickListener {
                                    bottomSheetDialog.dismiss()
                                    if (Constants.NEW_RELEASE_TYPE_WEBVIEW == primaryDomain.cta?.action) {
                                        val url = BuildConfig.WEB_VIEW_URL + "${primaryDomain.cta?.pageUrl}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&domain_name=${primaryDomain.domainName}&purchase_price=${primaryDomain.originalPrice}&renewal_price=${primaryDomain.renewalPrice}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.ON_BOARDING}"
                                        openWebViewFragmentV3(this@BaseFragment, "", url)
                                    }
                                }
                            }
                            messageTextView.text = primaryDomain.infoData?.firstYearText?.trim()
                            message2TextView.text = primaryDomain.infoData?.renewsText?.trim()
                        }
                        val suggestedDomainRecyclerView = findViewById<RecyclerView>(R.id.suggestedDomainRecyclerView)
                        suggestedDomainRecyclerView.apply {
                            layoutManager = LinearLayoutManager(mActivity)
                            adapter = CustomDomainSelectionAdapter(
                                customDomainBottomSheetResponse.suggestedDomainsList,
                                object : IAdapterItemClickListener {

                                    override fun onAdapterItemClickListener(position: Int) {
                                        bottomSheetDialog.dismiss()
                                        val item = customDomainBottomSheetResponse.suggestedDomainsList?.get(position)
                                        if (Constants.NEW_RELEASE_TYPE_WEBVIEW == item?.cta?.action) {
                                            val url = BuildConfig.WEB_VIEW_URL + "${item.cta?.pageUrl}?storeid=${getStringDataFromSharedPref(Constants.STORE_ID)}&token=${getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}&domain_name=${item.domainName}&purchase_price=${item.originalPrice}&renewal_price=${item.renewalPrice}&${AFInAppEventParameterName.CHANNEL}=${AFInAppEventParameterName.ON_BOARDING}"
                                            openWebViewFragment(this@BaseFragment, "", url)
                                        }
                                    }
                                })
                        }
                    }
                }.show()
            }
        }
    }

    open fun openWebConsoleBottomSheet(webConsoleBottomSheet: WebConsoleBottomSheetResponse?) {
        CoroutineScopeUtils().runTaskOnCoroutineMain {
            if (null != webConsoleBottomSheetDialog && true == webConsoleBottomSheetDialog?.isShowing) return@runTaskOnCoroutineMain
            mActivity?.let { context ->
                webConsoleBottomSheetDialog = BottomSheetDialog(context, R.style.BottomSheetDialogTheme)
                val view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_web_console, context.findViewById(R.id.bottomSheetContainer))
                webConsoleBottomSheetDialog?.apply {
                    setContentView(view)
                    behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    view?.run {
                        val bottomSheetUrlTextView: TextView = findViewById(R.id.bottomSheetUrlTextView)
                        val bottomSheetLaptop: ImageView = findViewById(R.id.bottomSheetLaptop)
                        val bottomSheetBrowserImageView: ImageView = findViewById(R.id.bottomSheetBrowserImageView)
                        val bottomSheetBrowserText: TextView = findViewById(R.id.bottomSheetBrowserText)
                        val bottomSheetHeadingTextView: TextView = findViewById(R.id.bottomSheetHeadingTextView)
                        val bottomSheetSubHeadingTextView: TextView = findViewById(R.id.bottomSheetSubHeadingTextView)
                        webConsoleBottomSheet?.let { response ->
                            with(response) {
                                bottomSheetHeadingTextView.text = heading
                                bottomSheetSubHeadingTextView.text = subHeading
                                bottomSheetBrowserText.text = textBestViewedOn
                                Glide.with(context).load(primaryCdn).into(bottomSheetLaptop)
                                Glide.with(context).load(secondaryCdn).into(bottomSheetBrowserImageView)
                                bottomSheetUrlTextView.apply {
                                    setBackgroundColor(Color.parseColor(cta.bgColor))
                                    setTextColor(Color.parseColor(cta.textColor))
                                    setHtmlData(cta.text)
                                    setOnClickListener { copyDataToClipboard(Constants.DOTPE_OFFICIAL_URL_CLIPBOARD) }
                                }
                            }
                        }
                    }
                }?.show()
            }
        }
    }

}