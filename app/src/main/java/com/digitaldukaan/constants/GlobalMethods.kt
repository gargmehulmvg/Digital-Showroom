package com.digitaldukaan.constants

import android.app.Activity
import android.app.DownloadManager
import android.content.ContentValues
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Base64
import android.util.Base64OutputStream
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.View.MeasureSpec
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.MainActivity
import com.digitaldukaan.R
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.fragments.CommonWebViewFragment
import com.digitaldukaan.models.dto.ContactModel
import com.digitaldukaan.models.response.ProfileInfoResponse
import com.digitaldukaan.models.response.ProfilePreviewSettingsKeyResponse
import com.skydoves.balloon.Balloon
import com.skydoves.balloon.BalloonAnimation
import com.skydoves.balloon.createBalloon
import io.sentry.Sentry
import org.shadow.apache.commons.lang3.StringUtils
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLConnection
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


fun getBitmapFromURL(src: String?): Bitmap? {
    return try {
        BitmapFactory.decodeStream(URL(src).openConnection().getInputStream())
    } catch (e: Exception) {
        null
    }
}
fun getBitmapFromUri(uri: Uri?, context: Context?): Bitmap? {
    if (uri == null || context == null) return null
    return try {
        val imageStream = context.contentResolver.openInputStream(uri)
        return BitmapFactory.decodeStream(imageStream);
    } catch (e: Exception) {
        null
    }
}

fun isLocationEnabledInSettings(context: Context?): Boolean {
    if (context == null) return false
    var locationMode = 0
    val locationProviders: String
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        locationMode = try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            return false
        }
        locationMode != Settings.Secure.LOCATION_MODE_OFF
    } else {
        locationProviders = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.LOCATION_PROVIDERS_ALLOWED
        )
        !TextUtils.isEmpty(locationProviders)
    }
}

fun convertImageFileToBase64(imageFile: File?): String {
    if (imageFile == null) return ""
    return ByteArrayOutputStream().use { outputStream ->
        Base64OutputStream(outputStream, Base64.DEFAULT).use { base64FilterStream ->
            imageFile.inputStream().use { inputStream ->
                inputStream.copyTo(base64FilterStream)
            }
        }
        return@use outputStream.toString()
    }
}

fun downloadImageNew(filename: String, downloadUrlOfImage: String, context: Context) {
    try {
        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
        val downloadUri = Uri.parse(downloadUrlOfImage)
        val request = DownloadManager.Request(downloadUri)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
            .setAllowedOverRoaming(false)
            .setTitle(filename)
            .setMimeType("image/jpeg") // Your file type. You can use this code to download other file types also.
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(
                Environment.DIRECTORY_PICTURES,
                File.separator.toString() + filename + ".jpg"
            )
        dm!!.enqueue(request)
        Toast.makeText(context, "Image download started.", Toast.LENGTH_SHORT).show()
    } catch (e: java.lang.Exception) {
        Toast.makeText(context, "Image download failed.", Toast.LENGTH_SHORT).show()
    }
}

fun downloadMediaToStorage(bitmap: Bitmap?, activity: MainActivity?): Boolean {
    val filename = "${System.currentTimeMillis()}.jpg"
    var fos: OutputStream? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        activity?.contentResolver?.also { resolver ->
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
            val imageUri: Uri? = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = imageUri?.let { resolver.openOutputStream(it) }
        }
    } else {
        val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        val image = File(imagesDir, filename)
        fos = FileOutputStream(image)
    }
    fos?.use {
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
        activity?.showToast("Image Saved to Gallery successfully")
        return true
    }
    return false
}

fun getBase64FromImageURL(url: String): String? {
    try {
        val imageUrl = URL(url)
        val ucon: URLConnection = imageUrl.openConnection()
        val inputStream: InputStream = ucon.getInputStream()
        val baos = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var read: Int
        while (inputStream.read(buffer, 0, buffer.size).also { read = it } != -1) baos.write(buffer, 0, read)
        baos.flush()
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
    } catch (e: Exception) {
        Log.e("Error", "getBase64FromImageURL", e)
    }
    return null
}

fun getImageFileFromBitmap(bitmap: Bitmap?, context: Context?): File? {
    if (null == context || null == bitmap) return null
    return try {
        val bitmapFile = File(context.cacheDir, "tempFile_${System.currentTimeMillis()}")
        bitmapFile.createNewFile()
        val bos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100 , bos)
        val bitmapData = bos.toByteArray()
        val fos = FileOutputStream(bitmapFile)
        fos.write(bitmapData)
        fos.flush()
        fos.close()
        bitmapFile
    } catch (e: Exception) {
        null
    }
}

fun downloadBillInGallery(bitmap: Bitmap, orderId: String?): File? {
    val bytes = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val file = File("${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)}" + File.separator + "$orderId-${System.currentTimeMillis()}.jpg")
    return try {
        val fo = FileOutputStream(file)
        fo.write(bytes.toByteArray())
        fo.flush()
        fo.close()
        file
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}

fun getBitmapFromBase64(base64Str: String?): Bitmap? {
    val decodedBytes: ByteArray = Base64.decode(base64Str?.substring(base64Str.indexOf(",") + 1), Base64.DEFAULT)
    return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
}

fun getBitmapFromBase64V2(input: String?): Bitmap? {
    val decodedByte = Base64.decode(input, 0)
    return BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
}

fun Bitmap.getImageUri(inContext: Context?): Uri? {
    val bytes = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(inContext?.contentResolver, this, "IMG_${Calendar.getInstance().time}", null)
    return if (path == null || path.isEmpty()) null else Uri.parse(path)
}

fun openWebViewFragment(fragment: BaseFragment, title: String, webViewType: String?, redirectFromStr: String) {
    try {
        fragment.launchFragment(CommonWebViewFragment().newInstance(title, BuildConfig.WEB_VIEW_URL + webViewType + "?storeid=${fragment.getStringDataFromSharedPref(Constants.STORE_ID)}&redirectFrom=$redirectFromStr&token=${fragment.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"), true)
    } catch (e: Exception) {
        Sentry.captureException(e, "openWebViewFragment :: fragment: BaseFragment, title: String, webViewType: String?, redirectFromStr: String")
    }
}

fun openWebViewFragmentV2(fragment: BaseFragment, title: String, webViewType: String, redirectFromStr: String) {
    try {
        fragment.launchFragment(CommonWebViewFragment().newInstance(title, webViewType + "?storeid=${fragment.getStringDataFromSharedPref(Constants.STORE_ID)}&redirectFrom=$redirectFromStr&token=${fragment.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"), true)
    } catch (e: Exception) {
        Sentry.captureException(e, "openWebViewFragment :: fragment: BaseFragment, title: String, webViewType: String, redirectFromStr: String")
    }
}

fun openWebViewFragment(fragment: BaseFragment, title: String, webViewUrl: String?) {
    try {
        fragment.launchFragment(CommonWebViewFragment().newInstance(title, webViewUrl + "?storeid=${fragment.getStringDataFromSharedPref(Constants.STORE_ID)}&token=${fragment.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)}"), true)
    } catch (e: Exception) {
        Sentry.captureException(e, "openWebViewFragment :: fragment: BaseFragment, title: String, webViewType: String?")
    }
}

fun openWebViewFragmentV3(fragment: BaseFragment, title: String, webViewUrl: String?) {
    try {
        fragment.launchFragment(CommonWebViewFragment().newInstance(title, webViewUrl ?: ""), true)
    } catch (e: Exception) {
        Sentry.captureException(e, "openWebViewFragment :: fragment: BaseFragment, title: String, webViewType: String?")
    }
}

fun openWebViewFragmentWithLocation(fragment: BaseFragment, title: String, webViewType: String?) {
    try {
        fragment.launchFragment(CommonWebViewFragment().newInstance(title, webViewType ?: ""), true)
    } catch (e: Exception) {
        Sentry.captureException(e, "openWebViewFragment :: fragment: BaseFragment, title: String, webViewType: String?")
    }
}

fun getDateFromOrderString(dateStr: String?): Date? {
    if (isEmpty(dateStr)) return Date()
    val format: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.parse(dateStr ?: "")
}

fun getCompleteDateFromOrderString(date: String?): Date? {
    var dateStr: String = date ?: return Date()
    return try {
        dateStr = if (dateStr.contains("12:")) "$dateStr pm" else "$dateStr am"
        val format: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.getDefault())
        format.parse(dateStr)
    } catch (e: Exception) {
        val format: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
        format.parse(dateStr)
    }
}

fun getTimeFromOrderString(date: Date?): String {
    date?.let {dateValue ->
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return dateFormat.format(dateValue)
    }
    return ""
}

fun getStringFromOrderDate(date: Date): String {
    val dateFormat = SimpleDateFormat("dd MMM yy", Locale.getDefault())
    return dateFormat.format(date)
}

fun getTxnDateStringFromTxnDate(date: Date): String {
    val dateFormat = SimpleDateFormat("d MMM yy, EEE", Locale.getDefault())
    val string = dateFormat.format(date)
    return string.substring(0, string.length - 7) + "\'" + string.substring(string.length - 7, string.length)
}

fun getStringDateTimeFromOrderDate(date: Date?): String {
    if (date == null) return ""
    val dateFormat = SimpleDateFormat("dd MMM yy | hh:mm a", Locale.getDefault())
    return dateFormat.format(date)
}

fun getStringDateTimeFromTransactionDetailDate(date: Date?): String {
    if (date == null) return ""
    val dateFormat = SimpleDateFormat("hh:mm a - d MMM,yyyy", Locale.getDefault())
    return dateFormat.format(date)
}

fun getStringTimeFromDate(date: Date?): String {
    if (date == null) return ""
    val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return dateFormat.format(date)
}

fun getContactsFromStorage2(ctx: Context?) {
    val tag = "CONTACTS"
    try {
        if (StaticInstances.sUserContactList.isNotEmpty()) return
        Log.d(tag, "getContactsFromStorage: started")
        val list: ArrayList<ContactModel> = ArrayList()
        val uniqueMobilePhones = ArrayList<String>()
        val phoneCursor: Cursor? = ctx?.contentResolver?.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        if (phoneCursor != null && phoneCursor.count > 0) {
            val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val nameIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            while (phoneCursor.moveToNext()) {
                var number: String = phoneCursor.getString(numberIndex)?.trim() ?: ""
                number = number.trim()
                val name: String = phoneCursor.getString(nameIndex)
                var duplicate = false
                uniqueMobilePhones.forEach { addedNumber -> if (PhoneNumberUtils.compare(addedNumber, number)) duplicate = true }
                if (!duplicate) {
                    uniqueMobilePhones.add(number)
                    val info = ContactModel()
                    number = number.replace("-", "")
                    number = number.replace(" ", "")
                    number = if (number.startsWith("+91")) number.substring(3, number.length) else number
                    if (number.length < 10) continue
                    info.number = number.trim()
                    info.name = name
                    list.add(info)
                }
            }
            phoneCursor.close()
            list.run { StaticInstances.sUserContactList = this }
            Log.d(tag, "getContactsFromStorage: Completed")
            Log.d(tag, "StaticInstances.sUserContactList size :: ${StaticInstances.sUserContactList.size}")
        }
    } catch (e: Exception) {
        Log.e(tag, "getContactsFromStorage2: ${e.message}", e)
    }
}

fun getHeaderByActionInSettingKetList(profilePreviewResponse: ProfileInfoResponse?, str: String): ProfilePreviewSettingsKeyResponse? {
    profilePreviewResponse?.mSettingsKeysList?.run {
        for (settingItem in this) {
            if (settingItem.mAction == str) return settingItem
        }
    }
    return null
}

fun isAppInstalled(packageName: String, context: Context): Boolean {
    val pm: PackageManager = context.packageManager
    val appInstalled = try {
        pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
    return appInstalled
}

fun replaceTemplateString(text: String?): String? {
    if (text == null || text.isEmpty()) return ""
    var returnText = text
    returnText = StringUtils.replace(returnText," ", "-")
    returnText = StringUtils.replace(returnText,"(", "-")
    returnText = StringUtils.replace(returnText,")", "-")
    returnText = StringUtils.replace(returnText,"<", "-")
    returnText = StringUtils.replace(returnText,">", "-")
    returnText = StringUtils.replace(returnText,"{", "-")
    returnText = StringUtils.replace(returnText,"}", "-")
    returnText = StringUtils.replace(returnText,"+", "-")
    returnText = StringUtils.replace(returnText,"~", "-")
    returnText = StringUtils.replace(returnText,"$", "-")
    returnText = StringUtils.replace(returnText,"%", "-")
    returnText = StringUtils.replace(returnText,";", "-")
    returnText = StringUtils.replace(returnText,":", "-")
    returnText = StringUtils.replace(returnText,"/", "-")
    returnText = StringUtils.replace(returnText,"*", "-")
    returnText = StringUtils.replace(returnText,"#", "-")
    returnText = StringUtils.replace(returnText,"[", "-")
    returnText = StringUtils.replace(returnText,"]", "-")
    returnText = StringUtils.replace(returnText,"&", "-")
    returnText = StringUtils.replace(returnText,"@", "-")
    return returnText
}

fun isEmpty(list: List<Any>?): Boolean = list == null || list.isEmpty()

fun isEmpty(string: String?): Boolean = string == null || string.isEmpty()

fun isNotEmpty(string: String?): Boolean = !isEmpty(string)

fun isNotEmpty(list: List<Any>?): Boolean = !isEmpty(list)

fun getDayOfTheWeek(count: Int): String {
    when(count) {
        0   ->  return "Sun"
        1   ->  return "Mon"
        2   ->  return "Tue"
        3   ->  return "Wed"
        4   ->  return "Thu"
        5   ->  return "Fri"
        6   ->  return "Sat"
    }
    return ""
}

fun getMonthOfTheWeek(count: Int): String {
    when(count) {
        0   ->  return "Jan"
        1   ->  return "Feb"
        2   ->  return "Mar"
        3   ->  return "Apr"
        4   ->  return "May"
        5   ->  return "Jun"
        6   ->  return "Jul"
        7   ->  return "Aug"
        8   ->  return "Sep"
        9   ->  return "Oct"
        10  ->  return "Nov"
        11  ->  return "Dec"
    }
    return ""
}

fun isDouble(str: String?): Boolean {
    if (null == str?.toDoubleOrNull()) return false
    return true
}

fun greatestCommonFactor(width: Int, height: Int): Int = if (0 == height) width else greatestCommonFactor(height, width % height)

fun getDrawableFromUrl(url: String?): Drawable? {
    return try {
        val x: Bitmap
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        val input: InputStream = connection.inputStream
        x = BitmapFactory.decodeStream(input)
        BitmapDrawable(Resources.getSystem(), x)
    } catch (e: Exception) {
        null
    }
}

fun startShinningAnimation(view: View) {
    val service: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
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

fun getToolTipBalloon(mContext: Context?, text: String? = "Sample Testing", arrowPosition: Float = 0.5f): Balloon? {
    mContext?.let { context ->
        return createBalloon(context) {
            setArrowSize(15)
            setArrowPosition(arrowPosition)
            textSize = 11f
            paddingTop = 12
            paddingLeft = 20
            paddingRight = 20
            paddingBottom = 12
            setCornerRadius(8f)
            setText(text ?: "")
            setTextColorResource(R.color.black)
            setBackgroundColorResource(R.color.tooltip_background)
            setBalloonAnimation(BalloonAnimation.CIRCULAR)
            setAutoDismissDuration(Constants.TOOL_TIP_TIMER_INTERVAL)
        }
    }
    return null
}

fun isSingleDigitNumber(number: Long): Boolean {
    return !((number in 10..99) || (number < -9 && number > -100))
}

fun getBitmapFromView(view: View, activityMain: Activity?): Bitmap? {
    return try {
        activityMain?.let { activity ->
            Log.e("GlobalMethods", "getBitmapFromView: $view")
            val dm: DisplayMetrics = activity.resources.displayMetrics
            view.measure(
                MeasureSpec.makeMeasureSpec(dm.widthPixels, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(dm.heightPixels, MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
            val returnedBitmap = Bitmap.createBitmap(
                view.measuredWidth,
                view.measuredHeight, Bitmap.Config.ARGB_8888
            )
            val c = Canvas(returnedBitmap)
            view.draw(c)
            returnedBitmap
        }
    } catch (e: Exception) {
        Log.e("GlobalMethods", "getBitmapFromView: ${e.message}", e)
        null
    }
}

fun getQRCodeBitmap(activity: MainActivity?, text: String?): Bitmap? {
    activity?.let { context ->
        Log.d("GlobalMethods", "getQRCodeBitmap: $text")
        val manager = context.getSystemService(WINDOW_SERVICE) as WindowManager
        val display = manager.defaultDisplay
        val point = Point()
        display.getSize(point)
        val width = point.x
        val height = point.y
        var dimen = if (width < height) width else height
        dimen = dimen * 3 / 4
        val qrgEncoder = QRGEncoder(text, null, QRGContents.Type.TEXT, dimen)
        Log.d("GlobalMethods", "qrgEncoder: $qrgEncoder")
        return try {
            qrgEncoder.encodeAsBitmap()
        } catch (e: Exception) {
            Log.e("GlobalMethods", e.toString())
            null
        }
    }
    return null
}