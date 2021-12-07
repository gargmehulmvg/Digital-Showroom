package com.digitaldukaan.network

import android.util.Log
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.PrefsManager
import com.digitaldukaan.constants.StaticInstances
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitApi {

    private var mAppService: Apis? = null
    private var mAppAnalyticsService: Apis? = null
    private val mTag = "RetrofitApi"

    fun getServerCallObject(): Apis? {
        if (null == mAppService) {
            val retrofit = Retrofit.Builder().apply {
                baseUrl(BuildConfig.BASE_URL)
                addConverterFactory(GsonConverterFactory.create())
                client(getHttpClient())
            }.build()
            mAppService = retrofit.create(Apis::class.java)
        }
        return mAppService
    }

    fun getAnalyticsServerCallObject(): Apis? {
        if (null == mAppAnalyticsService) {
            val retrofit = Retrofit.Builder().apply {
                baseUrl(if (BuildConfig.DEBUG) BuildConfig.BASE_URL else Constants.ANALYTICS_PRODUCTION_URL)
                addConverterFactory(GsonConverterFactory.create())
                client(getHttpClient())
            }.build()
            mAppAnalyticsService = retrofit.create(Apis::class.java)
        }
        return mAppAnalyticsService
    }

    private fun getHttpClient(): OkHttpClient {
        val loggingInterface = HttpLoggingInterceptor().apply {
            level = when (BuildConfig.DEBUG) {
                true  -> HttpLoggingInterceptor.Level.BODY
                false -> HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder().apply {
            connectTimeout(1, TimeUnit.MINUTES)
            callTimeout(1, TimeUnit.MINUTES)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            addInterceptor {
                customizeCustomRequest(it)
            }
            addInterceptor(loggingInterface)
            protocols(arrayListOf(Protocol.HTTP_1_1, Protocol.HTTP_2))
        }.build()
    }

    private fun customizeCustomRequest(it: Interceptor.Chain): Response {
        val originalRequest = it.request()
        val newRequest = getNewRequest(originalRequest)
        return if (null == newRequest) it.proceed(originalRequest) else it.proceed(newRequest)
    }

    private fun getNewRequest(originalRequest: Request): Request? {
        return try {
            originalRequest.newBuilder().apply {
                addHeader("auth_token", PrefsManager.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
                addHeader("session_id", StaticInstances.sAppSessionId ?: "")
                addHeader("install_id", PrefsManager.getStringDataFromSharedPref(PrefsManager.APP_INSTANCE_ID))
                addHeader("app_os", "android_native")
                addHeader("app_version", BuildConfig.VERSION_NAME)
            }.build()
        } catch (e: Exception) {
            Log.e(mTag, "getNewRequest: ${e.message}", e)
            null
        }
    }
}
