package com.digitaldukaan.constants

import android.app.Activity
import android.app.NotificationManager
import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.clevertap.android.sdk.CleverTapAPI
import com.digitaldukaan.models.request.AndroidEventLogRequest
import com.digitaldukaan.network.RetrofitApi

class AppEventsManager {

    companion object {

        private const val TAG = "AppEventsManager"

        private var mActivityInstance: Activity? = null
        private var mCleverTapAPI: CleverTapAPI? = null

        fun setAppEventsManager(activity: Activity) = run {
            mActivityInstance = activity
            mCleverTapAPI = CleverTapAPI.getDefaultInstance(mActivityInstance)
        }

        fun pushAppEvents(eventName: String?, isCleverTapEvent: Boolean, isAppFlyerEvent: Boolean, isServerCallEvent: Boolean, data: Map<String, String>) {
            if (isCleverTapEvent) {
                pushCleverTapEvent(eventName, data)
            }
            if (isAppFlyerEvent) {
                pushAppFlyerEvent(eventName, data)
            }
            if (isServerCallEvent) {
                pushServerCallEvent(eventName, data)
            }
        }

        private fun pushServerCallEvent(eventName: String?, data: Map<String, String>) {
            CoroutineScopeUtils().runTaskOnCoroutineBackground {
                Log.d(TAG, "pushServerCallEvent: event name :: $eventName && map :: $data")
                val request = AndroidEventLogRequest(PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID).toInt(), eventName, data)
                val response = RetrofitApi().getServerCallObject()?.androidEventLog(request)
                Log.d(TAG, "pushServerCallEvent: $response")
            }
        }

        private fun pushCleverTapEvent(eventName: String?, data: Map<String, String>) {
            Log.d(TAG, "pushCleverTapEvent: event name :: $eventName && map :: $data")
            mCleverTapAPI?.pushEvent(eventName, data)
            Log.d(TAG, "pushCleverTapEvent: DONE")
        }

        private fun pushAppFlyerEvent(eventName: String?, data: Map<String, String>) {
            Log.d(TAG, "pushAppFlyerEvent: event name :: $eventName && map :: $data")
            val appFlyerInstance = AppsFlyerLib.getInstance()
            appFlyerInstance?.logEvent(mActivityInstance,  eventName, data)
            Log.d(TAG, "pushAppFlyerEvent: DONE")
        }

        private fun createNotificationChannel() {
            CleverTapAPI.createNotificationChannel(
                mActivityInstance,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_ID,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_NAME,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_DESC,
                NotificationManager.IMPORTANCE_MAX,
                true
            )
        }
    }
}