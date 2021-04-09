package com.digitaldukaan

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.digitaldukaan.constants.AFInAppEventParameterName

class App: Application() {

    companion object {
        private const val APP_FLYER_DEV_KEY = "aLPBh66qehqonqtR9AeCtL"
        private const val TAG = "AppApplication"
    }

    override fun onCreate() {
        super.onCreate()
        val conversionDataListener  = object : AppsFlyerConversionListener {
            override fun onConversionDataSuccess(data: MutableMap<String, Any>?) {
                data?.let { cvData ->
                    cvData.map {
                        Log.i(TAG, "conversion_attribute:  ${it.key} = ${it.value}")
                    }
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
        AppsFlyerLib.getInstance().init(APP_FLYER_DEV_KEY, conversionDataListener, this)
        AppsFlyerLib.getInstance().start(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_ID,
                AFInAppEventParameterName.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(notificationChannel)
        }
    }
}