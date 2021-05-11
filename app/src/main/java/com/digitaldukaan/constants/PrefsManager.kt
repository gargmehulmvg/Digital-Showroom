package com.digitaldukaan.constants

import android.app.Activity
import android.content.Context

class PrefsManager {

    companion object {

        const val APP_INSTANCE_ID = "APP_INSTANCE_ID"

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