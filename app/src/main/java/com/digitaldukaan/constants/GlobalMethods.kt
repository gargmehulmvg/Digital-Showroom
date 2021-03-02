package com.digitaldukaan.constants

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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