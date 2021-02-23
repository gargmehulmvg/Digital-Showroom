package com.digitaldukaan.network

import com.digitaldukaan.models.request.AuthenticateUserRequest
import com.digitaldukaan.models.request.GenerateOtpRequest
import com.digitaldukaan.models.request.ValidateOtpRequest
import com.digitaldukaan.models.response.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Apis {

    @POST("api/dotk/merchant/generateOtp")
    suspend fun generateOTP(@Body request: GenerateOtpRequest) : Response<GenerateOtpResponse>

    @POST("api/dotk/merchant/login")
    suspend fun validateOTP(@Body request: ValidateOtpRequest) : Response<ValidateOtpResponse>

    @POST("api/dotk/merchant/authenticate")
    suspend fun authenticateUser(@Body request: AuthenticateUserRequest) : Response<ValidateOtpResponse>

    @GET("api/dotk/template/getAppStaticText")
    suspend fun getAppStaticText(@Query("lanuageId") languageId:String) : Response<StaticTextResponse>

    @GET("api/dotk/template/getAccountInfo")
    suspend fun getProfileResponse(@Query("store_id") storeId:String) : Response<ProfileResponse>

    @GET("api/dotk/template/getProfileInfo")
    suspend fun getProfilePreviewResponse(@Query("store_id") storeId:String) : Response<ProfilePreviewResponse>
}