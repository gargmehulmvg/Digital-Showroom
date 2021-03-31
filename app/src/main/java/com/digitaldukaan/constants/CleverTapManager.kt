package com.digitaldukaan.constants

import android.app.Activity
import android.app.NotificationManager
import com.clevertap.android.sdk.CleverTapAPI

class CleverTapManager {

    companion object {
        private var mActivityInstance: Activity? = null
        fun setCleverTapManager(activity: Activity) = run { mActivityInstance = activity }

        fun createNotificationChannel() {
            CleverTapAPI.createNotificationChannel(
                mActivityInstance,
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                Constants.NOTIFICATION_CHANNEL_DESC,
                NotificationManager.IMPORTANCE_MAX,
                true
            )
        }

        fun pushCleverTapEvent(eventName: String? = "Test Event") {
            val cleverTapAPI = CleverTapAPI.getDefaultInstance(mActivityInstance)
            cleverTapAPI?.pushEvent(eventName)
        }
    }

}