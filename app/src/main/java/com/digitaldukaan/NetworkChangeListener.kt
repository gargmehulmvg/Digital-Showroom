package com.digitaldukaan

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import com.digitaldukaan.services.isInternetConnectionAvailable

class NetworkChangeListener : BroadcastReceiver() {

    companion object {
        private const val TAG = "NetworkChangeListener"
        private var noInternetDialog: Dialog? = null
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG, "onReceive: called")
        context?.run {
            if (noInternetDialog == null) {
                noInternetDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            }
            if (!isInternetConnectionAvailable(this)) {
                noInternetDialog?.apply {
                    val view = LayoutInflater.from(this@run).inflate(R.layout.layout_no_internet, null)
                    setContentView(view)
                    setCancelable(false)
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                }?.show()
            } else if (noInternetDialog?.isShowing == true) noInternetDialog?.dismiss()
            else noInternetDialog?.dismiss()
        }
    }
}