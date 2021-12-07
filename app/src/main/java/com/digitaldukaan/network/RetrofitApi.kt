package com.digitaldukaan.network

import android.util.Log
import com.digitaldukaan.BuildConfig
import com.digitaldukaan.constants.Constants
import com.digitaldukaan.constants.PrefsManager
import com.digitaldukaan.constants.StaticInstances
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import okhttp3.internal.http2.ConnectionShutdownException
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

class RetrofitApi {

    private var mAppService: Apis? = null
    private var mAppAnalyticsService: Apis? = null

    companion object {
        private const val TAG = "RetrofitApi"
        private const val APP_OS_VALUE = "android_native"
        private const val APP_OS_KEY = "app_os"
        private const val AUTH_TOKEN = "auth_token"
        private const val SESSION_ID = "session_id"
        private const val INSTALL_ID = "install_id"
        private const val APP_VERSION = "app_version"
    }

    fun getServerCallObject(): Apis? {
        return try {
            if (null == mAppService) {
                val retrofit = Retrofit.Builder().apply {
                    baseUrl(BuildConfig.BASE_URL)
                    addConverterFactory(GsonConverterFactory.create())
                    client(getHttpClient())
                }.build()
                mAppService = retrofit.create(Apis::class.java)
            }
            mAppService
        } catch (e: Exception) {
            Log.e(TAG, "getServerCallObject: ${e.message}", e)
            throw IOException(e.message)
        }
    }

    fun getAnalyticsServerCallObject(): Apis? {
        return try {
            if (null == mAppAnalyticsService) {
                val retrofit = Retrofit.Builder().apply {
                    baseUrl(if (BuildConfig.DEBUG) BuildConfig.BASE_URL else Constants.ANALYTICS_PRODUCTION_URL)
                    addConverterFactory(GsonConverterFactory.create())
                    client(getHttpClient())
                }.build()
                mAppAnalyticsService = retrofit.create(Apis::class.java)
            }
            mAppAnalyticsService
        } catch (e: Exception) {
            Log.e(TAG, "getServerCallObject: ${e.message}", e)
            throw IOException(e.message)
        }
    }

    private fun getHttpClient(): OkHttpClient {
        val loggingInterface = HttpLoggingInterceptor().apply {
            level = when (BuildConfig.DEBUG) {
                true  -> HttpLoggingInterceptor.Level.BODY
                false -> HttpLoggingInterceptor.Level.NONE
            }
        }
        return try {
            OkHttpClient.Builder().apply {
                connectTimeout(1, TimeUnit.MINUTES)
                callTimeout(1, TimeUnit.MINUTES)
                readTimeout(30, TimeUnit.SECONDS)
                writeTimeout(30, TimeUnit.SECONDS)
                addInterceptor { chain ->
                    try {
                        val response = customizeCustomRequest(chain)
                        val bodyString = response.body?.string() ?: ""
                        response.newBuilder().body(bodyString.toResponseBody(response.body?.contentType())).build()
                    } catch (e: Exception) {
                        val msg: String = when (e) {
                            is SocketTimeoutException -> "Timeout - Please check your internet connection"
                            is UnknownHostException -> "Unable to make a connection. Please check your internet"
                            is ConnectionShutdownException -> "Connection shutdown. Please check your internet"
                            is IOException -> "Server is unreachable, please try again later."
                            is IllegalStateException -> "${e.message}"
                            else -> "${e.message}"
                        }
                        Log.e(TAG, "getHttpClient: $msg", e)
                        Response.Builder().apply {
                            request(chain.request())
                            protocol(Protocol.HTTP_1_1)
                            protocol(Protocol.HTTP_2)
                            code(999)
                            message(msg)
                            body("{${e}}".toResponseBody(null))
                        }.build()
                    }
                }
                addInterceptor(loggingInterface)
                protocols(arrayListOf(Protocol.HTTP_1_1, Protocol.HTTP_2))
            }.build()
        } catch (e: Exception) {
            throw Exception(e.message)
        }
    }

    private fun customizeCustomRequest(chain: Interceptor.Chain): Response {
        try {
            val originalRequest = chain.request()
            val newRequest = getNewRequest(originalRequest)
            return if (null == newRequest) chain.proceed(originalRequest) else chain.proceed(newRequest)
        } catch (e: Exception) {
            Log.e(TAG, "customizeCustomRequest: ${e.message}", e)
            throw Exception(e.message)
        }
    }

    private fun getNewRequest(originalRequest: Request): Request? {
        return try {
            originalRequest.newBuilder().apply {
                addHeader(AUTH_TOKEN, PrefsManager.getStringDataFromSharedPref(Constants.USER_AUTH_TOKEN))
                addHeader(SESSION_ID, StaticInstances.sAppSessionId ?: "")
                addHeader(INSTALL_ID, PrefsManager.getStringDataFromSharedPref(PrefsManager.APP_INSTANCE_ID))
                addHeader(APP_OS_KEY, APP_OS_VALUE)
                addHeader(APP_VERSION, BuildConfig.VERSION_NAME)
            }.build()
        } catch (e: Exception) {
            Log.e(TAG, "getNewRequest: ${e.message}", e)
            null
        }
    }
}
