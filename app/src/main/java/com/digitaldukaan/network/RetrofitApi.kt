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

    fun getServerCallObject(): Apis? {
        return try {
            if (mAppService == null) {
                val retrofit = Retrofit.Builder().apply {
                    baseUrl(BuildConfig.BASE_URL)
                    addConverterFactory(GsonConverterFactory.create())
                    client(if (BuildConfig.DEBUG) getDebugHttpClient() else getProdHttpClient())
                }.build()
                mAppService = retrofit.create(Apis::class.java)
            }
            mAppService
        } catch (e: Exception) {
            Log.e("RetrofitApi", "getServerCallObject: ${e.message}", e)
            null
        }
    }

    private fun getProdHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            readTimeout(15, TimeUnit.SECONDS)
            connectTimeout(15, TimeUnit.SECONDS)
            addInterceptor { customizeCustomRequest(it) }
            protocols(arrayListOf(Protocol.HTTP_1_1))
        }.build()
    }

    private fun getDebugHttpClient(): OkHttpClient {
        val loggingInterface = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        return OkHttpClient.Builder().apply {
            readTimeout(15, TimeUnit.SECONDS)
            connectTimeout(15, TimeUnit.SECONDS)
            addInterceptor { customizeCustomRequest(it) }
            addInterceptor(loggingInterface)
            protocols(arrayListOf(Protocol.HTTP_1_1))
        }.build()
    }

    private fun customizeCustomRequest(it: Interceptor.Chain): Response {
        val originalRequest = it.request()
        val newRequest = getNewRequest(originalRequest)
        return if (newRequest == null) it.proceed(originalRequest) else it.proceed(newRequest)
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
            null
        }
    }
}
