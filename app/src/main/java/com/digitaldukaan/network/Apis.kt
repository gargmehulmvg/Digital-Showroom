package com.digitaldukaan.network

import com.digitaldukaan.models.request.*
import com.digitaldukaan.models.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface Apis {

    /* ----------------------       On-Boarding         ---------------------- */
    @GET("api/dotk/template/getAppStaticText")
    suspend fun getAppStaticText(@Query("lanuageId") languageId:String) : Response<StaticTextResponse>

    @POST("api/dotk/vc1/onboard/createStore")
    suspend fun createStore(@Body request: CreateStoreRequest?): Response<CommonApiResponse>

    @GET("api/dotk/vo1/onboard/getHelpScreens")
    suspend fun getHelpScreens(): Response<CommonApiResponse>

    @POST("api/dotk/vo1/onboard/validateUser")
    suspend fun validateUser(@Body request: ValidateUserRequest): Response<CommonApiResponse>

    @POST("api/dotk/vo1/user/generateOtp/{phoneNumber}")
    suspend fun generateOTP(@Path("phoneNumber") phoneNumber: String) : Response<GenerateOtpResponse>

    @POST("api/dotk/merchant/login")
    suspend fun validateOTP(@Body request: ValidateOtpRequest) : Response<ValidateOtpResponse>

    /* ----------------------       Settings         ---------------------- */
    @GET("api/dotk/vm1/settings/getAccountInfo")
    suspend fun getProfileResponse() : Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getProfileInfo")
    suspend fun getProfilePreviewResponse() : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setServices")
    suspend fun changeStoreAndDeliveryStatus(@Body request: StoreDeliveryStatusChangeRequest) : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setStoreDescription")
    suspend fun setStoreDescription(@Body request: StoreDescriptionRequest) : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setStoreName")
    suspend fun setStoreName(@Body request: StoreNameRequest) : Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/getAllBusinessList")
    suspend fun getBusinessList() : Response<CommonApiResponse>

    @GET("api/dotk/vm1/settings/referApp")
    suspend fun getReferAndEarnData() : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setStoreBusinesses")
    suspend fun setStoreBusinesses(@Header("auth_token") authToken:String, @Body request: BusinessTypeRequest) : Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setDeliveryInfo")
    suspend fun updateDeliveryInfo(@Header("auth_token") authToken: String, @Body request: MoreControlsRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/setBankDetails")
    suspend fun setBankDetails(@Header("auth_token") authToken: String, @Body request: BankDetailsRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/settings/initiateKyc")
    suspend fun initiateKyc(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    /* ----------------------       Images         ---------------------- */
    @POST("api/dotk/vm1/media/setStoreLogo")
    suspend fun setStoreLogo(@Header("auth_token") authToken:String, @Body request: StoreLogoRequest) : Response<CommonApiResponse>

    @Multipart
    @POST("api/dotk/vm1/media/uploadMediaToS3")
    suspend fun getImageUploadCdnLink(@Part("media_type") imageType: RequestBody, @Part file: MultipartBody.Part?): Response<CommonApiResponse>

    @GET("api/dotk/vm1/merchant/searchImages")
    suspend fun searchImagesFromBing(@Header("auth_token") authToken: String, @Query("search_text") searchText: String, @Query("store_id") storeId: String) : Response<ImagesSearchResponse>

    /* ----------------------       Marketing         ---------------------- */
    @GET("api/dotk/vm1/marketing/getStoreMarketingText")
    suspend fun getMarketingCardsData(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/marketing/shareStore")
    suspend fun getShareStoreData(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @GET("api/dotk/vm1/marketing/shareStorePdfText")
    suspend fun getShareStorePdfText(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @POST("api/dotk/vm1/marketing/generateStorePdf")
    suspend fun generateStorePdf(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @POST("api/dotk/vm1/marketing/generateStorePdf")
    suspend fun generateProductStorePdf(): Response<CommonApiResponse>

    /* ----------------------       Products         ---------------------- */
    @GET("api/dotk/vm1/products/shareStorePdfText")
    suspend fun getProductShareStorePdfText(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/shareStore")
    suspend fun getProductShareStoreData(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getProductPageInfo")
    suspend fun getProductPageInfo(@Header("auth_token") authToken: String): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getMasterCatalogStaticText")
    suspend fun getMasterCatalogStaticText(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getItemInfo/{itemId}")
    suspend fun getItemInfo(@Header("auth_token") authToken: String, @Path("itemId") itemId: Int): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/setItem")
    suspend fun setItem(@Header("auth_token") authToken: String, @Body request: AddProductRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getMasterCategories")
    suspend fun getMasterCategories(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getMasterSubCategories/{id}")
    suspend fun getMasterSubCategories(@Path("id") id: Int): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getMasterItems/{id}")
    suspend fun getMasterItems(@Path("id") id: Int, @Query("page") page: Int): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/buildCatalog")
    suspend fun buildCatalog(@Body request: BuildCatalogRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/updateStock")
    suspend fun updateStock(@Body request: UpdateStockRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getUserCategories")
    suspend fun getUserCategories(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/products/getDeleteCategoryInfo")
    suspend fun getDeleteCategoryInfo(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/updateCategory")
    suspend fun updateCategory(@Body request: UpdateCategoryRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/deleteCategory")
    suspend fun deleteCategory(@Body request: DeleteCategoryRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/products/deleteItem")
    suspend fun deleteItem(@Body request: DeleteItemRequest): Response<CommonApiResponse>

    /* ----------------------       Orders         ---------------------- */
    @GET("api/dotk/vm1/orders/getOrderAnalytics")
    suspend fun getAnalyticsData() : Response<CommonApiResponse>

    @GET("api/dotk/vm1/orders/getOrdersPageInfo")
    suspend fun getOrderPageInfo() : Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/getOrderList")
    suspend fun getOrdersList(@Header("auth_token") authToken: String, @Body request: OrdersRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/getSearchOrdersList")
    suspend fun getSearchOrdersList(@Body request: SearchOrdersRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/updateOrderStatus")
    suspend fun updateOrderStatus(@Body statusRequest: UpdateOrderStatusRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/orders/getOrderDetails/{orderId}")
    suspend fun getOrderDetails(@Header("auth_token") authToken: String, @Path("orderId") orderId: String): Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/completeOrder")
    suspend fun completeOrder(@Body request: CompleteOrderRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/orders/getDeliveryTime")
    suspend fun getDeliveryTime(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/orders/updateOrder")
    suspend fun updateOrder(@Header("auth_token") authToken: String, @Body statusRequest: UpdateOrderRequest): Response<CommonApiResponse>

    /* ----------------------       Premium         ---------------------- */
    @GET("api/dotk/vm1/premium/getPremiumPageInfo")
    suspend fun getPremiumPageInfo(): Response<CommonApiResponse>

    @GET("api/dotk/vm1/premium/getAllPresetColors")
    suspend fun getAllPresetColors(): Response<CommonApiResponse>

    @POST("api/dotk/vm1/premium/setStoreThemeColorPalette")
    suspend fun setStoreThemeColorPalette(@Body request: EditPremiumColorRequest): Response<CommonApiResponse>

    @POST("api/dotk/vm1/premium/setStoreThemeBanner")
    suspend fun setStoreThemeBanner(@Body request: StoreThemeBannerRequest): Response<CommonApiResponse>

    /* ----------------------       Others         ---------------------- */
    @POST("api/dotanalytics/push/androidEventLog")
    suspend fun androidEventLog(@Body request: AndroidEventLogRequest): Response<CommonApiResponse>

    @GET("api/dotk/vm1/template/referAppWa")
    suspend fun getReferAndEarnDataOverWhatsApp() : Response<ReferEarnOverWhatsAppResponse>

    @POST("api/dotk/vm1/merchant/updateStoreDomain")
    suspend fun updateStoreDomain(@Body request: StoreLinkRequest) : Response<StoreDescriptionResponse>

    @POST("api/dotk/vm1/merchant/setAddress")
    suspend fun updateStoreAddress(@Header("auth_token") authToken:String, @Body request: StoreAddressRequest) : Response<StoreAddressResponse>
}