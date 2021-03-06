package com.digitaldukaan.constants

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.fragments.CommonWebViewFragment
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

fun getStringFromOrderDate(date: Date): String {
    val dateFormat = SimpleDateFormat("dd MMM yy", Locale.getDefault());
    return dateFormat.format(date)
}