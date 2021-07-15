package com.digitaldukaan

import android.app.ActivityManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log
import android.webkit.WebView
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.clevertap.android.sdk.ActivityLifecycleCallback
import com.digitaldukaan.constants.AFInAppEventParameterName
import com.digitaldukaan.constants.StaticInstances
import com.google.firebase.crashlytics.FirebaseCrashlytics


class App: Application() {

    companion object {
        private const val APP_FLYER_DEV_KEY = "aLPBh66qehqonqtR9AeCtL"
        private const val TAG = "AppApplication"
    }

    override fun onCreate() {
        ActivityLifecycleCallback.register(this)
        super.onCreate()
        val conversionDataListener  = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                data?.let { cvData ->
                    cvData.forEach { (key, value) ->
                        Log.d(TAG, "conversion_attribute:  $key = $value")
                    }
                    val phoneNumber = cvData["af_referrer_customer_id"] as String
                    val isFirstLaunch = cvData["is_first_launch"] as Boolean
                    if (isFirstLaunch) StaticInstances.sAppFlyerRefMobileNumber = phoneNumber
                    Log.d(TAG, "conversion_attribute :: StaticInstances.sAppFlyerRefMobileNumber ::  ${StaticInstances.sAppFlyerRefMobileNumber}")
                }
            }

            override fun onConversionDataFail(error: String?) {
                Log.e(TAG, "error onAttributionFailure :  $error")
            }

            override fun onAppOpenAttribution(data: MutableMap<String, String>?) {
                data?.map {
                    Log.d(TAG, "onAppOpen_attribute: ${it.key} = ${it.value}")
                }
            }

            override fun onAttributionFailure(error: String?) {
                Log.e(TAG, "error onAttributionFailure :  $error")
            }
        }
        AppsFlyerLib.getInstance().apply {
            //setDebugLog(true)
            setAppInviteOneLink("KgNd")
            init(APP_FLYER_DEV_KEY, conversionDataListener, this@App)
            start(this@App)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_NOTIFICATIONS,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val processName = getProcessName(this)
            val packageName = this.packageName
            if (packageName != processName) WebView.setDataDirectorySuffix(processName)
        }
    }

    private fun getProcessName(context: Context?): String? {
        var processName: String? = ""
        try {
            if (null == context) return processName
            val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (processInfo in manager.runningAppProcesses) {
                if (processInfo.pid == Process.myPid()) {
                    processName = processInfo.processName
                    return processName
                }
            }
        } catch (e: Exception) {
            return processName
        }
        return processName
    }
}