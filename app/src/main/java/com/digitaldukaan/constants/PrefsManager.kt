package com.digitaldukaan.constants

import android.app.Activity
import android.content.Context

class PrefsManager {

    companion object {

        const val APP_INSTANCE_ID = "APP_INSTANCE_ID"
        const val KEY_SETTLEMENT_START_DATE = "KEY_SETTLEMENT_START_DATE"
        const val KEY_SETTLEMENT_END_DATE = "KEY_SETTLEMENT_END_DATE"
        const val KEY_TXN_START_DATE = "KEY_TXN_START_DATE"
        const val KEY_TXN_END_DATE = "KEY_TXN_END_DATE"

        private var mActivityInstance: Activity? = null
        fun setPrefsManager(activity: Activity) = run { mActivityInstance = activity }

        fun getStringDataFromSharedPref(keyName: String?): String {
            val prefs = mActivityInstance?.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)
            return prefs?.getString(keyName, "").toString()
        }

        fun storeStringDataInSharedPref(keyName: String, value: String?) {
            val editor = mActivityInstance?.getSharedPreferences(Constants.SHARED_PREF_NAME, Context.MODE_PRIVATE)?.edit()
            editor?.putString(keyName, value)
            editor?.apply()
        }
    }

}