package com.digitaldukaan

import android.os.Bundle
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI
import com.digitaldukaan.constants.StaticInstances
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFcmMessageListenerService : FirebaseMessagingService() {

    companion object {
        private val TAG = MyFcmMessageListenerService::class.simpleName
    }

    override fun onMessageReceived(message: RemoteMessage) {
        message.data.apply {
            try {
                if (size > 0) {
                    val extras = Bundle()
                    for ((key, value) in this) {
                        extras.putString(key, value)
                    }
                    val info = CleverTapAPI.getNotificationInfo(extras)
                    if (info.fromCleverTap) {
                        CleverTapAPI.createNotification(applicationContext, extras)
                    }
                }
            } catch (t: Throwable) {
                Log.d(TAG, "Error parsing FCM message", t)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "onNewToken: sFireBaseMessagingToken :: ${StaticInstances.sFireBaseMessagingToken}")
        StaticInstances.sFireBaseMessagingToken = token
    }
}