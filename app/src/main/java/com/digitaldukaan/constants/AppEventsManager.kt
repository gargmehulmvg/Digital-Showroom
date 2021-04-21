package com.digitaldukaan.constants

import android.app.Activity
import android.app.NotificationManager
import android.util.Log
import com.appsflyer.AppsFlyerLib
import com.clevertap.android.sdk.CleverTapAPI
import com.digitaldukaan.models.dto.CleverTapProfile
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
            CoroutineScopeUtils().runTaskOnCoroutineBackground {
                Log.d(TAG, "pushServerCallEvent: event name :: $eventName && map :: $data")
                val storeIdStr = PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID)
                val request = AndroidEventLogRequest(if (storeIdStr.isNotEmpty()) storeIdStr.toInt() else 0, eventName, data)
                val response = RetrofitApi().getServerCallObject()?.androidEventLog(request)
                Log.d(TAG, "pushServerCallEvent: $response")
            }
        }

        private fun pushCleverTapEvent(eventName: String?, data: Map<String, String?>) {
            Log.d(TAG, "pushCleverTapEvent: event name :: $eventName && map :: $data")
            mCleverTapAPI?.pushEvent(eventName, data)
            Log.d(TAG, "pushCleverTapEvent: DONE")
        }

        private fun pushAppFlyerEvent(eventName: String?, data: Map<String, String?>) {
            Log.d(TAG, "pushAppFlyerEvent: event name :: $eventName && map :: $data")
            val appFlyerInstance = AppsFlyerLib.getInstance()
            appFlyerInstance?.logEvent(mActivityInstance,  eventName, data)
            Log.d(TAG, "pushAppFlyerEvent: DONE")
        }

        fun pushCleverTapProfile(profile: CleverTapProfile) {
            val profileUpdate = HashMap<String, Any?>()
            profileUpdate["name"] = "dot kirana merchant"
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