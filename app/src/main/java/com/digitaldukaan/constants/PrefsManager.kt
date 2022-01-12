package com.digitaldukaan.constants

import android.app.Activity
import android.content.Context

class PrefsManager {

    companion object {

        const val APP_INSTANCE_ID               = "APP_INSTANCE_ID"
        const val KEY_SETTLEMENT_START_DATE     = "KEY_SETTLEMENT_START_DATE"
        const val KEY_SETTLEMENT_END_DATE       = "KEY_SETTLEMENT_END_DATE"
        const val KEY_TXN_START_DATE            = "KEY_TXN_START_DATE"
        const val KEY_TXN_END_DATE              = "KEY_TXN_END_DATE"
        const val KEY_FIRST_ITEM_COMPLETED      = "KEY_FIRST_ITEM_COMPLETED"

        private var mActivityInstance: Activity? = null

        fun setPrefsManager(activity: Activity) = run { mActivityInstance = activity }

        fun getStringDataFromSharedPref(keyName: String?): String {
            val prefs = mActivityInstance?.getSharedPreferences(getSharedPreferenceName(), Context.MODE_PRIVATE)
            return prefs?.getString(keyName, "").toString()
        }

        fun getBoolDataFromSharedPref(keyName: String?): Boolean {
            val prefs = mActivityInstance?.getSharedPreferences(getSharedPreferenceName(), Context.MODE_PRIVATE)
            return prefs?.getBoolean(keyName, false) ?: false
        }

        fun storeStringDataInSharedPref(keyName: String, value: String?) {
            val editor = mActivityInstance?.getSharedPreferences(getSharedPreferenceName(), Context.MODE_PRIVATE)?.edit()
            editor?.putString(keyName, value)
            editor?.apply()
        }

        fun storeBoolDataInSharedPref(keyName: String, value: Boolean) {
            val editor = mActivityInstance?.getSharedPreferences(getSharedPreferenceName(), Context.MODE_PRIVATE)?.edit()
            editor?.putBoolean(keyName, value)
            editor?.apply()
        }
    }

}