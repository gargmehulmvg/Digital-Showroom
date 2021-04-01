package com.digitaldukaan

import android.app.Application
import android.util.Log
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.clevertap.android.sdk.ActivityLifecycleCallback


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
    }
}