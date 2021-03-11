package com.digitaldukaan.network

import com.digitaldukaan.models.request.*
import com.digitaldukaan.models.response.*
import retrofit2.Response
import retrofit2.http.*

interface Apis {

    @POST("api/dotk/merchant/generateOtp")
    suspend fun generateOTP(@Body request: GenerateOtpRequest) : Response<GenerateOtpResponse>

    @POST("api/dotk/merchant/login")
    suspend fun validateOTP(@Body request: ValidateOtpRequest) : Response<ValidateOtpResponse>

    @POST("api/dotk/merchant/authenticate")
    suspend fun authenticateUser(@Body request: AuthenticateUserRequest) : Response<ValidateOtpResponse>

    @GET("api/dotk/template/getAppStaticText")
    suspend fun getAppStaticText(@Query("lanuageId") languageId:String) : Response<StaticTextResponse>

    @GET("api/dotk/settings/getAccountInfo")
    suspend fun getProfileResponse(@Header("auth_token") authToken:String) : Response<CommonApiResponse>

    @GET("api/dotk/settings/getProfileInfo")
    suspend fun getProfilePreviewResponse(@Header("auth_token") authToken:String) : Response<CommonApiResponse>

    @POST("api/dotk/settings/setServices")
    suspend fun changeStoreAndDeliveryStatus(@Header("auth_token") authToken:String, @Body request: StoreDeliveryStatusChangeRequest) : Response<CommonApiResponse>

    @POST("api/dotk/settings/setStoreDescription")
    suspend fun setStoreDescription(@Header("auth_token") authToken:String, @Body request: StoreDescriptionRequest) : Response<CommonApiResponse>

    @POST("api/dotk/merchant/setStoreName")
    suspend fun setStoreName(@Header("auth_token") authToken:String, @Body request: StoreNameRequest) : Response<StoreDescriptionResponse>

    @POST("api/dotk/merchant/updateStoreDomain")
    suspend fun updateStoreDomain(@Header("auth_token") authToken:String, @Body request: StoreLinkRequest) : Response<StoreDescriptionResponse>

    @POST("api/dotk/merchant/setAddress")
    suspend fun updateStoreAddress(@Header("auth_token") authToken:String, @Body request: StoreAddressRequest) : Response<StoreAddressResponse>

    @GET("api/dotk/settings/getAllBusinessList")
    suspend fun getBusinessList() : Response<CommonApiResponse>

    @POST("api/dotk/settings/setStoreBusinesses")
    suspend fun setStoreBusinesses(@Header("auth_token") authToken:String, @Body request: BusinessTypeRequest) : Response<CommonApiResponse>

    @POST("api/dotk/settings/setStoreLogo")
    suspend fun setStoreLogo(@Header("auth_token") authToken:String, @Body request: StoreLogoRequest) : Response<CommonApiResponse>

    @GET("api/dotk/merchant/searchImages")
    suspend fun searchImagesFromBing(@Header("auth_token") authToken: String, @Query("search_text") searchText: String, @Query("store_id") storeId: String) : Response<ImagesSearchResponse>

    @GET("api/dotk/template/referApp")
    suspend fun getReferAndEarnData() : Response<ReferEarnResponse>

    @GET("api/dotk/template/referAppWa")
    suspend fun getReferAndEarnDataOverWhatsApp() : Response<ReferEarnOverWhatsAppResponse>

    @GET("api/dotk/marketing/getStoreMarketingText")
    suspend fun getMarketingCardsData(): Response<CommonApiResponse>

    @POST("api/dotk/marketing/shareStore")
    suspend fun getShareStoreData(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @GET("api/dotk/marketing/shareStorePdfText")
    suspend fun getShareStorePdfText(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @POST("api/dotk/marketing/generateStorePdf")
    suspend fun generateStorePdf(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @POST("api/dotk/settings/setDeliveryInfo")
    suspend fun updateDeliveryInfo(@Header("auth_token") authToken: String, @Body request: MoreControlsRequest): Response<CommonApiResponse>

    @GET("api/remoteOrder/getPendingOrders")
    suspend fun getPendingOrders(@Query("store_id") storeId: String, @Query("page") page: Int): Response<OrdersResponse>

    @GET("api/remoteOrder/getCompletedOrders")
    suspend fun getCompletedOrders(@Query("store_id") storeId: String, @Query("page") page: Int): Response<OrdersResponse>

    @POST("api/dotk/settings/setBankDetails")
    suspend fun setBankDetails(@Header("auth_token") authToken: String, @Body request: BankDetailsRequest): Response<CommonApiResponse>

    @GET("api/dotk/orders/getOrderAnalytics")
    suspend fun getAnalyticsData(@Header("auth_token") authToken: String) : Response<CommonApiResponse>
}