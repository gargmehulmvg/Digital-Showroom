package com.digitaldukaan.smsapi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * BroadcastReceiver to wait for SMS messages. This can be registered either
 * in the AndroidManifest or at runtime.  Should filter Intents on
 * SmsRetriever.SMS_RETRIEVED_ACTION.
 */
class MySMSBroadcastReceiver : BroadcastReceiver() {

    companion object {
        var mSmsReceiverListener: ISmsReceivedListener? = null
        val tag = MySMSBroadcastReceiver::class.simpleName
    }

    override fun onReceive(context: Context?, intent: Intent) {
        Log.d(tag, "onReceive: called :: intent :: $intent")
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status: Status? = extras!![SmsRetriever.EXTRA_STATUS] as Status?
            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?
                    Log.d(tag, "onReceive: $message")
                    Log.d(tag, "mSmsReceiverListener: $mSmsReceiverListener")
                    mSmsReceiverListener?.onNewSmsReceived(message?.split(":")?.get(1)?.trim()?.substring(0, 4))
                }
                CommonStatusCodes.TIMEOUT -> {
                    val message = "Timeout no SMS received"
                }
            }
        }
    }
}