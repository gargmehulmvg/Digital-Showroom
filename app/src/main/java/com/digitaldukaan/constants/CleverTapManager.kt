package com.digitaldukaan.constants

import android.app.Activity
import android.app.NotificationManager
import android.util.Log
import com.clevertap.android.sdk.CleverTapAPI

class CleverTapManager {

    companion object {

        private const val EVENT_SET_MIN_ORDER_VALUE = "Set_minimum_order_value"
        private const val EVENT_SET_DELIVERY_CHARGE = "Set_delivery_charge"
        private const val EVENT_MARKET_VIEW_NOW = "Mkt_View_Now"
        private const val STORE_ID = "Store ID"
        private const val TAG = "CleverTapManager"

        private var mActivityInstance: Activity? = null
        private var mCleverTapAPI: CleverTapAPI? = null

        fun setCleverTapManager(activity: Activity) = run {
            mActivityInstance = activity
            mCleverTapAPI = CleverTapAPI.getDefaultInstance(mActivityInstance)
        }

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

        fun pushMinOrderValueEvent() {
            val map = mapOf(STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
            Log.d(TAG, "pushMinOrderValueEvent: mCleverTap :: $mCleverTapAPI && map :: $map")
            mCleverTapAPI?.pushEvent(EVENT_SET_MIN_ORDER_VALUE, map)
            Log.d(TAG, "pushMinOrderValueEvent: DONE")
        }

        fun pushDeliveryChargeEvent() {
            val map = mapOf(STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID))
            Log.d(TAG, "pushDeliveryChargeEvent: mCleverTap :: $mCleverTapAPI && map :: $map")
            mCleverTapAPI?.pushEvent(EVENT_SET_DELIVERY_CHARGE, map)
            Log.d(TAG, "pushDeliveryChargeEvent: DONE")
        }

        fun pushMarketingOffersViewNowEvent() {
            val map = mapOf(STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
            "type" to "Offers")
            Log.d(TAG, "pushMarketingOffersViewNowEvent: mCleverTap :: $mCleverTapAPI && map :: $map")
            mCleverTapAPI?.pushEvent(EVENT_MARKET_VIEW_NOW, map)
            Log.d(TAG, "pushMarketingOffersViewNowEvent: DONE")
        }

        fun pushMarketingSocialViewNowEvent() {
            val map = mapOf(STORE_ID to PrefsManager.getStringDataFromSharedPref(Constants.STORE_ID),
            "type" to "Social")
            Log.d(TAG, "pushMarketingSocialViewNowEvent: mCleverTap :: $mCleverTapAPI && map :: $map")
            mCleverTapAPI?.pushEvent(EVENT_MARKET_VIEW_NOW, map)
            Log.d(TAG, "pushMarketingSocialViewNowEvent: DONE")
        }
    }

}