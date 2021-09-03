package com.digitaldukaan.constants

import android.app.Activity
import android.app.NotificationManager
import android.os.Bundle
import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.clevertap.android.sdk.CleverTapAPI
import com.digitaldukaan.models.dto.CleverTapProfile
import com.digitaldukaan.models.request.AndroidEventLogRequest
import com.digitaldukaan.network.RetrofitApi
import com.google.firebase.analytics.FirebaseAnalytics
import io.sentry.Sentry

class AppEventsManager {

    companion object {

        private const val TAG = "AppEventsManager"

        private var mActivityInstance: Activity? = null
        private var mCleverTapAPI: CleverTapAPI? = null
        private var mFirebaseAnalytics: FirebaseAnalytics? = null

        fun setAppEventsManager(activity: Activity) = run {
            mActivityInstance = activity
            mCleverTapAPI = CleverTapAPI.getDefaultInstance(mActivityInstance)
            mActivityInstance?.let { context -> mFirebaseAnalytics = FirebaseAnalytics.getInstance(context) }
            StaticInstances.sCleverTapId = mCleverTapAPI?.cleverTapID
            Log.d(TAG, "setAppEventsManager: ${StaticInstances.sCleverTapId}")
            createNotificationChannel()
        }

        fun pushAppEvents(eventName: String?, isCleverTapEvent: Boolean, isAppFlyerEvent: Boolean, isServerCallEvent: Boolean, data: Map<String, String?>) {
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

        private fun pushServerCallEvent(eventName: String?, data: Map<String, String?>) {
            if (eventName == null || eventName.isEmpty()) {
                return
            }
            CoroutineScopeUtils().runTaskOnCoroutineBackground {
                try {
                    Log.d(TAG, "pushServerCallEvent: event name :: $eventName && map :: $data")
                    val storeIdStr = PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)
                    val request = AndroidEventLogRequest(if (storeIdStr.isNotEmpty()) storeIdStr.toInt() else 0, eventName, data)
                    val response = RetrofitApi().getServerCallObject()?.androidEventLog(request)
                    Log.d(TAG, "pushServerCallEvent: $response")
                } catch (e: Exception) {
                    Sentry.captureException(e, "pushServerCallEvent: exception")
                }
            }
        }

        private fun pushCleverTapEvent(eventName: String?, data: Map<String, String?>) {
            try {
                Log.d(TAG, "pushCleverTapEvent: event name :: $eventName && map :: $data")
                mCleverTapAPI?.pushEvent(eventName, data)
                Log.d(TAG, "pushCleverTapEvent: DONE")
            } catch (e: Exception) {
                Sentry.captureException(e, "pushCleverTapEvent: exception")
                pushServerCallEvent(AFInAppEventType.EVENT_CLERVERTAP_EXCEPTION, mapOf("exception" to e.toString()))
            }
        }

        private fun pushAppFlyerEvent(eventName: String?, data: Map<String, String?>) {
            try {
                Log.d(TAG, "pushAppFlyerEvent: event name :: $eventName && map :: $data")
                val appFlyerInstance = AppsFlyerLib.getInstance()
                mActivityInstance?.let { context -> appFlyerInstance?.logEvent(context,  eventName, data) }
                Log.d(TAG, "pushAppFlyerEvent: DONE")
            } catch (e: Exception) {
                Sentry.captureException(e, "pushAppFlyerEvent: exception")
                pushServerCallEvent(AFInAppEventType.EVENT_APPFLYER_EXCEPTION, mapOf("exception" to e.toString()))
            }
            try {
                val bundle = Bundle()
                for (entry in data.entries) {
                    val value = "" + entry.value
                    bundle.putString(entry.key, value)
                }
                Log.d(TAG, "FIREBASE ANALYTICS: event name :: $eventName && bundle :: $bundle")
                mFirebaseAnalytics?.logEvent(eventName ?: "", bundle)
                Log.d(TAG, "FIREBASE ANALYTICS: DONE")
            } catch (e: Exception) {
                Log.e(TAG, "pushAppFlyerEvent: FIREBASE ANALYTICS Exception", e)
                Sentry.captureException(e, "FIREBASE ANALYTICS: exception")
                pushServerCallEvent(AFInAppEventType.EVENT_APPFLYER_EXCEPTION, mapOf("exception" to e.toString()))
            }
        }

        fun pushCleverTapProfile(profile: CleverTapProfile) {
            val profileUpdate = HashMap<String, Any?>()
            profileUpdate["name"] = "Digital Showroom Merchant"
            profileUpdate["Identity"] = profile.mIdentity
            profileUpdate["Phone"] = "+91${profile.mPhone}"
            profileUpdate["isMerchant"] = profile.mIsMerchant
            profileUpdate["storeID"] = PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)
            profileUpdate["shopCategory"] = profile.mShopCategory
            profileUpdate["lat"] = profile.mLat
            profileUpdate["lng"] = profile.mLong
            profileUpdate["merchant"] = 1
            profileUpdate["customer"] = 0
            profileUpdate["shopName"] = profile.mShopName
            profileUpdate["address"] = profile.mAddress
            profileUpdate.forEach { (key, value) ->
                Log.d(TAG, "pushCleverTapProfile:  $key = $value")
            }
            CleverTapAPI.getDefaultInstance(mActivityInstance)?.onUserLogin(profileUpdate)
        }

        private fun createNotificationChannel() {
            CleverTapAPI.createNotificationChannelGroup(
                mActivityInstance,
                AFInAppEventParameterName.NOTIFICATION_GROUP_ID,
                AFInAppEventParameterName.NOTIFICATION_GROUP_NAME
            )
            CleverTapAPI.createNotificationChannel(
                mActivityInstance,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_NOTIFICATIONS,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_NAME,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_DESC,
                NotificationManager.IMPORTANCE_MAX,
                true
            )
            CleverTapAPI.createNotificationChannel(
                mActivityInstance,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_UPDATES,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_NAME,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_DESC,
                NotificationManager.IMPORTANCE_MAX,
                true
            )
            CleverTapAPI.createNotificationChannel(
                mActivityInstance,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_CAMPAIGN,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_NAME,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_DESC,
                NotificationManager.IMPORTANCE_MAX,
                true
            )
        }
    }
}