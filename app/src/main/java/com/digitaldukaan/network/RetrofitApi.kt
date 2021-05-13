package com.digitaldukaan.network

import com.digitaldukaan.BuildConfig
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.PrefsManager
import com.digitaldukaan.constants.StaticInstances
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class RetrofitApi {

    private var mAppService: Apis? = null

    fun getServerCallObject(): Apis? {
        if (mAppService == null) {
            val loggingInterface = HttpLoggingInterceptor()
            loggingInterface.level = HttpLoggingInterceptor.Level.BODY
            val okHttpClient = OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor {
                    val originalRequest = it.request()
                    val newRequest = getNewRequest(originalRequest)
                    it.proceed(newRequest)
                }
                .addInterceptor(loggingInterface)
                .protocols(arrayListOf(Protocol.HTTP_1_1))
                .build()
            val okHttpClientProd = OkHttpClient.Builder()
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .addInterceptor {
                    val originalRequest = it.request()
                    val newRequest = getNewRequest(originalRequest)
                    it.proceed(newRequest)
                }
                .protocols(arrayListOf(Protocol.HTTP_1_1))
                .build()
            val retrofit = Retrofit.Builder()
                .baseUrl(BuildConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(if (BuildConfig.DEBUG) okHttpClient else okHttpClientProd)
                .build()
            mAppService = retrofit.create(Apis::class.java)
        }
        return mAppService
    }

    private fun getNewRequest(originalRequest: Request): Request {
        return originalRequest.newBuilder()
            .addHeader(
                "auth_token",
                PrefsManager.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN)
            )
            .addHeader("session_id", StaticInstances.sAppSessionId ?: "")
            .addHeader(
                "install_id",
                PrefsManager.getStringDataFromSharedPref(PrefsManager.APP_INSTANCE_ID)
            )
            .addHeader("app_os", "android_native")
            .addHeader("app_version", BuildConfig.VERSION_NAME)
            .build()
    }

}