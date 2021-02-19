package com.digitaldukaan.network

import com.digitaldukaan.models.request.GenerateOtpRequest
import com.digitaldukaan.models.response.GenerateOtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface Apis {

    @POST("api/dotk/merchant/generateOtp")
    suspend fun generateOTP(@Body request: GenerateOtpRequest) : Response<GenerateOtpResponse>

}