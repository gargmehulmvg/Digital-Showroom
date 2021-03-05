package com.digitaldukaan.constants

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.R
import com.digitaldukaan.fragments.BaseFragment
import com.digitaldukaan.fragments.CommonWebViewFragment
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL


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

fun openHelpFromToolbar(fragment: BaseFragment) {
    fragment.launchFragment(
        CommonWebViewFragment().newInstance(fragment.getString(R.string.help),
            BuildConfig.WEB_VIEW_URL + Constants.WEB_VIEW_HELP + "?storeid=${fragment.getStringDataFromSharedPref(
                Constants.STORE_ID
            )}&" + "redirectFrom=settings" + "&token=${fragment.getStringDataFromSharedPref(
                Constants.USER_AUTH_TOKEN
            )}"
        ), true
    )
}