package com.digitaldukaan.smsapi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

/**
 * BroadcastReceiver to wait for SMS messages. This can be registered either
 * in the AndroidManifest or at runtime.  Should filter Intents on
 * SmsRetriever.SMS_RETRIEVED_ACTION.
 */
class MySMSBroadcastReceiver : BroadcastReceiver() {

    private lateinit var mSmsReceiverListener: ISmsReceivedListener

    fun setSmsReceiveListener(listener: ISmsReceivedListener) {
        mSmsReceiverListener = listener
    }

    override fun onReceive(context: Context?, intent: Intent) {
        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
            val extras = intent.extras
            val status: Status? = extras!![SmsRetriever.EXTRA_STATUS] as Status?
            when (status?.statusCode) {
                CommonStatusCodes.SUCCESS -> {
                    val message = extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?
                    mSmsReceiverListener.onNewSmsReceived(message)
                }
                CommonStatusCodes.TIMEOUT -> {
                    val message = "Timeout no SMS received"
                }
            }
        }
    }
}