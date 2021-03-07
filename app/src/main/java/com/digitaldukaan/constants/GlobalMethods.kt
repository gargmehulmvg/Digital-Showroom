package com.digitaldukaan.constants

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.provider.MediaStore
import android.util.Log
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.fragments.CommonWebViewFragment
import com.digitaldukaan.models.request.ContactModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


fun getBitmapFromURL(src: String?): Bitmap? {
    return try {
        BitmapFactory.decodeStream(URL(src).openConnection().getInputStream())
    } catch (e: Exception) {
        null
    }
}

fun getImageFileFromBitmap(bitmap: Bitmap, context: Context): File {
    val bitmapFile = File(context.cacheDir, "tempFile")
    bitmapFile.createNewFile()
    val bos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos)
    val bitmapData = bos.toByteArray()
    val fos = FileOutputStream(bitmapFile)
    fos.write(bitmapData)
    fos.flush()
    fos.close()
    return bitmapFile
}

fun Bitmap.getImageUri(inContext: Context): Uri? {
    val bytes = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
    val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, this, "Title", null)
    return Uri.parse(path)
}

fun openWebViewFragment(fragment: BaseFragment, title: String, webViewType: String, redirectFromStr: String) {
    fragment.launchFragment(
        CommonWebViewFragment().newInstance(title,
            BuildConfig.WEB_VIEW_URL + webViewType + "?storeid=${fragment.getStringDataFromSharedPref(Constants.STORE_ID)}&" + "redirectFrom=$redirectFromStr" + "&token=${fragment.getStringDataFromSharedPref(
                Constants.USER_AUTH_TOKEN
            )}"
        ), true
    )
}

fun getDateFromOrderString(dateStr: String): Date? {
    val format: DateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    return format.parse(dateStr)
}

fun getCompleteDateFromOrderString(dateStr: String): Date? {
    val format: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault())
    return format.parse(dateStr)
}

fun getTimeFromOrderString(date: Date?): String {
    date?.run {
        val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault());
        return dateFormat.format(this)
    }
    return ""
}

fun getStringFromOrderDate(date: Date): String {
    val dateFormat = SimpleDateFormat("dd MMM yy", Locale.getDefault());
    return dateFormat.format(date)
}

fun getContactsFromStorage2(ctx: Context) {
    val tag = "CONTACTS"
    if (StaticInstances.sUserContactList.isNotEmpty()) return
    Log.d(tag, "getContactsFromStorage: started")
    val list: MutableList<ContactModel> = ArrayList()
    val phoneCursor: Cursor? = ctx.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        null,
        null,
        null,
        null
    )
    if (phoneCursor != null && phoneCursor.count > 0) {
        val contactIdIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
        val numberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
        val nameIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        while (phoneCursor.moveToNext()) {
            val contactId = phoneCursor.getString(contactIdIndex)
            var number: String = phoneCursor.getString(numberIndex)
            val name: String = phoneCursor.getString(nameIndex)
            //check if the map contains key or not, if not then create a new array list with number
            val info = ContactModel()
            number = number.replace("-", "")
            number = if (number.startsWith("+91")) number.substring(3, number.length) else number
            if (number.length < 10) continue
            info.id = contactId
            info.mobileNumber = number
            info.name = name
            list.add(info)
        }
        //contact contains all the number of a particular contact
        phoneCursor.close()
        list.run { StaticInstances.sUserContactList = this }
        Log.d(tag, "getContactsFromStorage: Completed")
    }
}