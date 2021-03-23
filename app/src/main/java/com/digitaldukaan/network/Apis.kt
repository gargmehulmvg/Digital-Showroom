package com.digitaldukaan.network

import com.digitaldukaan.models.request.*
import com.digitaldukaan.models.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface Apis {

    @POST("api/dotk/merchant/generateOtp")
    suspend fun generateOTP(@Body request: GenerateOtpRequest) : Response<GenerateOtpResponse>

    @POST("api/dotk/merchant/login")
    suspend fun validateOTP(@Body request: ValidateOtpRequest) : Response<ValidateOtpResponse>

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

    @POST("api/dotk/settings/setStoreName")
    suspend fun setStoreName(@Header("auth_token") authToken:String, @Body request: StoreNameRequest) : Response<CommonApiResponse>

    @POST("api/dotk/merchant/updateStoreDomain")
    suspend fun updateStoreDomain(@Header("auth_token") authToken:String, @Body request: StoreLinkRequest) : Response<StoreDescriptionResponse>

    @POST("api/dotk/merchant/setAddress")
    suspend fun updateStoreAddress(@Header("auth_token") authToken:String, @Body request: StoreAddressRequest) : Response<StoreAddressResponse>

    @GET("api/dotk/settings/getAllBusinessList")
    suspend fun getBusinessList() : Response<CommonApiResponse>

    @POST("api/dotk/settings/setStoreBusinesses")
    suspend fun setStoreBusinesses(@Header("auth_token") authToken:String, @Body request: BusinessTypeRequest) : Response<CommonApiResponse>

    @POST("api/dotk/images/setStoreLogo")
    suspend fun setStoreLogo(@Header("auth_token") authToken:String, @Body request: StoreLogoRequest) : Response<CommonApiResponse>

    @Multipart
    @POST("api/dotk/images/uploadImageToS3")
    suspend fun getImageUploadCdnLink(@Header("auth_token") authToken: String, @Part("image_type") imageType: RequestBody, @Part file: MultipartBody.Part?): Response<CommonApiResponse>

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

    @POST("api/dotk/products/shareStore")
    suspend fun getProductShareStoreData(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @GET("api/dotk/marketing/shareStorePdfText")
    suspend fun getShareStorePdfText(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @GET("api/dotk/products/shareStorePdfText")
    suspend fun getProductShareStorePdfText(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @POST("api/dotk/marketing/generateStorePdf")
    suspend fun generateStorePdf(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @POST("api/dotk/marketing/generateStorePdf")
    suspend fun generateProductStorePdf(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @POST("api/dotk/settings/setDeliveryInfo")
    suspend fun updateDeliveryInfo(@Header("auth_token") authToken: String, @Body request: MoreControlsRequest): Response<CommonApiResponse>

    @POST("api/dotk/settings/setBankDetails")
    suspend fun setBankDetails(@Header("auth_token") authToken: String, @Body request: BankDetailsRequest): Response<CommonApiResponse>

    @GET("api/dotk/orders/getOrderAnalytics")
    suspend fun getAnalyticsData(@Header("auth_token") authToken: String) : Response<CommonApiResponse>

    @GET("api/dotk/orders/getOrdersPageInfo")
    suspend fun getOrderPageInfo(@Header("auth_token") authToken: String) : Response<CommonApiResponse>

    @POST("api/dotk/orders/getOrderList")
    suspend fun getOrdersList(@Header("auth_token") authToken: String, @Body request: OrdersRequest): Response<CommonApiResponse>

    @POST("api/dotk/orders/getSearchOrdersList")
    suspend fun getSearchOrdersList(@Header("auth_token") authToken: String, @Body request: SearchOrdersRequest): Response<CommonApiResponse>

    @POST("api/dotk/settings/initiateKyc")
    suspend fun initiateKyc(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @POST("api/dotk/orders/updateOrderStatus")
    suspend fun updateOrderStatus(@Header("auth_token") authToken: String, @Body request: UpdateOrderRequest): Response<CommonApiResponse>

    @GET("api/dotk/orders/getOrderDetails/{orderId}")
    suspend fun getOrderDetails(@Header("auth_token") authToken: String, @Path("orderId") orderId: String): Response<CommonApiResponse>

    @GET("api/dotk/products/getProductPageInfo")
    suspend fun getProductPageInfo(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @GET("api/dotk/products/getMasterCatalogStaticText")
    suspend fun getMasterCatalogStaticText(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @GET("api/dotk/products/getItemInfo/{itemId}")
    suspend fun getItemInfo(@Header("auth_token") authToken: String, @Path("itemId") itemId: Int): Response<CommonApiResponse>

    @POST("api/dotk/products/setItem")
    suspend fun setItem(@Header("auth_token") authToken: String, @Body request: AddProductRequest): Response<CommonApiResponse>
}