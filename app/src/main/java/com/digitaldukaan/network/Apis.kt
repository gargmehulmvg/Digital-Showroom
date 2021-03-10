package com.digitaldukaan.network

import com.digitaldukaan.models.request.*
import com.digitaldukaan.models.response.*
import okhttp3.ResponseBody
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

    @POST("api/dotstore/store/setServices")
    suspend fun changeStoreAndDeliveryStatus(@Body request: StoreDeliveryStatusChangeRequest) : Response<StoreDeliveryStatusChangeResponse>

    @POST("api/dotk/merchant/setStoreDescription")
    suspend fun setStoreDescription(@Header("auth_token") authToken:String, @Body request: StoreDescriptionRequest) : Response<StoreDescriptionResponse>

    @POST("api/dotk/merchant/setStoreName")
    suspend fun setStoreName(@Header("auth_token") authToken:String, @Body request: StoreNameRequest) : Response<StoreDescriptionResponse>

    @POST("api/dotk/merchant/updateStoreDomain")
    suspend fun updateStoreDomain(@Header("auth_token") authToken:String, @Body request: StoreLinkRequest) : Response<StoreDescriptionResponse>

    @POST("api/dotk/merchant/setAddress")
    suspend fun updateStoreAddress(@Header("auth_token") authToken:String, @Body request: StoreAddressRequest) : Response<StoreAddressResponse>

    @GET("api/dotk/merchant/getbusinessList")
    suspend fun getBusinessList() : Response<BusinessTypeResponse>

    @POST("api/dotk/merchant/setStoreBusinesses")
    suspend fun setStoreBusinesses(@Header("auth_token") authToken:String, @Body request: BusinessTypeRequest) : Response<StoreDescriptionResponse>

    @POST("api/dotk/merchant/setStoreLogo")
    suspend fun setStoreLogo(@Header("auth_token") authToken:String, @Body request: StoreLogoRequest) : Response<StoreDescriptionResponse>

    @GET("api/dotk/merchant/searchImages")
    suspend fun searchImagesFromBing(@Header("auth_token") authToken: String, @Query("search_text") searchText: String, @Query("store_id") storeId: String) : Response<ImagesSearchResponse>

    @GET("api/dotk/template/referApp")
    suspend fun getReferAndEarnData() : Response<ReferEarnResponse>

    @GET("api/dotk/template/referAppWa")
    suspend fun getReferAndEarnDataOverWhatsApp() : Response<ReferEarnOverWhatsAppResponse>

    @GET("api/dotk/template/storeMarketingTextV3")
    suspend fun getMarketingCardsData(): Response<MarketingCardsResponse>

    @POST("api/dotk/template/shareStore")
    suspend fun getShareStoreData(@Header("auth_token") authToken: String, @Body request: StoreLogoRequest): Response<AppShareDataResponse>

    @GET("api/dotk/template/shareStorePdfText")
    suspend fun getShareStorePdfText(): Response<ShareStorePDFDataResponse>

    @POST("api/dotk/catalog/generateStorePdf")
    suspend fun generateStorePdf(@Header("auth_token") authToken: String, @Body request: StoreLogoRequest): Response<ResponseBody>

    @POST("api/dotk/merchant/updateDeliveryInfo")
    suspend fun updateDeliveryInfo(@Header("auth_token") authToken: String, @Body request: MoreControlsRequest): Response<MoreControlsResponse>

    @GET("api/remoteOrder/getPendingOrders")
    suspend fun getPendingOrders(@Query("store_id") storeId: String, @Query("page") page: Int): Response<OrdersResponse>

    @GET("api/remoteOrder/getCompletedOrders")
    suspend fun getCompletedOrders(@Query("store_id") storeId: String, @Query("page") page: Int): Response<OrdersResponse>
}